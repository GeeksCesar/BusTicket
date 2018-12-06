package com.smartgeeks.busticket.Modelo;

import com.orm.SugarRecord;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;

@Table
public class Ruta extends SugarRecord {

    /**
     * remoto => Indica el id remoto
     */
    @Unique
    String remoto;
    String partida;
    String destino;
    int vehiculo;
    int rutaDisponible;
    int ruta;

    public Ruta() {
    }

    public Ruta(String remoto, String partida, String destino, int vehiculo, int rutaDisponible, int ruta) {
        this.remoto = remoto;
        this.partida = partida;
        this.destino = destino;
        this.vehiculo = vehiculo;
        this.rutaDisponible = rutaDisponible;
        this.ruta = ruta;
    }

    /**
     * El idRemoto, corresponde al id de ruta disponible
     * @return remoto
     */
    public String getIdRemoto() {
        return remoto;
    }

    public String getPartida() {
        return partida;
    }

    public String getDestino() {
        return destino;
    }

    public int getVehiculo() {
        return vehiculo;
    }

    public int getRutaDisponible() {
        return rutaDisponible;
    }

    public int getRuta() {
        return ruta;
    }
}
