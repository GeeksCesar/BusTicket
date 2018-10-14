package com.smartgeeks.busticket.sync;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.smartgeeks.busticket.Modelo.Horario;
import com.smartgeeks.busticket.Utils.Constantes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OpsTarifaParadero {

    private static final String TAG = OpsTarifaParadero.class.getSimpleName();
    private static final Gson gson = new Gson();

    public static void realizarSincronizacionLocal(Context context) {
        Log.i(TAG, "Actualizando el cliente.");

        VolleySingleton.getInstance(context).addToRequestQueue(
                new StringRequest(
                        Request.Method.GET,
                        Constantes.GET_TARIFAS_PARADERO,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "Response: " + response);
                                try {
                                    JSONObject object = new JSONObject(response);
                                    procesarRespuestaGet(object);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "" + error);
                            }
                        }
                )
        );
    }

    /**
     * Procesa la respuesta del servidor al pedir que se retornen todos los horarios.
     *
     * @param response Respuesta en formato Json
     */
    private static void procesarRespuestaGet(JSONObject response) {
        try {
            // Obtener atributo "estado"
            String estado = response.getString(Constantes.ESTADO);

            switch (estado) {
                case Constantes.SUCCESS: // EXITO
                    actualizarDatosLocales(response);
                    break;
                case Constantes.FAILED: // FALLIDO
                    Log.e(TAG, "Error al traer datos");
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Actualiza los registros locales a través de una comparación con los datos
     * del servidor
     *
     * @param response Respuesta en formato Json obtenida del servidor
     */
    private static void actualizarDatosLocales(JSONObject response) {

        JSONArray horarios = null;

        try {
            // Obtener array "horarios"
            horarios = response.getJSONArray(Constantes.TARIFAS_PARADERO);
            // Parsear con Gson
            Horario[] res = gson.fromJson(horarios != null ? horarios.toString() : null, Horario[].class);
            List<Horario> data = Arrays.asList(res);
            Log.i(TAG, "Se encontraron " + data.size() + " registros remotos.");

            // Tabla hash para recibir las entradas entrantes
            HashMap<String, Horario> expenseMap = new HashMap<String, Horario>();
            for (Horario horario : data) {
                expenseMap.put(horario.getId_remoto(), horario);
            }

            List<Horario> locales = Horario.find(Horario.class, "remoto IS NOT NULL");
            Log.i(TAG, "Se encontraron " + locales.size() + " registros locales.");

            // Encontrar datos obsoletos
            int numUpdates = 0;
            int numDeletes = 0;
            int numInserts = 0;

            for (Horario horario : locales) {

                // Match son los registros Remotos, esos son los datos que debo tomar para actualizar
                Horario match = expenseMap.get(horario.getId_remoto());

                if (match != null) {
                    // Esta entrada existe, por lo que se remueve del mapeado
                    expenseMap.remove(horario.getId_remoto());

                    // Comprobar si necesita ser actualizado los datos
                    boolean b1 = match.getRuta_id() != horario.getRuta_id();
                    boolean b2 = match.getVehiculo_id() != horario.getVehiculo_id();
                    boolean b3 = match.getFecha() != null && !match.getFecha().equals(horario.getFecha());
                    boolean b4 = match.getHora() != null && !match.getHora().equals(horario.getHora());

                    if (b1 || b2 || b3 || b4) {
                        Log.i(TAG, "Programando actualización de: " + horario.getId_remoto());
                        match.update();
                        numUpdates++;
                    } else {
                        Log.i(TAG, "No hay acciones para este registro: " + horario.getId_remoto());
                    }
                } else {
                    // Debido a que la entrada no existe, es removida de la base de datos
                    Log.i(TAG, "Programando eliminación de: " + horario.getId_remoto());
                    horario.delete();
                    numDeletes++;
                }
            }

            // Insertar items resultantes
            Log.i(TAG, "Programando inserción de horarios ");
            for (Horario horario : expenseMap.values()) {
                horario.save();
                numInserts++;
            }

            Log.i(TAG, "Actualizaciones: " + numUpdates + " Borrados: " + numDeletes + " Nuevos: " + numInserts);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
