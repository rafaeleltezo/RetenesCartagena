package com.app.master.retenescartagena.Vista;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.app.master.retenescartagena.Modelo.Usuario;
import com.app.master.retenescartagena.Presentador.PresentadorMapsActivity;
import com.app.master.retenescartagena.Presentador.iPresentadorMapsActivity;
import com.app.master.retenescartagena.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class MapsActivity extends FragmentActivity implements iMapsActivity, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, View.OnClickListener {


    private static final int PETICION_PERMISO_LOCALIZACION = 1;
    private GoogleMap mMap;
    private static final int PETICION_CONFIG_UBICACION = 21;
    private static GoogleApiClient apiClient;
    private Activity actividad;
    private LocationRequest locRequest;
    private static Location location;
    private Button btnReportarReten;
    private iPresentadorMapsActivity presentador;
    private Location localizacion;
    private FirebaseDatabase database;
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btnReportarReten=(Button) findViewById(R.id.btnReportarReten);
        presentador=new PresentadorMapsActivity(this,this);
        btnReportarReten.setOnClickListener(this);
        database=FirebaseDatabase.getInstance();
        mInterstitialAd=new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5246970221791662/6443547402");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        //tareaPublicidad tarea=new tareaPublicidad();
        //tarea.execute();

    }

    public Location getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(Location localizacion) {
        this.localizacion = localizacion;
    }

    public void ejecutarTarea(){
        tareaPublicidad tarea=new tareaPublicidad();
        tarea.execute();
    }

    public void pausarHilo(){
        try{
            Thread.sleep(120000);
        }catch (Exception e){

        }
    }

    //Localizacion
    @Override
    public void inicializarApiGps() {
        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraPosition cameraPosition;
        cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(10.4027901, -75.5146382))
                .zoom(14)
                .bearing(0)
                .tilt(0)
                .build();
        CameraUpdate camara = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.animateCamera(camara);

        mMap.setMinZoomPreference(14f);
        mMap.setMaxZoomPreference(18f);
        presentador.inicializarMapa();
        presentador.inicializarActualizacionLocalizacion();
    }

    @Override
    public void agregarLimitesMapa() {
        LatLngBounds Cartagena = new LatLngBounds(
                //10.4027901, -75.5156382
                new LatLng(10.3027, -75.6156), new LatLng(10.6627, -75.4556));

// Constrain the camera target to the Adelaide bounds.
        mMap.setLatLngBoundsForCameraTarget(Cartagena);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
        }catch (Exception e){

        }

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Conexion Fallida a servicios Google Play", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(actividad,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PETICION_PERMISO_LOCALIZACION);
        } else {

            Location lastLocation =
                    LocationServices.FusedLocationApi.getLastLocation(apiClient);

            actualizarUbicacion(lastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Conexion Suspendida", Toast.LENGTH_SHORT).show();
    }

    public void actualizarUbicacion(Location loc){
        if (loc != null) {
            setLocalizacion(location);
            CameraPosition cameraPosition;
            cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .build();
            CameraUpdate camara = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mMap.animateCamera(camara);


        } else {
            Toast.makeText(this, "latitud desconocida", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "longitud desconocida", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void enableLocationUpdates() {

        locRequest = new LocationRequest();
        locRequest.setInterval(60000);
        locRequest.setFastestInterval(30000);
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest locSettingsRequest =
                new LocationSettingsRequest.Builder()
                        .addLocationRequest(locRequest)
                        .build();


        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        apiClient, locSettingsRequest);

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:

                        //Toast.makeText(MapsActivity.this, "Configuracion correcta", Toast.LENGTH_SHORT).show();
                        startLocationUpdates();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            Toast.makeText(MapsActivity.this, "Active GPS para ubicar paraderos cercanos", Toast.LENGTH_SHORT).show();
                            status.startResolutionForResult(MapsActivity.this, PETICION_CONFIG_UBICACION);
                        } catch (IntentSender.SendIntentException e) {
                            Toast.makeText(MapsActivity.this, "Error al intentar solucionar configuración de ubicación", Toast.LENGTH_SHORT).show();

                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(MapsActivity.this, "No se puede cumplir la configuración de ubicación necesaria", Toast.LENGTH_SHORT).show();


                        break;
                }
            }
        });
    }
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //Ojo: estamos suponiendo que ya tenemos concedido el permiso.
            //Sería recomendable implementar la posible petición en caso de no tenerlo.
            //Toast.makeText(this, "Inicio de recepción de ubicaciones", Toast.LENGTH_SHORT).show();

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    apiClient, locRequest, this);
            mMap.setMyLocationEnabled(true);

        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //mMap.setMyLocationEnabled(true);
        actualizarUbicacion(location);

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==btnReportarReten.getId()){
            ingrearPuntocontrol();
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {
                Log.d("Mensaje", "No esta cargando la publicidad");
            }
        }
    }

    public void ingrearPuntocontrol(){
        DatabaseReference referencia=database.getReference("Puntos de control");
        if(FirebaseInstanceId.getInstance().getToken()!=null && getLocalizacion()!=null) {
            referencia.child(FirebaseInstanceId.getInstance().getToken())
                    .setValue(new Usuario("12/12/12", getLocalizacion().getLatitude(), getLocalizacion().getLongitude()));
        }else {
            Toast.makeText(this, "No se puede reportar punto de control", Toast.LENGTH_SHORT).show();
        }


    }
    private class tareaPublicidad extends AsyncTask<Void,Integer,Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            for (int i = 1; i <=4 ; i++) {
                pausarHilo();

            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            try {
                ejecutarTarea();
            }catch (Exception e){

            }


        }
    }
}
