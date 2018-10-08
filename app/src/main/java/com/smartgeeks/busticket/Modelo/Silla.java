package com.smartgeeks.busticket.Modelo;

public class Silla {

    int _id;
    int ticket_id;
    int numero_silla;

    public Silla() {
    }

    public Silla(int id, int ticket_id, int numero_silla) {
        this._id = id;
        this.ticket_id = ticket_id;
        this.numero_silla = numero_silla;
    }
}
