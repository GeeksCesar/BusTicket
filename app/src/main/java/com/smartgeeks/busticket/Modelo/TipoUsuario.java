package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;

public class TipoUsuario extends SugarRecord {

    private int _id;
    private String nombre;

    public TipoUsuario() {
    }

    public TipoUsuario(int id, String nombre) {
        this._id = id;
        this.nombre = nombre;
    }

    public int get_Id() {
        return _id;
    }

    public void set_Id(int id) {
        this._id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
