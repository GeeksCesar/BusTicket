package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;

public class TipoUsuario extends SugarRecord {

    int id_remoto;
    String nombre;

    public TipoUsuario() {
    }

    public TipoUsuario(int id_remoto, String nombre) {
        this.id_remoto = id_remoto;
        this.nombre = nombre;
    }

    public int getId_remoto() {
        return id_remoto;
    }

    public String getNombre() {
        return nombre;
    }
}
