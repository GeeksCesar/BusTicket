package com.smartgeeks.busticket.sync;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.orm.SugarRecord;
import com.smartgeeks.busticket.Modelo.TipoUsuario;
import com.smartgeeks.busticket.Utils.Constantes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OpsTipoUsuario {

    private static final String TAG = OpsTipoUsuario.class.getSimpleName();
    private static final Gson gson = new Gson();

    public static void realizarSincronizacionLocal(Context context) {
        Log.i(TAG, "Actualizando el cliente.");
        Log.d(TAG, "Url: " + Constantes.GET_TIPOS_USUARIO);
        VolleySingleton.getInstance(context).addToRequestQueue(
                new StringRequest(
                        Request.Method.GET,
                        Constantes.GET_TIPOS_USUARIO,
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
     * Procesa la respuesta del servidor al pedir que se retornen todos los tiposUsuarios.
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

        JSONArray tiposUsuarios = null;

        try {
            // Obtener array "tiposUsuarios"
            tiposUsuarios = response.getJSONArray(Constantes.TIPOS_USUARIO);

            // Parsear con Gson
            TipoUsuario[] res = gson.fromJson(tiposUsuarios != null ? tiposUsuarios.toString() : null, TipoUsuario[].class);
            List<TipoUsuario> data = Arrays.asList(res);
            Log.e(TAG, "Se encontraron " + data.size() + " registros remotos.");

            // Tabla hash para recibir las entradas entrantes
            HashMap<String, TipoUsuario> expenseMap = new HashMap<String, TipoUsuario>();
            for (TipoUsuario ruta : data) {
                expenseMap.put(ruta.getId_remoto(), ruta);
            }

            List<TipoUsuario> locales = TipoUsuario.find(TipoUsuario.class, "remoto IS NOT NULL");
            Log.i(TAG, "Se encontraron " + locales.size() + " registros locales.");

            // Encontrar datos obsoletos
            int numUpdates = 0;
            int numDeletes = 0;
            int numInserts = 0;

            for (TipoUsuario ruta : locales) {

                // Match son los registros Remotos, esos son los datos que debo tomar para actualizar
                TipoUsuario match = expenseMap.get(ruta.getId_remoto());

                if (match != null) {
                    // Esta entrada existe, por lo que se remueve del mapeado
                    expenseMap.remove(ruta.getId_remoto());

                    // Comprobar si necesita ser actualizado los datos
                    boolean b1 = match.getNombre() != null && !match.getNombre().equals(ruta.getNombre());

                    if (b1) {
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
            Log.i(TAG, "Programando inserción de TipoUsuarios ");
            for (TipoUsuario ruta : expenseMap.values()) {
                ruta.save();
                numInserts++;
            }

            Log.i(TAG, "Actualizaciones: " + numUpdates + " Borrados: " + numDeletes + " Nuevos: " + numInserts);
            Log.e(TAG, "Sincronización finalizada.");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
