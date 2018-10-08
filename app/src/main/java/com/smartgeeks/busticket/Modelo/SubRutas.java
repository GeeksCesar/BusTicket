package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;

public class SubRutas extends SugarRecord {

    int _id;
    int ruta_id;
    String parada;
    double tarifa_normal;
    double tarifa_frecuente;
    double tarifa_adulto_mayor;
    double tarifa_estudiante;

    public SubRutas() {
    }

    public SubRutas(int id, int ruta_id, String parada, double tarifa_normal, double tarifa_frecuente, double tarifa_adulto_mayor, double tarifa_estudiante) {
        this._id = id;
        this.ruta_id = ruta_id;
        this.parada = parada;
        this.tarifa_normal = tarifa_normal;
        this.tarifa_frecuente = tarifa_frecuente;
        this.tarifa_adulto_mayor = tarifa_adulto_mayor;
        this.tarifa_estudiante = tarifa_estudiante;
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

    public String getParada() {
        return parada;
    }

    public void setParada(String parada) {
        this.parada = parada;
    }

    public double getTarifa_normal() {
        return tarifa_normal;
    }

    public void setTarifa_normal(double tarifa_normal) {
        this.tarifa_normal = tarifa_normal;
    }

    public double getTarifa_frecuente() {
        return tarifa_frecuente;
    }

    public void setTarifa_frecuente(double tarifa_frecuente) {
        this.tarifa_frecuente = tarifa_frecuente;
    }

    public double getTarifa_adulto_mayor() {
        return tarifa_adulto_mayor;
    }

    public void setTarifa_adulto_mayor(double tarifa_adulto_mayor) {
        this.tarifa_adulto_mayor = tarifa_adulto_mayor;
    }

    public double getTarifa_estudiante() {
        return tarifa_estudiante;
    }

    public void setTarifa_estudiante(double tarifa_estudiante) {
        this.tarifa_estudiante = tarifa_estudiante;
    }
}
