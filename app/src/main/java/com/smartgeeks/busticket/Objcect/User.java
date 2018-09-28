package com.smartgeeks.busticket.Objcect;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("idUsuario")
    @Expose
    private int idUsuario;

    @SerializedName("docu")
    @Expose
    private String docu;

    @SerializedName("nombre")
    @Expose
    private String nombre;

    @SerializedName("idRol")
    @Expose
    private int idRol;

    //GETTER && SETTER
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDocu() {
        return docu;
    }

    public void setDocu(String docu) {
        this.docu = docu;
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
}
