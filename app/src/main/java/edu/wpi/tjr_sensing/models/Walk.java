package edu.wpi.tjr_sensing.models;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Created by Adonay on 9/29/2017.
 */

public class Walk implements Serializable {
    private WalkType walkType;
    private LinkedList<String[]> phoneAccelerometerDataList;
    private LinkedList<String[]> phoneGyroscopeDataList;
    private LinkedList<String[]> compassDataList;
    private int stoppedTime;

    public Walk(WalkType walkType) {
        this.walkType = walkType;
        phoneAccelerometerDataList = new LinkedList<>();
        phoneGyroscopeDataList = new LinkedList<>();
        compassDataList = new LinkedList<>();
    }

    public WalkType getWalkType() {
        return walkType;
    }

    public void addPhoneAccelerometerData(String[] sensorData) {
        this.phoneAccelerometerDataList.add(sensorData);
    }

    public void addPhoneGyroscopeData(String[] sensorData) {
        this.phoneGyroscopeDataList.add(sensorData);
    }

    public void addCompassData(String[] compassData) {
        this.compassDataList.add(compassData);
    }

    public int getSampleSize(){
        return phoneAccelerometerDataList.size() + phoneGyroscopeDataList.size() + compassDataList.size();
    }

    public int getSubjectDuration(){
        return walkType.testTime-stoppedTime;
    }

    public LinkedList<LinkedList<String[]>> toCSVFormat() {
        final String[] PHONE_ACCELEROMETER_TITLE = {"ACCELEROMETER DATA (PHONE)"};
        final String[] PHONE_GYROSCOPE_TITLE = {"GYROSCOPE DATA (PHONE)"};
        final String[] COMPASS_TITLE = {"COMPASS (PHONE)"};

        final String[] A_G_TABLE_HEADER = {"Sensor Name", "X", "Y", "Z", "Accuracy", "Timestamp"};
        final String[] COMPASS_TABLE_HEADER = {"Derived Data", "Azimuth", "Pitch", "Roll", "Accuracy", "Timestamp"};

        LinkedList<String[]> accelFormat = new LinkedList<>();
        LinkedList<String[]> gyroFormat = new LinkedList<>();
        LinkedList<String[]> compassFormat = new LinkedList<>();

        accelFormat.add(PHONE_ACCELEROMETER_TITLE);
        accelFormat.add(A_G_TABLE_HEADER);
        accelFormat.addAll(phoneAccelerometerDataList);
        gyroFormat.add(PHONE_GYROSCOPE_TITLE);
        gyroFormat.add(A_G_TABLE_HEADER);
        gyroFormat.addAll(phoneGyroscopeDataList);
        compassFormat.add(COMPASS_TITLE);
        compassFormat.add(COMPASS_TABLE_HEADER);
        compassFormat.addAll(compassDataList);

        LinkedList<LinkedList<String[]>> result = new LinkedList<>();
        result.add(accelFormat);
        result.add(gyroFormat);
        result.add(compassFormat);

        return result;
    }

    public int getStoppedTime() {
        return stoppedTime;
    }

    public void setStoppedTime(int stoppedTime) {
        this.stoppedTime = stoppedTime;
    }
}
