package com.example.car_search;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Orientationer implements SensorEventListener {
    private MainActivity mainActivity;
    private SensorManager sensorManager;
    private Sensor accelometer;
    private Sensor magnetometer;

    private Context context;

    private float[] gravity = new float[3];
    private float[] magnetic = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];

    public Orientationer(Context context, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.context = context;
        this.sensorManager = (SensorManager) this.context.getSystemService(this.context.SENSOR_SERVICE);
        this.accelometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        this.sensorManager.registerListener(this, accelometer, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            this.gravity = sensorEvent.values;
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            this.magnetic = sensorEvent.values;
        }

        SensorManager.getRotationMatrix(rotationMatrix, null, gravity, magnetic);
        SensorManager.getOrientation(rotationMatrix, orientation);

        float azimuth_in_radians = orientation[0];
        float azimuth_in_degrees = (float) Math.toDegrees(azimuth_in_radians);

        if (azimuth_in_degrees < 0) {
            azimuth_in_degrees += 360;
        }

        azimuth_in_degrees = 360 - azimuth_in_degrees;

        this.mainActivity.update_degrees(azimuth_in_degrees);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Ta metoda mora obstajati, ker razred Orientationer razsirja SensorEventListner
    }
}
