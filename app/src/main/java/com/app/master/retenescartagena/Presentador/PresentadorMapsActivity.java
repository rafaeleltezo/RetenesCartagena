package com.app.master.retenescartagena.Presentador;

import android.content.Context;
import android.os.AsyncTask;

import com.app.master.retenescartagena.Vista.iMapsActivity;

/**
 * Created by Rafael p on 22/9/2017.
 */

public class PresentadorMapsActivity implements iPresentadorMapsActivity {

    private Context context;
    private iMapsActivity actividad;

    public PresentadorMapsActivity(Context context,iMapsActivity actividad){
        this.actividad=actividad;
        this.context=context;
        actividad.inicializarApiGps();
    }


    @Override
    public void inicializarMapa() {
        actividad.agregarLimitesMapa();
    }

    @Override
    public void inicializarActualizacionLocalizacion() {
        TareaActivarLocalizacion tarea=new TareaActivarLocalizacion();
        tarea.execute();
    }

    private class TareaActivarLocalizacion extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            actividad.enableLocationUpdates();
            return null;
        }
    }

}
