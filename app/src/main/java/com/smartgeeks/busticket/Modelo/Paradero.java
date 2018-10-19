package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

public class Paradero extends SugarRecord {

    @Unique
    int remoto;
    int ruta;
    String paradero;

    public Paradero() {
    }

    public Paradero(int id_remoto, int ruta, String paradero) {
        this.remoto = id_remoto;
        this.ruta = ruta;
        this.paradero = paradero;
    }

    public int getIdRemoto() {
        return remoto;
    }

    public void setIdRemoto(int remoto) {
        this.remoto = remoto;
    }

    public int getIdRuta() {
        return ruta;
    }

    public void getIdRuta(int ruta) {
        this.ruta = ruta;
    }

    public String getParadero() {
        return paradero;
    }

    public void setParadero(String paradero) {
        this.paradero = paradero;
    }
}
