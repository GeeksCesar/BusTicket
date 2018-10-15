package com.smartgeeks.busticket.Objcect;

public class RutaPojo {

    private int ruta_id;
    private int vehiculo_id;
    private int ruta_disponible_id;
    private int horario_id;
    private String horario;
    private String informacion;
    private boolean status_ruta;

    // Getter && Setter
    public int getRuta_id() {
        return ruta_id;
    }

    public void setRuta_id(int ruta_id) {
        this.ruta_id = ruta_id;
    }

    public int getVehiculo_id() {
        return vehiculo_id;
    }

    public void setVehiculo_id(int vehiculo_id) {
        this.vehiculo_id = vehiculo_id;
    }

    public int getRuta_disponible_id() {
        return ruta_disponible_id;
    }

    public void setRuta_disponible_id(int ruta_disponible_id) {
        this.ruta_disponible_id = ruta_disponible_id;
    }

    public int getHorario_id() {
        return horario_id;
    }

    public void setHorario_id(int horario_id) {
        this.horario_id = horario_id;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getInformacion() {
        return informacion;
    }

    public void setInformacion(String informacion) {
        this.informacion = informacion;
    }

    public boolean getStatus_ruta() {
        return status_ruta;
    }

    public void setStatus_ruta(boolean status_ruta) {
        this.status_ruta = status_ruta;
    }
}
