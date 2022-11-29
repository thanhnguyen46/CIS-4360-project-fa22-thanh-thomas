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
import android.provider.MediaStore;

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
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import com.example.myapplication.FFT;

public class MainActivity extends Activity implements SensorEventListener {

    AudioRecord microphoneRecorder;
    int audioSampleSize;


    SensorManager manageSensors;
    Sensor accelerometerSensor;
    long timeStart;
    long frameSwitched;
    long audioFrameSwitched;
    List<Frame> allFrames;
    List<Frame> audioFrames;
    Frame tempFrame;
    Frame tempAudioFrame;
    boolean started = false;
    frameMeasurement theDecrease = null;
    int theCount = 0;
    frameMeasurement maxMeasurement = null;

    short[] audioSampleHolder;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*allFrames = new ArrayList<>();
        tempFrame = new Frame();

        manageSensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = manageSensors.getDefaultSensor(Sensor.TYPE_LIGHT);
        manageSensors.registerListener(MainActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);*/

        audioSampleSize = AudioRecord.getMinBufferSize(6000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_DEFAULT);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        audioSampleHolder = new short[audioSampleSize];
        microphoneRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 6000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_DEFAULT, audioSampleSize);
        microphoneRecorder.startRecording();
        Thread microphoneThread = new Thread(microphoneRecordingandAnalysis);
        microphoneThread.start();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorChanged) {
        /*if(!started){
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
        }*/

        /*if(tempHolder[0] > 0){
            System.out.println("Change of: " + tempHolder[0] + " detected");
        }*/

    }

    Runnable microphoneRecordingandAnalysis = new Runnable(){
        public void run() {
            audioFrames = new ArrayList<>();
            audioFrameSwitched = 0;


            timeStart = System.currentTimeMillis();
            double audioTime = 0;
            long measurementTimestamp = System.currentTimeMillis() - timeStart;
            System.out.println("Initial Timestamp: " + measurementTimestamp);
            System.out.println("STARTED!!!!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            double[] FFTinput = new double[audioSampleHolder.length];
            double[] FFToutput = new double[audioSampleHolder.length];
            tempAudioFrame = new Frame();
            int totalValues = 0;
            while (measurementTimestamp < 5000) {
                System.out.println("TIME: " + measurementTimestamp);
                measurementTimestamp = System.currentTimeMillis() - timeStart;
                if ( (totalValues % 4096) == 0 ) {
                    runOnUiThread(() -> {
                        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ConstraintLayout gotYourBackLayout = findViewById(R.id.appScreenID);
                        gotYourBackLayout.setBackgroundColor(Color.WHITE);
                    });
                    audioFrameSwitched = measurementTimestamp;
                    audioFrames.add(tempAudioFrame);
                    List<frameMeasurement> tempMeasurements = tempAudioFrame.getFrameMeasurements();
                    System.out.println("NEW FRAME!");
                    float increaseCounter = 0;
                    for(int x = 0; x < tempMeasurements.size(); x++){
                        if(x > 0){
                            float prevMeasurement = tempMeasurements.get(x-1).measurement;
                            if(tempMeasurements.get(x).measurement - prevMeasurement > 0){
                                increaseCounter = increaseCounter + (tempMeasurements.get(x).measurement - prevMeasurement);
                                if(increaseCounter > 1500){
                                    System.out.println("CLOSE STEP DETECTED");
                                    double[] input = new double[16];
                                    double[] output = new double[16];
                                    int inputIndex = 0;
                                    for(int y = x; y < x+16; y++){
                                        input[inputIndex] = tempMeasurements.get(y).measurement;
                                        inputIndex++;
                                    }
                                    FFT theFourierTransform = new FFT(16);
                                    theFourierTransform.fft(input, output);
                                    for(int index = 0; index < 16; index++){
                                        System.out.println(output[index]);
                                    }

                                    runOnUiThread(() -> {
                                        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ConstraintLayout gotYourBackLayout = findViewById(R.id.appScreenID);
                                        gotYourBackLayout.setBackgroundColor(Color.RED);
                                    });
                                }
                                if( (increaseCounter > 1000) && (increaseCounter < 1500) ){
                                    System.out.println("MEDIUM STEP DETECTED");
                                    double[] input = new double[16];
                                    double[] output = new double[16];
                                    int inputIndex = 0;
                                    for(int y = x; y < x+16; y++){
                                        input[inputIndex] = tempMeasurements.get(y).measurement;
                                        inputIndex++;
                                    }
                                    FFT theFourierTransform = new FFT(16);
                                    theFourierTransform.fft(input, output);
                                    for(int index = 0; index < 16; index++){
                                        System.out.println(output[index]);
                                    }
                                    runOnUiThread(() -> {
                                        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ConstraintLayout gotYourBackLayout = findViewById(R.id.appScreenID);
                                        gotYourBackLayout.setBackgroundColor(Color.YELLOW);
                                    });
                                }
                                if( (increaseCounter > 500) && (increaseCounter < 1000) ){
                                    System.out.println("FAR STEP DETECTED");
                                    double[] input = new double[16];
                                    double[] output = new double[16];
                                    int inputIndex = 0;
                                    for(int y = x; y < x+16; y++){
                                        input[inputIndex] = tempMeasurements.get(y).measurement;
                                        inputIndex++;
                                    }
                                    FFT theFourierTransform = new FFT(16);
                                    theFourierTransform.fft(input, output);
                                    for(int index = 0; index < 16; index++){
                                        System.out.println(output[index]);
                                    }
                                    runOnUiThread(() -> {
                                        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ConstraintLayout gotYourBackLayout = findViewById(R.id.appScreenID);
                                        gotYourBackLayout.setBackgroundColor(Color.GREEN);
                                    });
                                }
                            }
                            else{
                                increaseCounter = 0;
                            }
                        }
                    }
                    for(frameMeasurement tempFrameMeasurement : tempMeasurements){
                        /*System.out.print(tempFrameMeasurement.audioTimeStamp);
                        System.out.print(",");
                        System.out.print(tempFrameMeasurement.measurement + "\n");*/
                        //System.out.println(tempFrameMeasurement.fftValue);
                    }
                    tempAudioFrame = new Frame();
                }
                //System.out.println("Timestamp: " + measurementTimestamp);
                microphoneRecorder.read(audioSampleHolder, 0, audioSampleSize);
                int bufferIterations = 0;
                FFToutput = new double[audioSampleHolder.length];
                for (short audioSample : audioSampleHolder) {
                    double placeHolder = audioSample;
                    float floatAudioMeasurement = audioSample;
                    FFTinput[bufferIterations] = placeHolder;
                    totalValues++;
                    audioTime = audioTime + .000125;
                    frameMeasurement tempFrameMeasurement = new frameMeasurement();
                    tempFrameMeasurement.measurement = floatAudioMeasurement;
                    tempFrameMeasurement.audioTimeStamp = audioTime;
                    tempAudioFrame.addFrameMeasurement(tempFrameMeasurement);
                    bufferIterations++;
                }
                FFT theFourierTransform = new FFT(audioSampleHolder.length);
                theFourierTransform.fft(FFTinput, FFToutput);
                int x = 0;
                for(int y = tempAudioFrame.getFrameMeasurements().size()-512; y < tempAudioFrame.getFrameMeasurements().size(); y++){
                    tempAudioFrame.getFrameMeasurements().get(y).fftValue = FFToutput[x];
                    x++;
                }
            }
        }
    };



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }

}

