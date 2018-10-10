package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;

public class Vehiculo extends SugarRecord {

    int id_remoto;
    int tipo_vehiculo;
    String placa;
    int col_derecha;
    int col_izquierda;
    int filas;
    int cant_pasajeros;

    public Vehiculo() {
    }

    public Vehiculo(int id_remoto, int tipo_vehiculo, String placa, int col_derecha, int col_izquierda, int filas, int cant_pasajeros) {
        this.id_remoto = id_remoto;
        this.tipo_vehiculo = tipo_vehiculo;
        this.placa = placa;
        this.col_derecha = col_derecha;
        this.col_izquierda = col_izquierda;
        this.filas = filas;
        this.cant_pasajeros = cant_pasajeros;
    }

    public int getId_remoto() {
        return id_remoto;
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
