package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

public class TarifaParadero extends SugarRecord {

    @Unique
    String remoto;
    int paradaInicio;
    int paradaFin;
    double normal;
    double frecuente;
    double adultoMayor;
    double estudiante;
    double valeMuni;

    public TarifaParadero() {
    }

    public TarifaParadero(String remoto, int paradaInicio, int paradaFin, double normal, double frecuente, double adultoMayor, double estudiante, double valeMuni) {
        this.remoto = remoto;
        this.paradaInicio = paradaInicio;
        this.paradaFin = paradaFin;
        this.normal = normal;
        this.frecuente = frecuente;
        this.adultoMayor = adultoMayor;
        this.estudiante = estudiante;
        this.valeMuni = valeMuni;
    }

    public String getIdRemoto() {
        return remoto;
    }

    public int getParada_inicio() {
        return paradaInicio;
    }

    public int getParada_fin() {
        return paradaFin;
    }

    public double getNormal() {
        return normal;
    }

    public double getFrecuente() {
        return frecuente;
    }

    public double getAdulto_mayor() {
        return adultoMayor;
    }

    public double getEstudiante() {
        return estudiante;
    }

    public double getVale_muni() {
        return valeMuni;
    }
}
