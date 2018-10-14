package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;

@Table
public class Vehiculo extends SugarRecord {

    /**
     * remoto => Indica el id remoto
     */
    @Unique
    String remoto;
    String placa;
    int num_asientos;
    int empresa;

    public Vehiculo() {
    }

    public Vehiculo(String remoto, String placa, int num_asientos, int empresa) {
        this.remoto = remoto;
        this.placa = placa;
        this.num_asientos = num_asientos;
        this.empresa = empresa;
    }

    public String getIdRemoto() {
        return remoto;
    }

    public String getPlaca() {
        return placa;
    }

    public int getNumAsientos() {
        return num_asientos;
    }

    public int getEmpresa() {
        return empresa;
    }
}
