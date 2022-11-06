package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class Frame {
    List<frameMeasurement> Frame = new ArrayList<>();

    public void addFrameMeasurement(frameMeasurement fm) {
        Frame.add(fm);
    }

    public int getFrameMeasurementLength(){
        return Frame.size();
    }

    public List<frameMeasurement> getFrameMeasurements(){
        return Frame;
    }
}
