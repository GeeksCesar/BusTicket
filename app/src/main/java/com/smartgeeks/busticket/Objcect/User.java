package com.smartgeeks.busticket.Objcect;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("idUsuario")
    @Expose
    private int idUsuario;

    @SerializedName("idEmpresa")
    @Expose
    private int id_empresa;

    @SerializedName("nombre")
    @Expose
    private String nombre;

    @SerializedName("idRol")
    @Expose
    private int idRol;

    @SerializedName("rut")
    @Expose
    private String rut;

    //GETTER && SETTER
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getId_empresa() {
        return id_empresa;
    }

    public void setId_empresa(int id_empresa) {
        this.id_empresa = id_empresa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }
}
