package com.example.car_search;

/*
* Class Device serves as a class to represent the user's location
* */

public class Device {
    private float longitude;
    private float latitude;

    public float get_longitude() {
        return this.longitude;
    }
    public float get_latitude() {
        return this.latitude;
    }

    public void set_longitude(float val) {
        this.longitude = val;
    }
    public void set_latitude(float val) {
        this.latitude = val;
    }
    public Device() {

    }
}
