package edu.wpi.tjr_sensing.models;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import edu.wpi.tjr_sensing.R;
import edu.wpi.tjr_sensing.tasks.SaveWalkHolderToCSVTask;
import edu.wpi.tjr_sensing.ui.activities.DataGatheringActivity;

import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD;

/**
 * Created by Adonay on 9/11/2017.
 */

public class SensorRecorder implements SensorEventListener {

    public static final int DELAY_IN_MILLISECONDS = 5;

    private DataGatheringActivity dataGatheringActivity;
    private TestSubject testSubject;
    private Walk walk;

    private TextView walkNumberDisplay;
    private TextView walkLogDisplay;
    private TextView titleTextView;
    private TextView summaryTextView;
    private Button startButton;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mMagnetometer;

    private float[] accVal;
    private float[] gyroVal;
    private float[] magVal;
    private Readings readings;
    private LinkedList<WalkType> logQueue;
    private HashMap<WalkType, Integer> logMap;

    private String rootFolderName;
    private String walkFolderName;
    private boolean isRecording;
    private String TAG = "SensorRecorder";
    private static final float ALPHA = 0.15f;

    public SensorRecorder(DataGatheringActivity gatheringActivity, String rootFolderName, TestSubject testSubject, TextView walkNumberDisplay, TextView walkLogDisplay, Button startButton) {
        this.dataGatheringActivity = gatheringActivity;
        this.testSubject = testSubject;
        this.mSensorManager = (SensorManager) gatheringActivity.getSystemService(SENSOR_SERVICE);
        this.mAccelerometer = mSensorManager.getDefaultSensor(TYPE_ACCELEROMETER);
        this.mGyroscope = mSensorManager.getDefaultSensor(TYPE_GYROSCOPE);
        this.mMagnetometer = mSensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD);
        this.rootFolderName = rootFolderName;
        isRecording = false;
        this.titleTextView = gatheringActivity.findViewById(R.id.title);
        this.summaryTextView =gatheringActivity.findViewById(R.id.summary);
        this.walkNumberDisplay = walkNumberDisplay;
        this.walkLogDisplay = walkLogDisplay;
        this.startButton = startButton;
        logQueue = new LinkedList<>();
        logMap = new HashMap<>();
        testSubject.setCurrentWalkHolder(new WalkHolder());
        testSubject.setWalkTypeDialog(this, walkNumberDisplay.getContext());
        prepareReportFile(walkNumberDisplay.getContext());
    }

    public boolean isRecording() {
        return isRecording;
    }

    public TestSubject getTestSubject() {
        return testSubject;
    }

    public void setTestSubject(TestSubject testSubject) {
        this.testSubject = testSubject;
    }

    public void registerListeners() {
        readings = new Readings();
        mSensorManager.registerListener(this, mAccelerometer, DELAY_IN_MILLISECONDS * 1000);
        mSensorManager.registerListener(this, mGyroscope, DELAY_IN_MILLISECONDS * 1000);
        mSensorManager.registerListener(this, mMagnetometer, DELAY_IN_MILLISECONDS * 1000);
    }

    public void unregisterListeners() {
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        if (isRecording) {
            String sensorName = sensorEvent.sensor.getName();
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accVal = sensorEvent.values.clone();
                readings.setAccelerometer(generatePrintableSensorData(sensorName, accVal, sensorEvent.accuracy, sensorEvent.timestamp));

            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gyroVal = sensorEvent.values.clone();
                readings.setGyroscope(generatePrintableSensorData(sensorName, gyroVal, sensorEvent.accuracy, sensorEvent.timestamp));
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magVal = sensorEvent.values.clone();
            }
            if (accVal != null && magVal != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, accVal, magVal);
                if (success) {
                    float compassVal[] = new float[3];
                    SensorManager.getOrientation(R, compassVal);
                    readings.setCompass(generatePrintableSensorData("Compass", compassVal, sensorEvent.accuracy, sensorEvent.timestamp));
                }
            }
            if (readings.isPhoneReady()) {
                readings.updateTime();
                walk.addPhoneAccelerometerData(readings.getAccelerometer());
                walk.addPhoneGyroscopeData(readings.getGyroscope());
                walk.addCompassData(readings.getCompass());
            }
        } else {
            unregisterListeners();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void startRecording() {
        isRecording = true;
        registerListeners();
        walkLogDisplay.setVisibility(View.GONE);
        walk = new Walk(testSubject.getCurrentWalkHolder().getWalkType());
    }

    public void stopRecording(int stopped_time) {
        isRecording = false;
        unregisterListeners();
        walkLogDisplay.setVisibility(View.VISIBLE);
        walk.setStoppedTime(stopped_time);

        testSubject.setCurrentWalkHolder(testSubject.getCurrentWalkHolder().addWalk(walk));

        updateWalkLogDisplay(true);
        updateWalkNumberDisplay();
        dataGatheringActivity.requestSave();
        rePurposeStartButton();
    }

    public void prepareWalkStorage() {
        walkFolderName = rootFolderName;
        File f = new File(walkFolderName);
        f.mkdirs();
    }

    public void saveCurrentWalkNumberToCSV(Context context) {
        new SaveWalkHolderToCSVTask(this, walkFolderName, context).execute();
    }

    public void updateTitleAndInstructions(WalkType walkType){
        titleTextView.setText(walkType.toString());
        summaryTextView.setText(walkType.getInstructions(summaryTextView.getContext()));

    }

    public void updateWalkNumberDisplay() {
        walkNumberDisplay.setText("Walk Type: " + testSubject.getCurrentWalkHolder().getWalkType().toString());
    }

    public void reDoWalk(Context context) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    WalkType walkType = testSubject.getCurrentWalkHolder().getWalkType();
                    if(testSubject.getCurrentWalkHolder().hasWalk(walkType)){
                        testSubject.setCurrentWalkHolder(testSubject.getCurrentWalkHolder().removeWalk(walkType));
                        testSubject.removeFromBooleanWalkList();
                        dataGatheringActivity.resetStartButton();
                    }else{
                        walk = null;
                        dataGatheringActivity.resetStartButton();
                        return;
                    }
                    updateWalkNumberDisplay();
                    removeFromLog(walkType);
                    updateWalkLogDisplay(false);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Re-Do Walk");
        builder.setMessage("Do you want re-do the previous walk? (Walk Type: " +
                testSubject.getCurrentWalkHolder().getWalkType() + ")").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }


    private void updateWalkLogDisplay(boolean addNewWalk) {
        int MAX_LOGS = 5;

        if (addNewWalk) {
            if (logQueue.size() >= MAX_LOGS) {
                logQueue.removeFirst();
            }

            logQueue.add(walk.getWalkType());
            logMap.put(walk.getWalkType(), walk.getStoppedTime());
        }

        String walkLog;
        walkLog = "Last " + MAX_LOGS + " Walks:";
        for (int i = logQueue.size() - 1; i >= 0; i--) {
            walkLog += "\n" + logQueue.get(i).toString() + " stopped at " + logMap.get(logQueue.get(i)) + "s";
        }

        walkLogDisplay.setText(walkLog);
    }

    private void removeFromLog(WalkType walkType){
        if(logQueue.size()>0){
            logQueue.remove(walkType);
            logMap.remove(walkType);
        }
    }

    private void clearWalkLog() {
        logQueue.clear();
        walkLogDisplay.setText("");
    }

    public void saveFinished() {
        testSubject.setWalkTypeDialog(this, walkNumberDisplay.getContext());
        dataGatheringActivity.resetStartButton();
        testSubject.addToBooleanWalkList();
    }

    public void setupTimer(WalkType walkType){
        dataGatheringActivity.setupTimer(walkType);
    }

    public void prepareReportFile(Context context) {
        final File file = new File(rootFolderName, "report.txt");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file, false);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);

            bufferWriter.append(testSubject.printInfo());

            bufferWriter.close();

            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
    }

    public void saveWalkReport(Context context) {
        final File file = new File(rootFolderName, "report.txt");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);

            bufferWriter.append("\n\nReported Walk Types:\n");
            boolean hasReportedWalks = false;
            for (int i = 0; i < testSubject.getBooleanWalksList().size(); i++) {
                if (testSubject.getBooleanWalksList().get(i)) {
                    bufferWriter.append(testSubject.getCurrentWalkHolder().getRecordedWalkTypeList().get(i).toString());
                    if (i != testSubject.getBooleanWalksList().size() - 1 && testSubject.getBooleanWalksList().size() > 1) {
                        bufferWriter.append(", ");
                    }
                    hasReportedWalks = true;
                }
            }
            if (!hasReportedWalks) {
                bufferWriter.append("None");
            }

            bufferWriter.append("\n\nReport Message:\n");
            bufferWriter.append(testSubject.getReportMessage() + "\n");

            bufferWriter.close();

            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
    }

    public WalkType getCurrentWalkType() {
        return testSubject.getCurrentWalkHolder().getWalkType();
    }

    public void rePurposeStartButton(){
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reDoWalk(v.getContext());
            }
        });
        startButton.setText("RESTART");
    }

    public static String[] generatePrintableSensorData(String sensorName, float[] values, int accuracy, long timestamp) {
        int i = 0;

        String[] result = new String[values.length + 3];
        result[i++] = sensorName;
        for (; i <= values.length; i++) {
            result[i] = String.valueOf(values[i - 1]);
        }

        result[i++] = String.valueOf(accuracy);

        //result[i] = simpleDateFormat.format(new Date(getCurrentTimeFromSensor(timestamp)));
        result[i] = "";

        return result;
    }
}
