package com.smartgeeks.busticket.Modelo;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Table;

@Table
public class Silla extends SugarRecord {

    Long ticket;
    int numero_silla;

    public Silla() {
    }

    public Silla(Long ticket_id, int numero_silla) {
        this.ticket = ticket_id;
        this.numero_silla = numero_silla;
    }

    public Long getTicket_id() {
        return ticket;
    }

    public int getNumero_silla() {
        return numero_silla;
    }

    public void setTicket(Long ticket) {
        this.ticket = ticket;
    }

    public void setNumero_silla(int numero_silla) {
        this.numero_silla = numero_silla;
    }
}
