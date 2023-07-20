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

import android.Manifest;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int unlockedVALUE = 1000;
    public static final double EARTH_RADIUS_KM = 6371.0;
    public static final int PERMISSION_ID = 44;


    private TextView locationTextView;
    private TextView degreesTextView;
    private TextView distanceTextView;
    private TextView angleTextView;
    private Button lock_unlock;

    private float north_dgr;

    private Locationer locationer;
    private Orientationer orientationer;

    // User's Car and Device instances
    private Car car;
    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lock_unlock = (Button) findViewById(R.id.lock_unlock);
        lock_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                change_car_state();
            }
        });

        this.car = new Car(this, lock_unlock);
        this.device = new Device();

        locationTextView = findViewById(R.id.locationTextView); // delete later
        degreesTextView = findViewById(R.id.degreesTextView); // delete later
        distanceTextView = findViewById(R.id.distanceTextView); // delete later
        angleTextView = findViewById(R.id.angleTextView); // delete later
        prepare_application();

        this.orientationer = new Orientationer(this,this);
    }

    @Override
    public void onResume() {
        super.onResume();
        prepare_application();
    }


    /*
    Metoda change_car_state je odziv na gumb odkleni ali zakleni in klice javno metodo razreda Car, ki posodobi vse potrebne podatke o avtu
     */
    private void change_car_state() {
        this.car.negate_car_state(this.device);
    }

    /* Metoda start_location_updates sluzi kot zagon branja podatkov o lokaciji */
    private void start_location_updates() {
        locationer = new Locationer(this);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationer);
    }

    /*
    TODO: prikaz zahtev na graficnem vmesniku!
     */
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

    // https://www.movable-type.co.uk/scripts/latlong.html
    private float angle_device_to_car () {

        double car_lon_radians = Math.toRadians(this.car.get_longitude());
        double car_lat_radians = Math.toRadians(this.car.get_latitude());

        double device_lon_radians = Math.toRadians(this.device.get_longitude());
        double device_lat_radians = Math.toRadians(this.device.get_latitude());

        double lon_difference = car_lon_radians - device_lon_radians;
        double lat_difference = device_lat_radians - car_lat_radians;

        double y = Math.sin(lon_difference) * Math.cos(car_lat_radians);
        double x = Math.cos(device_lat_radians) * Math.sin(car_lat_radians)
                - Math.sin(device_lat_radians) * Math.cos(car_lat_radians)
                * Math.cos(lon_difference);

        double angle_in_radians = Math.atan2(y, x);
        double angle_in_degrees = Math.toDegrees(angle_in_radians);

        angle_in_degrees = 360 - ((angle_in_degrees + 360) % 360);

        angle_in_degrees = 0 + this.north_dgr - angle_in_degrees;

        if (angle_in_degrees < -180) {
            angle_in_degrees += 360;
        }

        if (angle_in_degrees < 0) {
            angle_in_degrees = 360 - Math.abs(angle_in_degrees);
        }

        String distanceText = "Angle to srk19: " + angle_in_degrees + " dgr";
        angleTextView.setText(distanceText);

        return (float) angle_in_degrees;
    }
    public void update_degrees(float val) {
        this.north_dgr = val;

        String degrees = "dgr " + val;
        degreesTextView.setText(degrees);
    }

    // https://www.geeksforgeeks.org/program-distance-two-points-earth/
    private float distnace_device_to_car () {
        double car_lon_radians = Math.toRadians(this.car.get_longitude());
        double car_lat_radians = Math.toRadians(this.car.get_latitude());
        double device_lon_radians = Math.toRadians(this.device.get_longitude());
        double device_lat_radians = Math.toRadians(this.device.get_latitude());

        double lon_difference = car_lon_radians - device_lon_radians;
        double lat_difference = car_lat_radians - device_lat_radians;

        double formula1 = Math.pow(Math.sin(lat_difference / 2), 2) + Math.cos(car_lat_radians) * Math.cos(device_lat_radians) * Math.pow(Math.sin(lon_difference / 2), 2);
        double formula2 = 2 * Math.asin(Math.sqrt(formula1));

        double distance = EARTH_RADIUS_KM * formula2;

        String distanceText = "Distance to srk19: " + distance + " km";
        distanceTextView.setText(distanceText);

        return (float) distance;
    }
    public void update_device_location(double lon, double lat) {
        String locationText = "Lon: " + lon + "\nLat: " + lat;
        locationTextView.setText(locationText);

        this.device.set_longitude((float) lon);
        this.device.set_latitude((float) lat);

        distnace_device_to_car();
        angle_device_to_car();
    }

    /* Metoda check_location_permission je namenjena preverjanju dovoljenja dveh nacinov dostopa lokacije v napravi */
    private boolean check_location_permission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&  ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /* Metoda request_location_permission omogoca, da se uporabnika prosi za dostop do lokacije naprave */
    private void request_location_permission() {
        Toast.makeText(this, "V nastavivah aplikaciji omogocite uporabo GPS!", Toast.LENGTH_SHORT).show();
        // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    /* Metoda check_location_access podpira preverbo uporabe lokacije na napravi */
    private boolean check_location_access () {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);
    }

    /* Metoda request_location_access uporabnika s sporocilom prosi, naj na napravi vklopi aplikacijo */
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