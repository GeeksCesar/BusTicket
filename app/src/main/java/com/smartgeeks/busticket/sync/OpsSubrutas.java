package com.smartgeeks.busticket.sync;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.smartgeeks.busticket.Modelo.SubRuta;
import com.smartgeeks.busticket.Utils.Constantes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OpsSubrutas {

    private static final String TAG = OpsSubrutas.class.getSimpleName();
    private static final Gson gson = new Gson();

    public static void realizarSincronizacionLocal(Context context) {
        Log.i(TAG, "Actualizando el cliente.");

        VolleySingleton.getInstance(context).addToRequestQueue(
                new StringRequest(
                        Request.Method.GET,
                        Constantes.GET_SUBRUTAS,
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
     * Procesa la respuesta del servidor al pedir que se retornen todos los subrutas.
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

        JSONArray subrutas = null;

        try {
            // Obtener array "subrutas"
            subrutas = response.getJSONArray(Constantes.SUBRUTAS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Parsear con Gson
        SubRuta[] res = gson.fromJson(subrutas != null ? subrutas.toString() : null, SubRuta[].class);
        List<SubRuta> data = Arrays.asList(res);
        Log.i(TAG, "Se encontraron " + data.size() + " registros remotos.");

        // Tabla hash para recibir las entradas entrantes
        HashMap<String, SubRuta> expenseMap = new HashMap<String, SubRuta>();
        for (SubRuta subruta : data) {
            expenseMap.put(subruta.getId_remoto(), subruta);
        }

        List<SubRuta> locales = SubRuta.find(SubRuta.class, "remoto IS NOT NULL");
        Log.i(TAG, "Se encontraron " + locales.size() + " registros locales.");

        // Encontrar datos obsoletos
        int numUpdates = 0;
        int numDeletes = 0;
        int numInserts = 0;

        for (SubRuta subruta : locales) {

            // Match son los registros Remotos, esos son los datos que debo tomar para actualizar
            SubRuta match = expenseMap.get(subruta.getId_remoto());

            if (match != null) {
                // Esta entrada existe, por lo que se remueve del mapeado
                expenseMap.remove(subruta.getId_remoto());

                // Comprobar si necesita ser actualizado los datos
                boolean b1 = match.getRuta_id() != subruta.getRuta_id();
                boolean b2 = match.getParada() != null && !match.getParada().equals(subruta.getParada());
                boolean b3 = match.getTarifa_normal() != subruta.getTarifa_normal();
                boolean b4 = match.getTarifa_frecuente() != subruta.getTarifa_frecuente();
                boolean b5 = match.getTarifa_adulto_mayor() != subruta.getTarifa_adulto_mayor();
                boolean b6 = match.getTarifa_estudiante() != subruta.getTarifa_estudiante();

                if (b1 || b2 || b3 || b4 || b5 || b6) {
                    Log.i(TAG, "Programando actualización de: " + subruta.getId_remoto());
                    match.update();
                    numUpdates++;
                } else {
                    Log.i(TAG, "No hay acciones para este registro: " + subruta.getId_remoto());
                }
            } else {
                // Debido a que la entrada no existe, es removida de la base de datos
                Log.i(TAG, "Programando eliminación de: " + subruta.getId_remoto());
                subruta.delete();
                numDeletes++;
            }
        }

        // Insertar items resultantes
        Log.i(TAG, "Programando inserción de subrutas ");
        for (SubRuta subruta : expenseMap.values()) {
            subruta.save();
            numInserts++;
        }

        Log.i(TAG, "Actualizaciones: " + numUpdates + " Borrados: " + numDeletes + " Nuevos: " + numInserts);
        Log.e(TAG, "Sincronización finalizada.");
    }

}
