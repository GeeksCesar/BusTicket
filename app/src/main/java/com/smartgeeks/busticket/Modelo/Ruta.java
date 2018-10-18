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

    public Ruta() {
    }

    public Ruta(String remoto, String partida, String destino, int vehiculo, int rutaDisponible) {
        this.remoto = remoto;
        this.partida = partida;
        this.destino = destino;
        this.vehiculo = vehiculo;
        this.rutaDisponible = rutaDisponible;
    }

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
}
