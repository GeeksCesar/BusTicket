package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;

@Table
public class Horario extends SugarRecord {

    /**
     * remoto => Indica el id remoto
     */
    @Unique
    String remoto;
    int ruta;
    String hora;

    public Horario() {
    }

    public Horario(String remoto, int ruta, String hora) {
        this.remoto = remoto;
        this.ruta = ruta;
        this.hora = hora;
    }

    public String getIdRemoto() {
        return remoto;
    }

    public int getIdRuta() {
        return ruta;
    }

    public String getHora() {
        return hora;
    }
}
