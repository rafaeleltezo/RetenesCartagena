package com.app.master.retenescartagena.Modelo;

/**
 * Created by Rafael p on 22/9/2017.
 */

public class Usuario {
    private String fecha;
    private double latitud,longitud;

    public Usuario( String fecha, double latitud, double longitud) {

        this.fecha = fecha;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Usuario() {
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
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
