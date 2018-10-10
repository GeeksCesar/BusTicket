package com.smartgeeks.busticket.sync;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.smartgeeks.busticket.Modelo.Ruta;
import com.smartgeeks.busticket.Utils.Constantes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OpsRuta {

    private static final String TAG = OpsRuta.class.getSimpleName();
    private static final Gson gson = new Gson();

    public static void realizarSincronizacionLocal(Context context) {
        Log.i(TAG, "Actualizando el cliente.");

        VolleySingleton.getInstance(context).addToRequestQueue(
                new StringRequest(
                        Request.Method.GET,
                        Constantes.GET_RUTAS,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "Response: "+response);
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
                                Log.e(TAG, ""+error);
                            }
                        }
                )
        );
    }

    /**
     * Procesa la respuesta del servidor al pedir que se retornen todos los rutas.
     *
     * @param response   Respuesta en formato Json
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
     * @param response   Respuesta en formato Json obtenida del servidor
     */
    private static void actualizarDatosLocales(JSONObject response) {

        JSONArray rutas = null;

        try {
            // Obtener array "rutas"
            rutas = response.getJSONArray(Constantes.RUTAS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Parsear con Gson
        Ruta[] res = gson.fromJson(rutas != null ? rutas.toString() : null, Ruta[].class);
        List<Ruta> data = Arrays.asList(res);
        Log.i(TAG, "Se encontraron " + data.size() + " registros remotos.");

        // Tabla hash para recibir las entradas entrantes
        HashMap<String, Ruta> expenseMap = new HashMap<String, Ruta>();
        for (Ruta ruta : data) {
            expenseMap.put(ruta.getId_remoto(), ruta);
        }

        List<Ruta> locales = Ruta.find(Ruta.class, "remoto IS NOT NULL");
        Log.i(TAG, "Se encontraron " + locales.size() + " registros locales.");

        // Encontrar datos obsoletos
        int numUpdates = 0;
        int numDeletes = 0;
        int numInserts = 0;

        for (Ruta ruta : locales){

            // Match son los registros Remotos, esos son los datos que debo tomar para actualizar
            Ruta match = expenseMap.get(ruta.getId_remoto());

            if (match != null){
                // Esta entrada existe, por lo que se remueve del mapeado
                expenseMap.remove(ruta.getId_remoto());

                // Comprobar si necesita ser actualizado los datos
                boolean b1 = match.getPartida() != null && !match.getPartida().equals(ruta.getPartida());
                boolean b2 = match.getDestino() != null && !match.getDestino().equals(ruta.getDestino());

                if (b1 || b2) {
                    Log.i(TAG, "Programando actualización de: " + ruta.getId_remoto());
                    match.update();
                    numUpdates++;
                } else {
                    Log.i(TAG, "No hay acciones para este registro: " + ruta.getId_remoto());
                }
            } else {
                // Debido a que la entrada no existe, es removida de la base de datos
                Log.i(TAG, "Programando eliminación de: " + ruta.getId_remoto());
                ruta.delete();
                numDeletes++;
            }
        }

        // Insertar items resultantes
        Log.i(TAG, "Programando inserción de Rutas ");
        for (Ruta ruta : expenseMap.values()){
            ruta.save();
            numInserts++;
        }

        Log.i(TAG, "Actualizaciones: "+numUpdates+ " Borrados: "+numDeletes + " Nuevos: "+numInserts);
        Log.e(TAG, "Sincronización finalizada.");
    }

}
