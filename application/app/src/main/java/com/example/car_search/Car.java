package com.example.car_search;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;

public class Car {

    private Context context;
    private boolean locked;
    private float latitude;
    private float longitude;
    public static final int unlockedVALUE = 1000;
    private Button lock_unlock;

    /*
    * Constructor of car accpepts two parameters:
    * context to be able to access Sharedpreferences of the application and
    * button which is a ButtonView of the application's LOCK/UNLOCK button
    * */
    public Car (Context context, Button button) {
        this.context = context;
        this.lock_unlock = button;
        update_car_info();
    }

    /*Getter methods for Car's properties*/
    public boolean get_state () {return this.locked;}
    public float get_longitude () {
        return this.longitude;
    }
    public float get_latitude () {
        return this.latitude;
    }

    /*
    * Method update_car_info loads previous information about parked car from Shared preferences.
    * */
    private void update_car_info() {
        SharedPreferences sp = this.context.getSharedPreferences("CarINFO", MODE_PRIVATE);
        this.locked = sp.getBoolean("locked", false);
        this.longitude = sp.getFloat("lon", unlockedVALUE);
        this.latitude = sp.getFloat("lat", unlockedVALUE);

        if (this.locked) {
            this.lock_unlock.setText("UNLOCK");
        } else {
            this.lock_unlock.setText("LOCK");
        }
    }

    /*
    * Mehtod unlock_the_car changes information in SharedPreferences to values of unparked car.
    * */
    private void unlock_the_car() {
        SharedPreferences sp = this.context.getSharedPreferences("CarINFO", MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();

        spe.putBoolean("locked", false);
        spe.putFloat("lon", unlockedVALUE);
        spe.putFloat("lat", unlockedVALUE);

        spe.apply();
    }

    /*
    * Mehtod lock_the_car stores the information of device location into SharedPreferences.
    * This information later serves as a location of parked car.
    * */
    private void lock_the_car(Device device) {
        SharedPreferences sp = this.context.getSharedPreferences("CarINFO", MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();

        spe.putBoolean("locked", true);
        spe.putFloat("lon", device.get_longitude());
        spe.putFloat("lat", device.get_latitude());

        spe.apply();
    }

    /*
    * Method negate_car_state always locks the unlocked car and unlocks the locked car.
    * Parameter Device is expected to get locational information of user.
    * */
    public void negate_car_state(Device device) {
        if (this.locked) {
            unlock_the_car();
        } else {
            lock_the_car(device);
        }

        update_car_info();
    }
}
