package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;

public class Ruta extends SugarRecord {

    int _id;
    String partida;
    String destino;

    public Ruta() {
    }

    public long get_id() {
        return _id;
    }

    public Ruta(int id, String partida, String destino) {
        this._id = id;
        this.partida = partida;
        this.destino = destino;
    }

    public String getPartida() {
        return partida;
    }

    public void setPartida(String partida) {
        this.partida = partida;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }
}
