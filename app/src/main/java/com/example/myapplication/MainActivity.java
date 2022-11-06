package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {

    SensorManager manageSensors;
    Sensor accelerometerSensor;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manageSensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = manageSensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        manageSensors.registerListener(MainActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorChanged) {
        float[] tempHolder = sensorChanged.values;
        if(tempHolder[0] > 0){
            System.out.println("Change of: " + tempHolder[0] + " detected");
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}