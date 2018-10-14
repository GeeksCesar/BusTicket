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

    public Vehiculo() {
    }

    public Vehiculo(String id_remoto, int tipo_vehiculo, String placa, int col_derecha, int col_izquierda, int filas, int num_asientos) {
        this.remoto = id_remoto;
        this.placa = placa;
        this.num_asientos = num_asientos;
    }

    public String getId_remoto() {
        return remoto;
    }

    public String getPlaca() {
        return placa;
    }

    public int getCant_pasajeros() {
        return num_asientos;
    }
}
