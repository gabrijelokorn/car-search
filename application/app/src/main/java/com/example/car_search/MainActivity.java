package com.example.car_search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.Manifest;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int unlockedVALUE = 1000;

    FusedLocationProviderClient locationProviderClient;
    int PERMISSION_ID = 44;
    private Locationer locationer;
    private TextView locationTextView;
    private Button lock_unlock;

    private boolean car_locked;
    private float car_lon;
    private float car_lat;
    private float dev_lon;
    private float dev_lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        get_car_info();
        lock_unlock = (Button) findViewById(R.id.lock_unlock);
        lock_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                change_car_state();
            }
        });
        update_car_info();

        locationTextView = findViewById(R.id.locationTextView);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        prepare_application();
    }

    @Override
    public void onResume() {
        super.onResume();
        prepare_application();
    }

    private void update_car_info() {
        if (this.car_locked) {
            this.lock_unlock.setText("ODKLENI");
        } else {
            this.lock_unlock.setText("ZAKLENI");
        }
    }
    private void unlock_the_car() {
        SharedPreferences sp = getSharedPreferences("CarINFO", MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();

        spe.putBoolean("locked", false);
        spe.putFloat("lon", unlockedVALUE);
        spe.putFloat("lat", unlockedVALUE);

        get_car_info();
    }

    private void lock_the_car() {
        SharedPreferences sp = getSharedPreferences("CarINFO", MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();

        spe.putBoolean("locked", true);
        spe.putFloat("lon", this.dev_lon);
        spe.putFloat("lat", this.dev_lat);

        get_car_info();
    }

    private void get_car_info() {
        SharedPreferences sp = getSharedPreferences("CarINFO", MODE_PRIVATE);
        this.car_locked = sp.getBoolean("locked", false);
        this.car_lon = sp.getFloat("lon", unlockedVALUE);
        this.car_lat = sp.getFloat("lat", unlockedVALUE);
    }

    private void change_car_state() {
        if (this.car_locked) {
            unlock_the_car();
        } else {
            lock_the_car();
        }

        update_car_info();
        Log.d("SharedPreferencesTest", "Is car locked? " + this.car_locked);
    }

    /* "start_location_updates" metoda sluzi kot zagon branja podatkov o lokaciji */
    private void start_location_updates() {
        locationer = new Locationer(this);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationer);
    }
    private void prepare_application () {
        if (!check_location_permission()) {
            request_location_permission();
        }
        if (!check_location_access()) {
            request_location_access();
        }

        if (check_location_access() && check_location_permission()) {
            start_location_updates();
        }
    }

    public void updateLocationText(double lon, double lat) {
        String locationText = "Lon: " + lon + "\nLat: " + lat;
        locationTextView.setText(locationText);
    }

    /* "check_location_permission" metoda je namenjena preverjanju dveh nacinov dovoljenja dostopa lokacije v napravi */
    private boolean check_location_permission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&  ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /* Metoda "request_location_permission" omogoca, da se uporabnika prosi za dostop do lokacije naprave */
    private void request_location_permission() {
        Toast.makeText(this, "V nastavivah aplikaciji omogocite uporabo GPS!", Toast.LENGTH_SHORT).show();
        // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    /* Metoda "check_location_access" podpira preverbo uporabe lokacije na napravi */
    private boolean check_location_access () {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);
    }

    /* Metoda "request_location_access" uporabnika s sporocilom prosi, naj na napravi vklopi aplikacijo */
    private void request_location_access() {
        Toast.makeText(this, "Prosimo, da vključite GPS!", Toast.LENGTH_LONG).show();
        // Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        // startActivity(intent);
    }

    /*
     * Metoda onRequestPermissionsResult se poklice avtomatsko, kadar se uporabnik odzove na zahtevo za dovoljenje
     * prejeme tri argumente:
     * @param requestCode - integer, ki indentificira katero dovloljenje aplikacija zahteva
     * @param permissions - array string-ov, ki predstavljajo katera dovoljenja so bila zahtevana
     * @param grantResults - array integer-jev, ki predstavljajo rezultate za zahtevana dovljenjal
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                request_location_permission();
            }
        }
    }
}