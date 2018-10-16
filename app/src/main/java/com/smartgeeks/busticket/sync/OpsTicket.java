package com.smartgeeks.busticket.sync;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.smartgeeks.busticket.Modelo.Ruta;
import com.smartgeeks.busticket.Modelo.Silla;
import com.smartgeeks.busticket.Modelo.Ticket;
import com.smartgeeks.busticket.Utils.Constantes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
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

        iniciarActualizacion();

        List<Ticket> tickets_to_sync = obtenerRegistrosSucios();

        Log.i(TAG, "Se encontraron " + tickets_to_sync.size() + " registros por Sincronizar.");

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
                                        Log.i(TAG, "response: "+response);
                                        procesarRespuestaInsert(response, idLocal);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e(TAG, "Error Volley: " + error.getMessage());
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

    }

    private static JSONObject setJSONObject(Ticket ticket) {
        JSONObject jsonObject = null;

        List<Silla> sillas = Silla.find(Silla.class, "ticket = ?",
                ""+ticket.getId());
        ArrayList<Integer> sillas_send = new ArrayList<>();
        for (Silla silla : sillas){
            sillas_send.add(silla.getNumero_silla());
        }

        try {
            jsonObject = new JSONObject(gson.toJson(ticket));
            // Eliminamos propiedades (No las necesita el servidor)
            jsonObject.remove("pendiente");
            jsonObject.remove("estado");
            jsonObject.remove("remoto");
            jsonObject.remove("id");
            jsonObject.put("sillas", sillas_send);

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
                    Log.i(TAG, mensaje);
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

        Ticket ticket = Ticket.findById(Ticket.class, idLocal);
        ticket.setPendiente_insercion(0);
        ticket.setEstado(Constantes.ESTADO_OK);
        ticket.setId_remoto(idRemota);
        ticket.update();

        Log.i(TAG, "Registro " + idRemota + " sincronización completada");
    }


    /**
     * Cambia a estado "de sincronización" el registro que se acaba de insertar localmente
     */
    private static void iniciarActualizacion() {
        /**
         * Cambiamos el estado a sync en ticket de todos los registros que tengan
         * pendiente_insercion = 1 y estado = 0
         */
        List<Ticket> tickets = Ticket.find(Ticket.class, "pendiente = ? AND estado = ?",
                "1", "" + Constantes.ESTADO_OK);

        for (Ticket ticket : tickets) {
            Log.e(TAG, "" + ticket.getPendiente_insercion());
            ticket.setEstado(Constantes.ESTADO_SYNC); // ESTADO_SYNC = 1
            ticket.update();
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


    /**
     * -------------- LOCAL SYNC -----------
     **/

    public static void realizarSincronizacionLocal(Context context) {
        Log.i(TAG, "Actualizando el cliente.");

        VolleySingleton.getInstance(context).addToRequestQueue(
                new StringRequest(
                        Request.Method.GET,
                        Constantes.GET_RUTAS,
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
     * Procesa la respuesta del servidor al pedir que se retornen todos los rutas.
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

        for (Ruta ruta : locales) {

            // Match son los registros Remotos, esos son los datos que debo tomar para actualizar
            Ruta match = expenseMap.get(ruta.getId_remoto());

            if (match != null) {
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
        for (Ruta ruta : expenseMap.values()) {
            ruta.save();
            numInserts++;
        }

        Log.i(TAG, "Actualizaciones: " + numUpdates + " Borrados: " + numDeletes + " Nuevos: " + numInserts);
        Log.e(TAG, "Sincronización finalizada.");
    }

}
