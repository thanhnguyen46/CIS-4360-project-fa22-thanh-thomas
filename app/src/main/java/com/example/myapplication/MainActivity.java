package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {

    SensorManager manageSensors;
    Sensor accelerometerSensor;
    long timeStart;
    long frameSwitched;
    List<Frame> allFrames;
    Frame tempFrame;

    private static int MICROPHONE_PERMISSION_CODE = 200;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allFrames = new ArrayList<>();
        tempFrame = new Frame();

        manageSensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = manageSensors.getDefaultSensor(Sensor.TYPE_LIGHT);
        manageSensors.registerListener(MainActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        timeStart = System.currentTimeMillis();
        frameSwitched = 0;

        if (isMicrophoneOn()) {
            getMicrophonePermission();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorChanged) {
        long measurementTimestamp = System.currentTimeMillis() - timeStart;
        //System.out.println("Time: " + measurementTimestamp);
        //System.out.println("Frame Switched Difference: " + (measurementTimestamp - frameSwitched) / 1000);
        if( ((measurementTimestamp - frameSwitched) / 1000) >= 4){
            frameSwitched = measurementTimestamp;
            allFrames.add(tempFrame);
            tempFrame = new Frame();
        }
        float[] tempHolder = sensorChanged.values;
        //System.out.println("Measurement Collected");
        frameMeasurement tempFrameRecording = new frameMeasurement();
        tempFrameRecording.measurement = tempHolder[0];
        tempFrameRecording.timestamp = measurementTimestamp;
        tempFrame.addFrameMeasurement(tempFrameRecording);
        if(allFrames.size() == 3){
            System.out.println("allFrames Length: " + allFrames.size());
            for(Frame frame : allFrames){
                List<frameMeasurement> tempMeasurements = frame.getFrameMeasurements();
                System.out.println("Frame Measurements Length: " + frame.getFrameMeasurementLength());
                for(frameMeasurement individualMeasurement : tempMeasurements){
                    System.out.println("Measurement: " + individualMeasurement.measurement);
                    System.out.println("Timestamp: " + individualMeasurement.timestamp);
                }
            }
            System.exit(1);
        }

        /*if(tempHolder[0] > 0){
            System.out.println("Change of: " + tempHolder[0] + " detected");
        }*/

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private boolean isMicrophoneOn() {
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            return true;
        } else {
            return false;
        }
    }

    private void getMicrophonePermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
        }
    }
}

