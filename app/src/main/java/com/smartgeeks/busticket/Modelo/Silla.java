package com.smartgeeks.busticket.Modelo;

<<<<<<< HEAD
import com.orm.SugarRecord;
import com.orm.dsl.Table;
=======
public class Silla {

    int numeroSilla;
    int origen;
    int destino;
>>>>>>> 2828d9f2c552bcca6b06d2efab6a4a9b6393fa0b

    public Silla() {
    }

    public Silla(int numeroSilla, int origen, int destino) {
        this.numeroSilla = numeroSilla;
        this.origen = origen;
        this.destino = destino;
    }

    public int getNumeroSilla() {
        return numeroSilla;
    }

    public void setNumeroSilla(int numeroSilla) {
        this.numeroSilla = numeroSilla;
    }

    public int getOrigen() {
        return origen;
    }

    public void setOrigen(int origen) {
        this.origen = origen;
    }

    public int getDestino() {
        return destino;
    }

    public void setDestino(int destino) {
        this.destino = destino;
    }
}
