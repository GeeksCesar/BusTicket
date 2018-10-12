package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;

@Table
public class Ticket extends SugarRecord {

    /**
     * remoto => Indica el id remoto
     */
    @Unique
    String remoto;
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
    int pendiente;

    public Ticket() {
    }

    public Ticket(String id_remoto, String cliente, int horario_id, int sub_partida, int sub_destino, int tipo_usuario, int sillas, String fecha, String hora, double precio, int estado, int pendiente_insercion) {
        this.remoto = id_remoto;
        this.cliente = cliente;
        this.horario_id = horario_id;
        this.sub_partida = sub_partida;
        this.sub_destino = sub_destino;
        this.tipo_usuario = tipo_usuario;
        this.cant_sillas = sillas;
        this.fecha = fecha;
        this.hora = hora;
        this.precio = precio;
        this.estado = estado;
        this.pendiente = pendiente_insercion;
    }

    public String getId_remoto() {
        return remoto;
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

    public int getSillas() {
        return cant_sillas;
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
        return pendiente;
    }

    public void setId_remoto(String remoto) {
        this.remoto = remoto;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public void setHorario_id(int horario_id) {
        this.horario_id = horario_id;
    }

    public void setSub_partida(int sub_partida) {
        this.sub_partida = sub_partida;
    }

    public void setSub_destino(int sub_destino) {
        this.sub_destino = sub_destino;
    }

    public void setTipo_usuario(int tipo_usuario) {
        this.tipo_usuario = tipo_usuario;
    }

    public void setSillas(int sillas) {
        this.cant_sillas = sillas;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public void setPendiente_insercion(int pendiente_insercion) {
        this.pendiente = pendiente_insercion;
    }
}
