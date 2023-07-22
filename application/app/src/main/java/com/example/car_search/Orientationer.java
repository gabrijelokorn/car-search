package com.example.car_search;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/*
* Orientationer class implements the SensorEventListener interface to listen to changes in the accelerometer and magnetometer sensors.
* */
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

    /*
    * Constructor for Orientationer class that initializes the sensor manager and registers listeners for accelerometer and magnetometer sensors.
    * */
    public Orientationer(Context context, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.context = context;

        // Get the SensorManager system service to access sensors.
        this.sensorManager = (SensorManager) this.context.getSystemService(this.context.SENSOR_SERVICE);
        // Obtain the default accelerometer and magnetometer sensors.
        this.accelometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Register the Orientationer as the listener for the sensors with normal delay.
        this.sensorManager.registerListener(this, accelometer, SensorManager.SENSOR_DELAY_NORMAL);
        this.sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    /*
    * Method onSensorChanged is called when sensor data is changed.
    * */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Accelerometer.
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            this.gravity = sensorEvent.values;
        // Magnetometer
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            this.magnetic = sensorEvent.values;
        }

        // Compute the rotation matrix and device orientation using gravity and magnetic data.
        SensorManager.getRotationMatrix(rotationMatrix, null, gravity, magnetic);
        SensorManager.getOrientation(rotationMatrix, orientation);

        // Extract the azimuth (rotation around the Z-axis) in radians and convert it to degrees.
        float azimuth_in_radians = orientation[0];
        float azimuth_in_degrees = (float) Math.toDegrees(azimuth_in_radians);

        if (azimuth_in_degrees < 0) {
            azimuth_in_degrees += 360;
        }

        // Clockwise orientation
        azimuth_in_degrees = 360 - azimuth_in_degrees;

        this.mainActivity.update_degrees(azimuth_in_degrees);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Method has to be overriden
    }
}
