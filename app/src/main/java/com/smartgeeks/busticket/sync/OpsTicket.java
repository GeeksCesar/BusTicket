package com.smartgeeks.busticket.sync;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.smartgeeks.busticket.Modelo.Ticket;
import com.smartgeeks.busticket.Utils.Constantes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpsTicket {

    private static final String TAG = OpsTicket.class.getSimpleName();
    private static final Gson gson = new Gson();


    /**
     * ------------    Sincronización remota REMOTE SYNC  -------------------
     */
    public static void realizarSincronizacionRemota(final Context context) {
        Log.e(TAG, "Actualizando el servidor...");
        final Intent localIntent = new Intent(Constantes.ACTION_RUN_REMOTE_SYNC);

        iniciarActualizacion();

        List<Ticket> tickets_to_sync = obtenerRegistrosSucios();
        Log.e(TAG, "Se encontraron " + tickets_to_sync.size() + " registros por Sincronizar.");

        if (tickets_to_sync.size() > 0) {

            for (Ticket ticket : tickets_to_sync) {

                final Long idLocal = ticket.getId();

                VolleySingleton.getInstance(context).addToRequestQueue(
                        new JsonObjectRequest(
                                Request.Method.POST,
                                Constantes.INSERT_TICKET,
                                setJSONObject(ticket),
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.i(TAG, "response: " + response);
                                        procesarRespuestaInsert(response, idLocal);

                                        localIntent.putExtra(Constantes.EXTRA_PROGRESS, true);
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e(TAG, "Error Volley: " + error.getMessage());

                                        // Emisión para avisar que se terminó el servicio
                                        localIntent.putExtra(Constantes.EXTRA_PROGRESS, false);
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);

                                    }
                                }

                        ) {
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<String, String>();
                                headers.put("Content-Type", "application/json; charset=utf-8");
                                headers.put("Accept", "application/json");
                                return headers;
                            }

                            @Override
                            public String getBodyContentType() {
                                return "application/json; charset=utf-8" + getParamsEncoding();
                            }
                        }
                );
            }

        } else {
            Log.i(TAG, "No se requiere sincronización");
        }

        // Emisión para avisar que se terminó el servicio
        Log.e(TAG, "...Emisión de finalización de sincronización Remota...");

        localIntent.putExtra(Constantes.EXTRA_PROGRESS, false);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    private static JSONObject setJSONObject(Ticket ticket) {
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(gson.toJson(ticket));
            Log.e(TAG, "Ticket: " + ticket.getId() + " " + jsonObject.toString());
            // Eliminamos propiedades (No las necesita el servidor)
            jsonObject.remove("pendiente");
            jsonObject.remove("estado");
            jsonObject.remove("remoto");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * Procesa los diferentes tipos de respuesta obtenidos del servidor
     *
     * @param response Respuesta en formato Json
     */
    private static void procesarRespuestaInsert(JSONObject response, Long idLocal) {
        try {
            // Obtener estado
            String estado = response.getString(Constantes.ESTADO);
            // Obtener mensaje
            String mensaje = response.getString(Constantes.MENSAJE);

            switch (estado) {
                case Constantes.SUCCESS:
                    Log.i(TAG, mensaje);
                    // Obtener identificador del nuevo registro creado en el servidor
                    String idRemota = response.getString("remoto");
                    finalizarActualizacion(idRemota, idLocal);
                    break;

                case Constantes.FAILED:
                    Log.e(TAG, mensaje);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Limpia el registro que se sincronizó y le asigna la nueva id remota proveida
     * por el servidor
     *
     * @param idRemota id remota
     */
    private static void finalizarActualizacion(String idRemota, Long idLocal) {
        // Si se registró remotamente, el ticket. Lo elimino de la base de datos local
        try {
            Ticket ticket = Ticket.findById(Ticket.class, idLocal);
            ticket.delete();
        } catch (Exception ex){
            ex.printStackTrace();
        }

        Log.e(TAG, "Registro " + idRemota + " sincronización completada");
    }


    /**
     * Cambia a estado "de sincronización" el registro que se acaba de insertar localmente
     */
    private static void iniciarActualizacion() {
        /**
         * Cambiamos el estado a sync en ticket de todos los registros que tengan
         * pendiente_insercion = 1 y estado = 0
         */
        List<Ticket> tickets = Ticket.find(Ticket.class, "estado = ? AND pendiente = ?",
                "0", "" + Constantes.ESTADO_SYNC);

        for (Ticket ticket : tickets) {
            // Actualizo los estados de sincronización
            Log.e(TAG, "Pendiente: " + ticket.getId());
            ticket = Ticket.findById(Ticket.class, ticket.getId());
            ticket.setEstado(Constantes.ESTADO_SYNC); // ESTADO_SYNC = 1
            ticket.save();
        }

        Log.i(TAG, "Registros puestos en cola de inserción: " + tickets.size());
    }

    /**
     * Obtiene el registro que se acaba de marcar como "pendiente por sincronizar" y
     * con "estado de sincronización"
     *
     * @return List con el registro.
     */
    private static List<Ticket> obtenerRegistrosSucios() {
        List<Ticket> tickets = Ticket.find(Ticket.class, "pendiente = ? AND estado = ?",
                "1", "" + Constantes.ESTADO_SYNC);

        return tickets;
    }


}
