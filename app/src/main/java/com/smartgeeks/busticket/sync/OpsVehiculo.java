package com.smartgeeks.busticket.sync;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.smartgeeks.busticket.Modelo.Vehiculo;
import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OpsVehiculo {

    private static final String TAG = OpsVehiculo.class.getSimpleName();
    private static final Gson gson = new Gson();

    public static void realizarSincronizacionLocal(Context context) {
        Log.i(TAG, "Actualizando el cliente.");

        int idEmpresa = UsuarioPreferences.getInstance(context).getIdEmpresa();

        Log.d(TAG, "Url: " + Constantes.GET_VEHICULOS + idEmpresa);
        VolleySingleton.getInstance(context).addToRequestQueue(
                new StringRequest(
                        Request.Method.GET,
                        Constantes.GET_VEHICULOS + idEmpresa,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
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
     * Procesa la respuesta del servidor al pedir que se retornen todos los vehiculos.
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

        JSONArray vehiculos = null;

        try {
            // Obtener array "vehiculos"
            vehiculos = response.getJSONArray(Constantes.VEHICULOS);

            // Parsear con Gson
            Vehiculo[] res = gson.fromJson(vehiculos != null ? vehiculos.toString() : null, Vehiculo[].class);
            List<Vehiculo> data = Arrays.asList(res);
            Log.e(TAG, "Se encontraron " + data.size() + " registros remotos.");

            // Tabla hash para recibir las entradas entrantes
            HashMap<String, Vehiculo> expenseMap = new HashMap<String, Vehiculo>();
            for (Vehiculo vehiculo : data) {
                expenseMap.put(vehiculo.getIdRemoto(), vehiculo);
            }

            List<Vehiculo> locales = Vehiculo.find(Vehiculo.class, "remoto IS NOT NULL");
            Log.i(TAG, "Se encontraron " + locales.size() + " registros locales.");

            // Encontrar datos obsoletos
            int numUpdates = 0;
            int numDeletes = 0;
            int numInserts = 0;

            for (Vehiculo vehiculo : locales) {

                // Match son los registros Remotos, esos son los datos que debo tomar para actualizar
                Vehiculo match = expenseMap.get(vehiculo.getIdRemoto());

                if (match != null) {
                    // Esta entrada existe, por lo que se remueve del mapeado
                    expenseMap.remove(vehiculo.getIdRemoto());

                    // Comprobar si necesita ser actualizado los datos
                    boolean b1 = match.getPlaca() != null && !match.getPlaca().equals(vehiculo.getPlaca());
                    boolean b2 = match.getNumAsientos() != vehiculo.getNumAsientos();
                    boolean b3 = match.getEmpresa() != vehiculo.getEmpresa();

                    if (b1 || b2 || b3) {
                        Log.i(TAG, "Programando actualización de: " + vehiculo.getIdRemoto());
                        match.update();
                        numUpdates++;
                    } else {
                        Log.i(TAG, "No hay acciones para este registro: " + vehiculo.getIdRemoto());
                    }
                } else {
                    // Debido a que la entrada no existe, es removida de la base de datos
                    Log.i(TAG, "Programando eliminación de: " + vehiculo.getIdRemoto());
                    vehiculo.delete();
                    numDeletes++;
                }
            }

            // Insertar items resultantes
            Log.i(TAG, "Programando inserción de Vehiculos ");
            for (Vehiculo vehiculo : expenseMap.values()) {
                vehiculo.save();
                numInserts++;
            }

            Log.i(TAG, "Actualizaciones: " + numUpdates + " Borrados: " + numDeletes + " Nuevos: " + numInserts);
            Log.e(TAG, "Sincronización finalizada.");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
