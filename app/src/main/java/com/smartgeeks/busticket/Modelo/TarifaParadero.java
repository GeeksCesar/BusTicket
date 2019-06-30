package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

public class TarifaParadero extends SugarRecord {

    @Unique
    int remoto;
    int paradaInicio;
    int paradaFin;
    int monto;
    int tipoUsuario;
    int idRuta;


    public TarifaParadero() {
    }

    public TarifaParadero(int remoto, int paradaInicio, int paradaFin, int monto, int tipoUsuario, int idRuta) {
        this.remoto = remoto;
        this.paradaInicio = paradaInicio;
        this.paradaFin = paradaFin;
        this.monto = monto;
        this.tipoUsuario = tipoUsuario;
        this.idRuta = idRuta;
    }

    public int getIdRemoto() {
        return remoto;
    }

    public int getParadaInicio() {
        return paradaInicio;
    }

    public int getParadaFin() {
        return paradaFin;
    }

    public int getMonto() {
        return monto;
    }

    public int getTipoUsuario() {
        return tipoUsuario;
    }

    public int getIdRuta() {
        return idRuta;
    }
}
