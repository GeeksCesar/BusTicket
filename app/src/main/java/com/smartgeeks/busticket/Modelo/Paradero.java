package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

public class Paradero extends SugarRecord {

    @Unique
    String remoto;
    int ruta;
    String paradero;

    public Paradero() {
    }

    public Paradero(String id_remoto, int ruta, String paradero) {
        this.remoto = id_remoto;
        this.ruta = ruta;
        this.paradero = paradero;
    }

    public String getIdRemoto() {
        return remoto;
    }

    public void setIdRemoto(String remoto) {
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
