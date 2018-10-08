package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;

public class Ticket extends SugarRecord {

    int _id;
    String cliente;
    int horario_id;
    int sub_partida;
    int sub_destino;
    int tipo_usuario;
    int cant_sillas;
    String fecha;
    String hora;
    double precio;
    int estado;

    public Ticket() {
    }

    public Ticket(int id, String cliente, int horario_id, int sub_partida, int sub_destino, int tipo_usuario, int cant_sillas, String fecha, String hora, double precio, int estado) {
        this._id = id;
        this.cliente = cliente;
        this.horario_id = horario_id;
        this.sub_partida = sub_partida;
        this.sub_destino = sub_destino;
        this.tipo_usuario = tipo_usuario;
        this.cant_sillas = cant_sillas;
        this.fecha = fecha;
        this.hora = hora;
        this.precio = precio;
        this.estado = estado;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public int getHorario_id() {
        return horario_id;
    }

    public void setHorario_id(int horario_id) {
        this.horario_id = horario_id;
    }

    public int getSub_partida() {
        return sub_partida;
    }

    public void setSub_partida(int sub_partida) {
        this.sub_partida = sub_partida;
    }

    public int getSub_destino() {
        return sub_destino;
    }

    public void setSub_destino(int sub_destino) {
        this.sub_destino = sub_destino;
    }

    public int getTipo_usuario() {
        return tipo_usuario;
    }

    public void setTipo_usuario(int tipo_usuario) {
        this.tipo_usuario = tipo_usuario;
    }

    public int getCant_sillas() {
        return cant_sillas;
    }

    public void setCant_sillas(int cant_sillas) {
        this.cant_sillas = cant_sillas;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
}
