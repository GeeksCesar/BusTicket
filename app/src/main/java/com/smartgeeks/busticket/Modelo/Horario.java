package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;

public class Horario extends SugarRecord {

    int id_remoto;
    int ruta_id;
    int vehiculo_id;
    String fecha;
    String hora;

    public Horario() {
    }

    public Horario(int id_remoto, int ruta_id, int vehiculo_id, String fecha, String hora) {
        this.id_remoto = id_remoto;
        this.ruta_id = ruta_id;
        this.vehiculo_id = vehiculo_id;
        this.fecha = fecha;
        this.hora = hora;
    }

    public int getId_remoto() {
        return id_remoto;
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
