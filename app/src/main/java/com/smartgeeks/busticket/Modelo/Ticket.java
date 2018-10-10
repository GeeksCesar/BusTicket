package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;

public class Ticket extends SugarRecord {

    int id_remoto;
    String cliente;
    int horario_id;
    int sub_partida;
    int sub_destino;
    int tipo_usuario;
    int[] sillas;
    String fecha;
    String hora;
    double precio;
    int estado;
    int pendiente_insercion;

    public Ticket() {
    }

    public Ticket(int id_remoto, String cliente, int horario_id, int sub_partida, int sub_destino, int tipo_usuario, int[] sillas, String fecha, String hora, double precio, int estado, int pendiente_insercion) {
        this.id_remoto = id_remoto;
        this.cliente = cliente;
        this.horario_id = horario_id;
        this.sub_partida = sub_partida;
        this.sub_destino = sub_destino;
        this.tipo_usuario = tipo_usuario;
        this.sillas = sillas;
        this.fecha = fecha;
        this.hora = hora;
        this.precio = precio;
        this.estado = estado;
        this.pendiente_insercion = pendiente_insercion;
    }

    public int getId_remoto() {
        return id_remoto;
    }

    public String getCliente() {
        return cliente;
    }

    public int getHorario_id() {
        return horario_id;
    }

    public int getSub_partida() {
        return sub_partida;
    }

    public int getSub_destino() {
        return sub_destino;
    }

    public int getTipo_usuario() {
        return tipo_usuario;
    }

    public int[] getSillas() {
        return sillas;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public double getPrecio() {
        return precio;
    }

    public int getEstado() {
        return estado;
    }

    public int getPendiente_insercion() {
        return pendiente_insercion;
    }
}
