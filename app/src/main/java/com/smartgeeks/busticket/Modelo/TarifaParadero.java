package com.smartgeeks.busticket.Modelo;

import com.orm.dsl.Unique;

public class TarifaParadero {

    @Unique
    String remoto;
    int inicio;
    int termina;
    double tarifa_normal;
    double tarifa_frecuente;
    double tarifa_adulto_mayor;
    double tarifa_estudiante;
    double tarifa_vale_muni;

    public TarifaParadero() {
    }

    public TarifaParadero(String remoto, int inicio, int termina, double tarifa_normal, double tarifa_frecuente, double tarifa_adulto_mayor, double tarifa_estudiante, double tarifa_vale_muni) {
        this.remoto = remoto;
        this.inicio = inicio;
        this.termina = termina;
        this.tarifa_normal = tarifa_normal;
        this.tarifa_frecuente = tarifa_frecuente;
        this.tarifa_adulto_mayor = tarifa_adulto_mayor;
        this.tarifa_estudiante = tarifa_estudiante;
        this.tarifa_vale_muni = tarifa_vale_muni;
    }

    public String getRemoto() {
        return remoto;
    }

    public void setRemoto(String remoto) {
        this.remoto = remoto;
    }

    public int getInicio() {
        return inicio;
    }

    public void setInicio(int inicio) {
        this.inicio = inicio;
    }

    public int getTermina() {
        return termina;
    }

    public void setTermina(int termina) {
        this.termina = termina;
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

    public double getTarifa_vale_muni() {
        return tarifa_vale_muni;
    }

    public void setTarifa_vale_muni(double tarifa_vale_muni) {
        this.tarifa_vale_muni = tarifa_vale_muni;
    }
}
