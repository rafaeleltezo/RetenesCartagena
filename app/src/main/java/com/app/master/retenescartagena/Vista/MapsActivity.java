package com.app.master.retenescartagena.Vista;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.app.master.retenescartagena.Modelo.Coordenadas;
import com.app.master.retenescartagena.Modelo.Usuario;
import com.app.master.retenescartagena.Presentador.PresentadorMapsActivity;
import com.app.master.retenescartagena.Presentador.iPresentadorMapsActivity;
import com.app.master.retenescartagena.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;

public class MapsActivity extends FragmentActivity implements iMapsActivity, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener, View.OnClickListener {


    private static final int PETICION_PERMISO_LOCALIZACION = 1;
    private GoogleMap mMap;
    private static final int PETICION_CONFIG_UBICACION = 21;
    private static GoogleApiClient apiClient;
    //private Activity actividad;
    private LocationRequest locRequest;
    private static Location location;
    private Button btnReportarReten;
    private iPresentadorMapsActivity presentador;
    private Location localizacion;
    private FirebaseDatabase database;
    private InterstitialAd mInterstitialAd;
    private CountDownTimer countDownTimer;
    private CountDownTimer cuentaRegresiva;
    private DatabaseReference dato;
    private DatabaseReference referenciaPuntoControl;
    private ArrayList<Coordenadas> coordenadas;
    private ProgressDialog progreso;
    private ArrayList<Coordenadas> coordenadaMisRetnees;
    private AdView adview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btnReportarReten=(Button) findViewById(R.id.btnReportarReten);
        progreso=new ProgressDialog(this);
        presentador=new PresentadorMapsActivity(this,this);
        coordenadaMisRetnees=new ArrayList();
        coordenadas=new ArrayList();
        btnReportarReten.setOnClickListener(this);
        database=FirebaseDatabase.getInstance();
        mInterstitialAd=new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-5246970221791662/6443547402");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        //presentador.inicializarContadorPublicidad();
        referenciaPuntoControl=database.getReference("Puntos de control");
        dato=referenciaPuntoControl.child(FirebaseInstanceId.getInstance().getToken());
        presentador.TareaTokenFirebase();
        presentador.TareaMisRetenes();
        contadorLlenarMapa();
        adview = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);

    }
    public ArrayList<Coordenadas> retenesCartagena(){
        ArrayList<Coordenadas> coor=new ArrayList();

        for (Coordenadas c:obtenerCoordenadasReten()) {
            int contador=0;
                    for (Coordenadas cs:obtenerCoordenadasReten()){
                        if(c.getLatitud()==cs.getLatitud() && c.getLongitud()==cs.getLongitud()){
                          contador++;
                        }
                        if(contador>=2){
                            coor.add(c);
                        }
                    }
            }

        HashSet<Coordenadas> hashSet = new HashSet<Coordenadas>(coor);
        coor.clear();
        coor.addAll(hashSet);
        contadorLlenarMapa();
            return coor;
    }

    @Override
    public void TareaMisRetenes(){
        TareaMisPuntoControl tarea= new TareaMisPuntoControl();
        tarea.execute();
    }

    public ArrayList<Coordenadas> obtenerCoordenadasReten(){
        return coordenadas;

    }

    public void TareaTokenFirebase(){
        Tarea t=new Tarea();
        t.execute();
    }

    public ArrayList<Coordenadas> getCoordenadaMisRetnees() {
        return coordenadaMisRetnees;
    }


    private void AgregarFechaFirebase(DatabaseReference referencia){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        HashMap<String, Object> result = new HashMap<>();
        result.put("fecha",new Usuario(formattedDate));
        dato.updateChildren(result);
        //dato.setValue(new Usuario(formattedDate));
    }

    public void contadorLlenarMapa(){
        cuentaRegresiva=new CountDownTimer(9000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Toast.makeText(MapsActivity.this, String.valueOf(millisUntilFinished/1000), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                mMap.clear();
                for (Coordenadas c:retenesCartagena()) {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(c.getLatitud(), c.getLongitud())).title("Reten" ).icon(BitmapDescriptorFactory.fromResource(R.drawable.reten)));
                    //Toast.makeText(MapsActivity.this, String.valueOf(c.getLatitud()), Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }

    @Override
    public void contadorPublicidad(){
       countDownTimer=new CountDownTimer(9000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Toast.makeText(MapsActivity.this,String.valueOf(millisUntilFinished/1000), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {

                publicidad();
            }
        }.start();
    }
    public void publicidad(){
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("Mensaje", "No esta cargando la publicidad");
        }
        presentador.inicializarContadorPublicidad();
    }

    public Location getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(Location localizacion) {
        this.localizacion = localizacion;
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
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
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

            ActivityCompat.requestPermissions(this,
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

    private void actualizarUbicacion(Location loc){
        if (loc != null) {
            setLocalizacion(loc);
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
        locRequest.setFastestInterval(60000);
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

    public Boolean CompararcoordenadaReten(){
        for (Coordenadas coordena:getCoordenadaMisRetnees()) {
            DecimalFormat df = new DecimalFormat("#.000");
            String latitudActual=df.format(getLocalizacion().getLatitude());
            String longitudActual=df.format(getLocalizacion().getLongitude());
            String latitudFirebase=df.format(coordena.getLatitud());
            String longitudFirebase=df.format(coordena.getLongitud());

            if(latitudActual.equals(latitudFirebase)&&longitudActual.equals(longitudFirebase)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==btnReportarReten.getId()){
            try {
                if(CompararcoordenadaReten()){
                    Toast.makeText(this, "Reten ya fue registrado", Toast.LENGTH_SHORT).show();
                }else {
                    presentador.ingresarPuntoControl();

                }
            }catch (Exception e){

            }


            }

    }

    @Override
    public void ingrearPuntocontrol(){


        if(FirebaseInstanceId.getInstance().getToken()!=null && getLocalizacion()!=null) {
            Calendar calendario = new GregorianCalendar();
            int hora, minutos, segundos;
            hora =calendario.get(Calendar.HOUR_OF_DAY);
            minutos = calendario.get(Calendar.MINUTE);
            segundos = calendario.get(Calendar.SECOND);
            String horas=String.valueOf(hora)+":"+String.valueOf(minutos)+":"+String.valueOf(segundos);
            double latitud=Math.round(getLocalizacion().getLatitude() * 1000d) / 1000d;
            double longitud=Math.round(getLocalizacion().getLongitude() * 1000d) / 1000d;
            dato.push().setValue(new Coordenadas(latitud,longitud,horas));
            //referencia=database.getReference(FirebaseInstanceId.getInstance().getToken());
            //referencia.push().setValue(new Coordenadas(getLocalizacion().getLatitude(),getLocalizacion().getLongitude(),"rafa"));
            Toast.makeText(this, "Reten registrado satisfactoriamente", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "No se pudo registrar reten", Toast.LENGTH_SHORT).show();
        }


    }

    private class Tarea extends AsyncTask<Void,Void,Void>{



        @Override
        protected void onPreExecute() {
            progreso.setMessage("Cargando retenes");
            progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progreso.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            coordenadas.clear();
            referenciaPuntoControl.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot data:dataSnapshot.getChildren()) {
                        for (DataSnapshot dat:data.getChildren()) {
                            Coordenadas coordenas=dat.getValue(Coordenadas.class);

                            coordenadas.add(coordenas);

                            //Toast.makeText(MapsActivity.this,String.valueOf(coordenas.getLatitud()), Toast.LENGTH_SHORT).show();
                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            referenciaPuntoControl.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    AgregarFechaFirebase(referenciaPuntoControl);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progreso.dismiss();
        }
    }
    private class TareaMisPuntoControl extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            coordenadaMisRetnees.clear();
            DatabaseReference datosMisretenes=database.getReference("Puntos de control/"+FirebaseInstanceId.getInstance().getToken());
            datosMisretenes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot s:dataSnapshot.getChildren()) {
                        Coordenadas coordena=s.getValue(Coordenadas.class);
                        //Toast.makeText(MapsActivity.this, "Hola", Toast.LENGTH_SHORT).show();

                        coordenadaMisRetnees.add(coordena);
                        //Toast.makeText(MapsActivity.this,String.valueOf(coordena.getLatitud()), Toast.LENGTH_SHORT).show();
                        /*
                        DecimalFormat df = new DecimalFormat("#.000");
                        String latitudActual=df.format(getLocalizacion().getLatitude());
                        String longitudActual=df.format(getLocalizacion().getLongitude());
                        String latitudFirebase=df.format(coordena.getLatitud());
                        String longitudFirebase=df.format(coordena.getLongitud());
                        if(latitudActual.equals(latitudFirebase)&&longitudActual.equals(longitudFirebase)){

                            verdad[0] =true;
                            break;
                        }*/

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MapsActivity.this, "Error al conectar al servidor", Toast.LENGTH_SHORT).show();
                }
            });

            return null;
        }

    }
}
