package com.app.master.retenescartagena.Modelo;

/**
 * Created by Rafael p on 23/9/2017.
 */

public class Coordenadas {
    private double latitud,longitud;
    private String hora;



    public Coordenadas() {
    }

    public Coordenadas(double latitud, double longitud,String hora) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.hora=hora;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }
    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
}
