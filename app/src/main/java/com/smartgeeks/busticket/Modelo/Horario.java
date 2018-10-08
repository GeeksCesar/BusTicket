package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;

public class Horario extends SugarRecord {

    int _id;
    int ruta_id;
    int vehiculo_id;
    String fecha;
    String hora;

    public Horario() {
    }

    public Horario(int id, int ruta_id, int vehiculo_id, String fecha, String hora) {
        this._id = id;
        this.ruta_id = ruta_id;
        this.vehiculo_id = vehiculo_id;
        this.fecha = fecha;
        this.hora = hora;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getRuta_id() {
        return ruta_id;
    }

    public void setRuta_id(int ruta_id) {
        this.ruta_id = ruta_id;
    }

    public int getVehiculo_id() {
        return vehiculo_id;
    }

    public void setVehiculo_id(int vehiculo_id) {
        this.vehiculo_id = vehiculo_id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }
}
