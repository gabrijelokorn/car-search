package com.example.car_search;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Button;

public class Car {

    private Context context;
    private boolean locked;
    private float latitude;
    private float longitude;
    private static final int unlockedVALUE = 1000;
    private Button lock_unlock;

    public Car (Context context, Button button) {
        this.context = context;
        this.lock_unlock = button;
        update_car_info();
    }

    public float get_longitude () {
        return this.longitude;
    }

    public float get_latitude () {
        return this.latitude;
    }
    /*
Z metodo get_car_info v lokalne spremenljivke shranimo prješnje stanje o avtu
 */
    private void update_car_info() {
        SharedPreferences sp = this.context.getSharedPreferences("CarINFO", MODE_PRIVATE);
        this.locked = sp.getBoolean("locked", false);
        this.longitude = sp.getFloat("lon", unlockedVALUE);
        this.latitude = sp.getFloat("lat", unlockedVALUE);

        Log.d("stanje:", "stanje: " + this.locked + "at: " + this.longitude + ", " + this.latitude);

        if (this.locked) {
            this.lock_unlock.setText("ODKLENI");
        } else {
            this.lock_unlock.setText("ZAKLENI");
        }
    }

    /*
    Metoda unlock_the_car spremeni informacije o avtu. Avto "odklene":
    locked = false,
    lon (longitude) in lat (latitude) postaneta neveljavni vrednosti
     */
    private void unlock_the_car() {
        SharedPreferences sp = this.context.getSharedPreferences("CarINFO", MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();

        spe.putBoolean("locked", false);
        spe.putFloat("lon", unlockedVALUE);
        spe.putFloat("lat", unlockedVALUE);

        spe.apply();
    }

    /*
    Metoda unlock_the_car spremeni informacije o avtu. Avto "zaklene":
    locked = true,
    lon (longitude) in lat (latitude) postaneta neveljavni vrednosti
     */
    private void lock_the_car(Device device) {
        SharedPreferences sp = this.context.getSharedPreferences("CarINFO", MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();

        spe.putBoolean("locked", true);
        spe.putFloat("lon", device.get_longitude());
        spe.putFloat("lat", device.get_latitude());

        spe.apply();
    }

    /*
    Metoda change_car_state je odziv na gumb odkleni ali zakleni
    Ko uporabnik pritisne gumb, ta funkcija klice pripadajoci funkciji unlock_the_car ali lock_the_car, ki shranita
    informacije o avtu.
    S klicem get_car_info ta funkcija poskrbi tudi za lokalno posodobitev podatkov in
    s klicem na update_car_info za prikaz le teh na graficnem vmesniku
     */
    public void negate_car_state(Device device) {
        if (this.locked) {
            unlock_the_car();
        } else {
            lock_the_car(device);
        }

        update_car_info();
    }
}
