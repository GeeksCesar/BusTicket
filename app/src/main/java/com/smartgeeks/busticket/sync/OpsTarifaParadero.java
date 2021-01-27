package com.smartgeeks.busticket.sync;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.orm.SugarRecord;
import com.smartgeeks.busticket.Modelo.TarifaParadero;
import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OpsTarifaParadero {

    private static final String TAG = OpsTarifaParadero.class.getSimpleName();
    private static final Gson gson = new Gson();

    public static void realizarSincronizacionLocal(final Context context) {
        //Log.i(TAG, "Actualizando el cliente.");
        int idEmpresa = UsuarioPreferences.getInstance(context).getIdEmpresa();

        //Log.d(TAG, "Url: " + Constantes.GET_TARIFAS_PARADERO + idEmpresa);
        VolleySingleton.getInstance(context).addToRequestQueue(
                new StringRequest(
                        Request.Method.GET,
                        Constantes.GET_TARIFAS_PARADERO + idEmpresa,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    procesarRespuestaGet(object, context);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "" + error);
                                // Emisión para avisar que se terminó el servicio
                                Intent localIntent = new Intent(Constantes.ACTION_FINISH_LOCAL_SYNC);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
                            }
                        }
                )
        );
    }

    /**
     * Procesa la respuesta del servidor al pedir que se retornen todos los tarifas_paradero.
     *
     * @param response Respuesta en formato Json
     */
    private static void procesarRespuestaGet(JSONObject response, Context context) {
        try {
            // Obtener atributo "estado"
            String estado = response.getString(Constantes.ESTADO);

            switch (estado) {
                case Constantes.SUCCESS: // EXITO
                    actualizarDatosLocales(response, context);
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
    private static void actualizarDatosLocales(JSONObject response, Context context) {

        JSONArray tarifas_paradero = null;
        try {
            // Obtener array "tarifas_paradero"
            tarifas_paradero = response.getJSONArray(Constantes.TARIFAS_PARADERO);
            // Parsear con Gson
            TarifaParadero[] res = gson.fromJson(tarifas_paradero != null ? tarifas_paradero.toString() : null, TarifaParadero[].class);
            List<TarifaParadero> data = Arrays.asList(res);
           // Log.e(TAG, "Se encontraron " + data.size() + " registros remotos.");

            // Tabla hash para recibir las entradas entrantes
            HashMap<Integer, TarifaParadero> expenseMap = new HashMap<Integer, TarifaParadero>();
            for (TarifaParadero tarifaParadero : data) {
                expenseMap.put(tarifaParadero.getIdRemoto(), tarifaParadero);
            }

            List<TarifaParadero> locales = TarifaParadero.find(TarifaParadero.class, "remoto IS NOT NULL");
            //Log.i(TAG, "Se encontraron " + locales.size() + " registros locales.");

            // Encontrar datos obsoletos
            int numUpdates = 0;
            int numDeletes = 0;
            int numInserts = 0;

            for (TarifaParadero tarifaParadero : locales) {

                // Match son los registros Remotos, esos son los datos que debo tomar para actualizar
                TarifaParadero match = expenseMap.get(tarifaParadero.getIdRemoto());

                if (match != null) {
                    // Esta entrada existe, por lo que se remueve del mapeado
                    expenseMap.remove(tarifaParadero.getIdRemoto());

                    // Comprobar si necesita ser actualizado los datos
                    boolean b1 = match.getParadaInicio() != tarifaParadero.getParadaInicio();
                    boolean b2 = match.getParadaFin() != tarifaParadero.getParadaFin();
                    boolean b3 = match.getMonto() != tarifaParadero.getMonto();
                    boolean b4 = match.getTipoUsuario() != tarifaParadero.getTipoUsuario();
                    boolean b5 = match.getIdRuta() != tarifaParadero.getIdRuta();

                    if (b1 || b2 || b3 || b4 || b5) {
                   //     Log.i(TAG, "Programando actualización de: " + tarifaParadero.getIdRemoto());
                        match.update();
                        numUpdates++;
                    } else {
                  ///      Log.i(TAG, "No hay acciones para este registro: " + tarifaParadero.getIdRemoto());
                    }
                } else {
                    // Debido a que la entrada no existe, es removida de la base de datos
                    Log.i(TAG, "Programando eliminación de: " + tarifaParadero.getIdRemoto());
                    tarifaParadero.delete();
                    numDeletes++;
                }
            }

            // Insertar items resultantes
            SugarRecord.saveInTx(expenseMap.values());
       //     Log.i(TAG, "Programando inserción de tarifas_paradero ");
          //  Log.i(TAG, "Actualizaciones: " + numUpdates + " Borrados: " + numDeletes);

            // Emisión para avisar que se terminó el servicio
       //     Log.e(TAG, "...Emisión de finalización de sincronización local...");
            Intent localIntent = new Intent(Constantes.ACTION_FINISH_LOCAL_SYNC);
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
