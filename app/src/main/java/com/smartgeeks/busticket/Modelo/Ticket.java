package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;
import com.orm.dsl.Table;

@Table
public class Ticket extends SugarRecord {

    String remoto;
    int paradaInicio;
    int paradaDestino;
    int idRutaDisponible;
    int idOperador;
    String horaSalida;
    int tipoUsuario;
    String fecha;
    String hora;
    int cantPasajes;
    double totalPagar;
    int idVehiculo = 0;
    int estado;
    int pendiente;

    public Ticket() {
    }

    public Ticket(String remoto, int paradaInicio, int paradaDestino, int idRutaDisponible, int idOperador, String horaSalida, int tipoUsuario, String fecha, String hora, int cantPasajes, double totalPagar, int idVehiculo, int estado, int pendiente) {
        this.remoto = remoto;
        this.paradaInicio = paradaInicio;
        this.paradaDestino = paradaDestino;
        this.idRutaDisponible = idRutaDisponible;
        this.idOperador = idOperador;
        this.horaSalida = horaSalida;
        this.tipoUsuario = tipoUsuario;
        this.fecha = fecha;
        this.hora = hora;
        this.cantPasajes = cantPasajes;
        this.totalPagar = totalPagar;
        this.idVehiculo = idVehiculo;
        this.estado = estado;
        this.pendiente = pendiente;
    }

    public String getIdRemoto() {
        return remoto;
    }

    public void setIdRemoto(String remoto) {
        this.remoto = remoto;
    }

    public int getParadaInicio() {
        return paradaInicio;
    }

    public void setParadaInicio(int paradaInicio) {
        this.paradaInicio = paradaInicio;
    }

    public int getParadaDestino() {
        return paradaDestino;
    }

    public void setParadaDestino(int paradaDestino) {
        this.paradaDestino = paradaDestino;
    }

    public int getIdRutaDisponible() {
        return idRutaDisponible;
    }

    public void setIdRutaDisponible(int idRutaDisponible) {
        this.idRutaDisponible = idRutaDisponible;
    }

    public int getIdOperador() {
        return idOperador;
    }

    public void setIdOperador(int idOperador) {
        this.idOperador = idOperador;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }

    public int getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(int tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
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

    public String getRemoto() {
        return remoto;
    }

    public void setRemoto(String remoto) {
        this.remoto = remoto;
    }

    public int getCantPasajes() {
        return cantPasajes;
    }

    public void setCantPasajes(int cantPasajes) {
        this.cantPasajes = cantPasajes;
    }

    public double getTotalPagar() {
        return totalPagar;
    }

    public void setTotalPagar(double totalPagar) {
        this.totalPagar = totalPagar;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public int getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(int idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public int getPendiente() {
        return pendiente;
    }

    public void setPendiente(int pendiente) {
        this.pendiente = pendiente;
    }
}
