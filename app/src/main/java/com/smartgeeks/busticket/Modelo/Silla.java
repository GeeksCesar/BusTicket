package com.smartgeeks.busticket.Modelo;

public class Silla {

    int ticket_id;
    int numero_silla;

    public Silla() {
    }

    public Silla(int ticket_id, int numero_silla) {
        this.ticket_id = ticket_id;
        this.numero_silla = numero_silla;
    }

    public int getTicket_id() {
        return ticket_id;
    }

    public int getNumero_silla() {
        return numero_silla;
    }
}
