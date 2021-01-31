package com.smartgeeks.busticket.Modelo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.smartgeeks.busticket.Objects.Tarifa;

import java.util.List;

public class TarifaUsuario {

    @SerializedName("count") @Expose private Integer count;
    @SerializedName("tarifas") @Expose private List<Tarifa> tarifas = null;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<Tarifa> getTarifas() {
        return tarifas;
    }

    public void setTarifas(List<Tarifa> tarifas) {
        this.tarifas = tarifas;
    }
}
