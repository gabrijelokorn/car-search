package com.example.car_search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;

import android.Manifest;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int INTERVAL_CHECK_LOCATION = 1000; // 5 seconds
    public static final double EARTH_RADIUS_KM = 6371.0;
    public static final int PERMISSION_ID = 44;


    private TextView distanceTextView;
    private Button lock_unlock;
    private ImageView needleImageView;
    private Switch loc_acc_Switch;
    private Switch loc_per_Switch;

    private float north_dgr;
    private boolean location_service;
    private boolean location_used;

    private Locationer locationer;
    private Orientationer orientationer;
    private Timer timer;

    // User's Car and Device instances
    private Car car;
    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lock_unlock = (Button) findViewById(R.id.ButtonTextView);
        lock_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                change_car_state();
            }
        });

        this.car = new Car(this, lock_unlock);
        this.device = new Device();
        this.orientationer = new Orientationer(this, this);
        this.locationer = new Locationer(this);
        this.timer = new Timer();

        distanceTextView = findViewById(R.id.distanceTextView);
        needleImageView = findViewById(R.id.needleImageView);
        loc_per_Switch = findViewById(R.id.loc_per_Switch);
        loc_acc_Switch = findViewById(R.id.loc_acc_Switch);

        loc_per_Switch.setEnabled(false);
        loc_per_Switch.setText("Location permission");
        loc_acc_Switch.setEnabled(false);
        loc_acc_Switch.setText("Location access");

        this.location_service = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        activate_location_timer();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!this.location_service) {
            deactivate_location_timer();
        }
    }

    /*
    * Method button_update updates the validity of LOCK/UNLOCK button based on device's location access.
    * */
    private void button_update() {
        if (!this.location_service && !this.car.get_state()) {
            this.lock_unlock.setEnabled(false);
        } else {
            this.lock_unlock.setEnabled(true);
        }
    }

    /*
    * Mehtod change_car_state is a response to LOCK/UNLOCK button click. It calls negate_car_state to update Car object
    * and button_update to update button state.
    * */
    private void change_car_state() {
        this.car.negate_car_state(this.device);
        button_update();
    }

    /*
    * Method rotate_needle rotates the image of needle in application's main view.
    * */
    private void rotate_needle(float needle_angle) {
        if (this.needleImageView != null) {
            this.needleImageView.setRotation(needle_angle);
        }
    }

    /*
    * Method angle_device_to_car calculates the direction in degrees from device to car based on
    * instructions from https://www.movable-type.co.uk/scripts/latlong.html
    * */
    private float angle_device_to_car () {

        double car_lon_radians = Math.toRadians(this.car.get_longitude());
        double car_lat_radians = Math.toRadians(this.car.get_latitude());

        double device_lon_radians = Math.toRadians(this.device.get_longitude());
        double device_lat_radians = Math.toRadians(this.device.get_latitude());

        double lon_difference = car_lon_radians - device_lon_radians;
        // double lat_difference = car_lat_radians - device_lat_radians;

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

        rotate_needle((float) angle_in_degrees);
        return (float) angle_in_degrees;
    }
    public void update_degrees(float val) {
        this.north_dgr = val;
        angle_device_to_car();
    }

    /*
    * Method distnace_device_to_car calculates distance from user's device to user's car
    * based on program solution from https://www.geeksforgeeks.org/program-distance-two-points-earth/
    * */
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
        distance *= 1000;

        String distanceText = "You are " + Math.round(distance) + " meters away from your car.";

        if (this.car.get_longitude() == 1000.0 || this.car.get_longitude() == 1000.0 || !this.car.get_state()) {
            distanceText = "Your car is not parked";
        }
        distanceTextView.setText(distanceText);

        return (float) distance;
    }
    public void update_device_location(double lon, double lat) {
        this.device.set_longitude((float) lon);
        this.device.set_latitude((float) lat);

        distnace_device_to_car();
    }

    /*
    * The method check_location_permission is intended for checking the permission of two location access modes on the device.
    * */
    private boolean check_location_permission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&  ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /*
    * The method request_location_permission enables requesting the user for access to the device's location.
    * */
    private void request_location_permission() {
        // Toast.makeText(this, "V nastavivah aplikaciji omogocite uporabo GPS!", Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    /*
    * The method check_location_access supports checking the usage of location on the device
    * */
    private boolean check_location_access () {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);
    }

    /*
    * The method request_location_access asks the user, with a message, to enable the location on the device.
    * */
    private void request_location_access() {
        // Toast.makeText(this, "Prosimo, da vključite GPS!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    public void disable_location_service() {
        this.location_service = false;
        activate_location_timer();
    }
    public void enable_location_service() {
        this.location_service = true;
        deactivate_location_timer();
    }

    /*
    * The method start_location_updates serves as the initiation of reading location data.
    * */
    private void start_location_updates() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.locationer);
        this.location_used = true;
        enable_location_service();
    }

    /*
    * Mehtod check_location_service enables location if it has not been yet activated and
    * informs user about lacking location access.*/
    private void check_location_service() {

        distanceTextView.setText("Location out of service!");

        if (!check_location_permission()) {
            this.loc_per_Switch.setChecked(false);
            this.loc_per_Switch.setTextColor(Color.RED);
            request_location_permission();
        } else {
            this.loc_per_Switch.setChecked(true);
            this.loc_per_Switch.setTextColor(Color.rgb(66, 161, 91));
        }

        if (!check_location_access()) {
            this.loc_acc_Switch.setChecked(false);
            this.loc_acc_Switch.setTextColor(Color.RED);
        } else {
            this.loc_acc_Switch.setChecked(true);
            this.loc_acc_Switch.setTextColor(Color.rgb(66, 161, 91));
        }

        if (check_location_permission() && check_location_access()) {
            if (!this.location_used) {
                start_location_updates();
            } else {
                enable_location_service();
            }
        }
        button_update();
    }

    /*
    * Method deactivate_location_timer stops the Timer timer when the location on device is acessible*/
    private void deactivate_location_timer() {
        this.timer.cancel();
    }

    /*
    * Method activate_location_timer periodically calls function check_location_service to check if the
    * location access has been permitted with Timer timer.
    * */
    private void activate_location_timer() {

        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        check_location_service();
                    }
                });
            }
        }, 0, INTERVAL_CHECK_LOCATION);
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
            } else {
                Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}