package com.smartgeeks.busticket.Objcect;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Tarifa {
    @SerializedName("color") @Expose private String color;
    @SerializedName("id") @Expose private String id;
    @SerializedName("module") @Expose private Integer module;
    @SerializedName("nombre_tarifa") @Expose private String nombreTarifa;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getModule() {
        return module;
    }

    public void setModule(Integer module) {
        this.module = module;
    }

    public String getNombreTarifa() {
        return nombreTarifa;
    }

    public void setNombreTarifa(String nombreTarifa) {
        this.nombreTarifa = nombreTarifa;
    }

}
