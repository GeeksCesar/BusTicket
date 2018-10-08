package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;

public class Vehiculo extends SugarRecord {

    int _id;
    int tipo_vehiculo;
    String placa;
    int col_derecha;
    int col_izquierda;
    int filas;
    int cant_pasajeros;

    public Vehiculo() {
    }

    public Vehiculo(int id, int tipo_vehiculo, String placa, int col_derecha, int col_izquierda, int filas, int cant_pasajeros) {
        this._id = id;
        this.tipo_vehiculo = tipo_vehiculo;
        this.placa = placa;
        this.col_derecha = col_derecha;
        this.col_izquierda = col_izquierda;
        this.filas = filas;
        this.cant_pasajeros = cant_pasajeros;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getTipo_vehiculo() {
        return tipo_vehiculo;
    }

    public void setTipo_vehiculo(int tipo_vehiculo) {
        this.tipo_vehiculo = tipo_vehiculo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public int getCol_derecha() {
        return col_derecha;
    }

    public void setCol_derecha(int col_derecha) {
        this.col_derecha = col_derecha;
    }

    public int getCol_izquierda() {
        return col_izquierda;
    }

    public void setCol_izquierda(int col_izquierda) {
        this.col_izquierda = col_izquierda;
    }

    public int getFilas() {
        return filas;
    }

    public void setFilas(int filas) {
        this.filas = filas;
    }

    public int getCant_pasajeros() {
        return cant_pasajeros;
    }

    public void setCant_pasajeros(int cant_pasajeros) {
        this.cant_pasajeros = cant_pasajeros;
    }
}
