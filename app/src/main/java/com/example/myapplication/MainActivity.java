package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;

import android.media.MediaRecorder;
import android.os.ParcelFileDescriptor;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements SensorEventListener {

    final MediaRecorder microphoneRecorder = new MediaRecorder();
    SensorManager manageSensors;
    Sensor accelerometerSensor;
    long timeStart;
    long frameSwitched;
    List<Frame> allFrames;
    Frame tempFrame;
    boolean started = false;
    frameMeasurement theDecrease = null;
    int theCount = 0;
    frameMeasurement maxMeasurement = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allFrames = new ArrayList<>();
        tempFrame = new Frame();

        manageSensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = manageSensors.getDefaultSensor(Sensor.TYPE_LIGHT);
        manageSensors.registerListener(MainActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        frameSwitched = 0;


    }

    @Override
    public void onSensorChanged(SensorEvent sensorChanged) {
        if(!started){
            started = true;
            timeStart = System.currentTimeMillis();
            System.out.println("STARTED!!!!");
        }
        long measurementTimestamp = System.currentTimeMillis() - timeStart;
        //System.out.println("Time: " + measurementTimestamp);
        //System.out.println("Frame Switched Difference: " + (measurementTimestamp - frameSwitched) / 1000);
        if (((measurementTimestamp - frameSwitched) / 1000) >= 4) {
            frameSwitched = measurementTimestamp;
            allFrames.add(tempFrame);
            List<frameMeasurement> tempMeasurements = tempFrame.getFrameMeasurements();

            for (int x = 0; x < tempMeasurements.size(); x++){
                if(maxMeasurement == null){
                    maxMeasurement = tempMeasurements.get(0);
                }
                if(maxMeasurement != null){
                    if(maxMeasurement.measurement < tempMeasurements.get(x).measurement){
                        maxMeasurement = tempMeasurements.get(x);
                    }
                }
                if(x == 0){
                    if (allFrames.size() >= 2) {
                        List<frameMeasurement> prevTempMeasurements = allFrames.get(allFrames.size()-2).getFrameMeasurements();
                        System.out.println("Last of Previous Frame: " + prevTempMeasurements.get(prevTempMeasurements.size()-1).measurement);
                        System.out.println("Current Frame: " + tempMeasurements.get(x).measurement);
                        System.out.println("Subtraction Between Frame 1: " + ( (prevTempMeasurements.get(prevTempMeasurements.size()-1).measurement) - (tempMeasurements.get(x).measurement) ) );
                        if( ( (prevTempMeasurements.get(prevTempMeasurements.size()-1).measurement) - (tempMeasurements.get(x).measurement) ) > 35 ) {
                            theDecrease = tempMeasurements.get(x);
                            System.out.println("The Decrease Between Frame: " + theDecrease.timestamp + " " + theDecrease.measurement);
                        }
                        else{
                            if( theDecrease != null){
                                theCount++;
                                System.out.println("Increase Count");
                                if( ( (theDecrease.measurement - 5) < (tempMeasurements.get(x).measurement) ) && ( (theDecrease.measurement + 5) > (tempMeasurements.get(x).measurement) ) && (theCount >= 4) ){
                                    System.out.println("STAND BY DETECTED!!!!");
                                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ConstraintLayout gotYourBackLayout = findViewById(R.id.appScreenID);
                                    gotYourBackLayout.setBackgroundColor(Color.RED);
                                }
                                if( (theCount >= 4) && ( (maxMeasurement.measurement - 5) < (tempMeasurements.get(x).measurement) ) && ( (maxMeasurement.measurement + 5) > (tempMeasurements.get(x).measurement) )  ){
                                    System.out.println("STAND BY LEFT!!!!");
                                    theCount = 0;
                                    theDecrease = null;
                                }
                                if( (theCount >= 2) && (theCount <=3) ){
                                    System.out.println("Subtraction 2: " + ( (tempMeasurements.get(x).measurement) - (prevTempMeasurements.get(prevTempMeasurements.size()-1).measurement) ));
                                    if( ( (tempMeasurements.get(x).measurement) - (prevTempMeasurements.get(prevTempMeasurements.size()-1).measurement) ) > 25 ){
                                        System.out.println("PASS BY DETECTED!!!!");
                                        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ConstraintLayout gotYourBackLayout = findViewById(R.id.appScreenID);
                                        gotYourBackLayout.setBackgroundColor(Color.YELLOW);
                                        theCount = 0;
                                        theDecrease = null;
                                    }
                                    else{
                                        if(theCount == 3){
                                            System.out.println("STAND BY DETECTED!!!!");
                                            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ConstraintLayout gotYourBackLayout = findViewById(R.id.appScreenID);
                                            gotYourBackLayout.setBackgroundColor(Color.RED);
                                        }
                                    }
                                }

                            }
                            /*if( (theCount >= 2) && (theCount <=3) ){
                                System.out.println("Subtraction 2: " + ( (tempMeasurements.get(x).measurement) - (prevTempMeasurements.get(prevTempMeasurements.size()-1).measurement) ));
                                if( ( (tempMeasurements.get(x).measurement) - (prevTempMeasurements.get(prevTempMeasurements.size()-1).measurement) ) > 28 ){
                                    System.out.println("STEP DETECTED!!!!");
                                    theCount = 0;
                                    theDecrease = null;
                                }
                            }
                            /*if(theCount > 3){
                                theCount = 0;
                                theDecrease = null;
                            }*/
                            //initialLight = tempMeasurements.get(x);
                        }
                    }
                }
                else{
                    System.out.println("Subtraction 1: " + ((tempMeasurements.get(x-1).measurement) - (tempMeasurements.get(x).measurement)) );
                    if( ( (tempMeasurements.get(x-1).measurement) - (tempMeasurements.get(x).measurement) ) > 35 ) {
                        theDecrease = tempMeasurements.get(x);
                        System.out.println("The Decrease: " + theDecrease.timestamp + " " + theDecrease.measurement);
                    }
                    else{
                        if( theDecrease != null){
                            theCount++;
                            System.out.println("Increase Count");
                            if( ( (theDecrease.measurement - 5) < (tempMeasurements.get(x).measurement) ) && ( (theDecrease.measurement + 5) > (tempMeasurements.get(x).measurement) ) && (theCount >= 4) ){
                                System.out.println("STAND BY DETECTED!!!!");
                                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ConstraintLayout gotYourBackLayout = findViewById(R.id.appScreenID);
                                gotYourBackLayout.setBackgroundColor(Color.RED);
                            }
                            if( (theCount >= 4) && ( (maxMeasurement.measurement - 5) < (tempMeasurements.get(x).measurement) ) && ( (maxMeasurement.measurement + 5) > (tempMeasurements.get(x).measurement) )  ){
                                System.out.println("STAND BY LEFT!!!!");
                                theCount = 0;
                                theDecrease = null;
                            }
                            if( (theCount >= 2) && (theCount <=3) ){
                                System.out.println("Subtraction 2: " + ( (tempMeasurements.get(x).measurement) - (tempMeasurements.get(x-1).measurement) ));
                                if( ( (tempMeasurements.get(x).measurement) - (tempMeasurements.get(x-1).measurement) ) > 25 ){
                                    System.out.println("PASS BY DETECTED!!!!");
                                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ConstraintLayout gotYourBackLayout = findViewById(R.id.appScreenID);
                                    gotYourBackLayout.setBackgroundColor(Color.YELLOW);
                                    theCount = 0;
                                    theDecrease = null;
                                }
                                else{
                                    if(theCount == 3){
                                        System.out.println("STAND BY DETECTED!!!!");
                                        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ConstraintLayout gotYourBackLayout = findViewById(R.id.appScreenID);
                                        gotYourBackLayout.setBackgroundColor(Color.RED);
                                    }
                                }
                            }

                        }
                        /*if( (theCount >= 2) && (theCount <=3) ){
                            System.out.println("Subtraction 2: " + ( (tempMeasurements.get(x).measurement) - (tempMeasurements.get(x-1).measurement) ));
                            if( ( (tempMeasurements.get(x).measurement) - (tempMeasurements.get(x-1).measurement) ) > 28 ){
                                System.out.println("STEP DETECTED!!!!");
                                theCount = 0;
                                theDecrease = null;
                            }
                        }
                        if(theCount > 3){
                            theCount = 0;
                            theDecrease = null;
                        }*/
                        //initialLight = tempMeasurements.get(x);
                    }
                }
            }
            tempFrame = new Frame();
        }
        float[] tempHolder = sensorChanged.values;
        //System.out.println("Measurement Collected");
        frameMeasurement tempFrameRecording = new frameMeasurement();
        tempFrameRecording.measurement = tempHolder[0];
        tempFrameRecording.timestamp = measurementTimestamp;
        tempFrame.addFrameMeasurement(tempFrameRecording);
        if (allFrames.size() == 10) {
            System.out.println("allFrames Length: " + allFrames.size());
            for (Frame frame : allFrames) {
                System.out.println("New Frame");
                List<frameMeasurement> tempMeasurements = frame.getFrameMeasurements();
                System.out.println("Frame Measurements Length: " + frame.getFrameMeasurementLength());
                for (frameMeasurement individualMeasurement : tempMeasurements) {
                    System.out.println(individualMeasurement.timestamp + "," + individualMeasurement.measurement);
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
}

