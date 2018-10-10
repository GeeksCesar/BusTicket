package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;

public class Ruta extends SugarRecord {

    int id_remoto;
    String partida;
    String destino;

    public Ruta() {
    }

    public Ruta(int id_remoto, String partida, String destino) {
        this.id_remoto = id_remoto;
        this.partida = partida;
        this.destino = destino;
    }

    public int getId_remoto() {
        return id_remoto;
    }

    public String getPartida() {
        return partida;
    }

    public String getDestino() {
        return destino;
    }
}
