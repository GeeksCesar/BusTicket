package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;

@Table
public class Horario extends SugarRecord {

    /**
     * remoto => Indica el id remoto
     */
    @Unique
    String remoto;
    int ruta_id;
    int vehiculo_id;
    String fecha;
    String hora;

    public Horario() {
    }

    public Horario(String id_remoto, int ruta_id, int vehiculo_id, String fecha, String hora) {
        this.remoto = id_remoto;
        this.ruta_id = ruta_id;
        this.vehiculo_id = vehiculo_id;
        this.fecha = fecha;
        this.hora = hora;
    }

    public String getId_remoto() {
        return remoto;
    }

    public int getRuta_id() {
        return ruta_id;
    }

    public int getVehiculo_id() {
        return vehiculo_id;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }
}
