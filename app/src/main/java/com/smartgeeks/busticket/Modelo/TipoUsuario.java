package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;

@Table
public class TipoUsuario extends SugarRecord {

    /**
     * remoto => Indica el id remoto
     */
    @Unique
    String remoto;
    String nombre;

    public TipoUsuario() {
    }

    public TipoUsuario(String id_remoto, String nombre) {
        this.remoto = id_remoto;
        this.nombre = nombre;
    }

    public String getId_remoto() {
        return remoto;
    }

    public String getNombre() {
        return nombre;
    }
}
