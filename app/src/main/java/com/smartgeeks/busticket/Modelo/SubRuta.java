package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;

@Table
public class SubRuta extends SugarRecord {

    /**
     * remoto => Indica el id remoto
     */
    @Unique
    String remoto;
    int ruta_id;
    String parada;
    double tarifa_normal;
    double tarifa_frecuente;
    double tarifa_adulto_mayor;
    double tarifa_estudiante;

    public SubRuta() {
    }

    public SubRuta(String id_remoto, int ruta_id, String parada, double tarifa_normal, double tarifa_frecuente, double tarifa_adulto_mayor, double tarifa_estudiante) {
        this.remoto = id_remoto;
        this.ruta_id = ruta_id;
        this.parada = parada;
        this.tarifa_normal = tarifa_normal;
        this.tarifa_frecuente = tarifa_frecuente;
        this.tarifa_adulto_mayor = tarifa_adulto_mayor;
        this.tarifa_estudiante = tarifa_estudiante;
    }

    public String getId_remoto() {
        return remoto;
    }

    public int getRuta_id() {
        return ruta_id;
    }

    public String getParada() {
        return parada;
    }

    public double getTarifa_normal() {
        return tarifa_normal;
    }

    public double getTarifa_frecuente() {
        return tarifa_frecuente;
    }

    public double getTarifa_adulto_mayor() {
        return tarifa_adulto_mayor;
    }

    public double getTarifa_estudiante() {
        return tarifa_estudiante;
    }
}
