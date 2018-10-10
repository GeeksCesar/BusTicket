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
    int tipo_vehiculo;
    String placa;
    int col_derecha;
    int col_izquierda;
    int filas;
    int cant_pasajeros;

    public Vehiculo() {
    }

    public Vehiculo(String id_remoto, int tipo_vehiculo, String placa, int col_derecha, int col_izquierda, int filas, int cant_pasajeros) {
        this.remoto = id_remoto;
        this.tipo_vehiculo = tipo_vehiculo;
        this.placa = placa;
        this.col_derecha = col_derecha;
        this.col_izquierda = col_izquierda;
        this.filas = filas;
        this.cant_pasajeros = cant_pasajeros;
    }

    public String getId_remoto() {
        return remoto;
    }

    public int getTipo_vehiculo() {
        return tipo_vehiculo;
    }

    public String getPlaca() {
        return placa;
    }

    public int getCol_derecha() {
        return col_derecha;
    }

    public int getCol_izquierda() {
        return col_izquierda;
    }

    public int getFilas() {
        return filas;
    }

    public int getCant_pasajeros() {
        return cant_pasajeros;
    }
}
