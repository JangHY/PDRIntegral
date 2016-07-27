package com.example.samsung.orientationsensor;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;

/**
 * Created by sohee on 16. 7. 26..
 */
public abstract class Accelerometer implements SensorEventListener {

    protected float lastX;
    protected float lastY;
    protected float lastZ;
    public abstract Point getPoint();
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }
}
