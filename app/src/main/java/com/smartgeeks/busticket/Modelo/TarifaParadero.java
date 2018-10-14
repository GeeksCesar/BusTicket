package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

public class TarifaParadero extends SugarRecord {

    @Unique
    String remoto;
    int parada_inicio;
    int parada_fin;
    double normal;
    double frecuente;
    double adulto_mayor;
    double estudiante;
    double vale_muni;

    public TarifaParadero() {
    }

    public TarifaParadero(String remoto, int parada_inicio, int parada_fin, double normal, double frecuente, double adulto_mayor, double estudiante, double vale_muni) {
        this.remoto = remoto;
        this.parada_inicio = parada_inicio;
        this.parada_fin = parada_fin;
        this.normal = normal;
        this.frecuente = frecuente;
        this.adulto_mayor = adulto_mayor;
        this.estudiante = estudiante;
        this.vale_muni = vale_muni;
    }

    public String getIdRemoto() {
        return remoto;
    }

    public int getParada_inicio() {
        return parada_inicio;
    }

    public int getParada_fin() {
        return parada_fin;
    }

    public double getNormal() {
        return normal;
    }

    public double getFrecuente() {
        return frecuente;
    }

    public double getAdulto_mayor() {
        return adulto_mayor;
    }

    public double getEstudiante() {
        return estudiante;
    }

    public double getVale_muni() {
        return vale_muni;
    }
}
