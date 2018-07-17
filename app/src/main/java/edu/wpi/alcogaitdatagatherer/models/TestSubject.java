package edu.wpi.alcogaitdatagatherer.models;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import edu.wpi.alcogaitdatagatherer.R;

/**
 * Created by Adonay on 9/27/2017.
 */

public class TestSubject implements Serializable {
    private String subjectID;
    private Gender gender;
    private int age;
    private double weight;
    private int heightFeet;
    private int heightInches;
    private WalkHolder currentWalkHolder;
    private LinkedList<Boolean> booleanWalksList; // changed from Walk Object to Integer in order to decrease memory usage
    //private LinkedList<Walk> successfulWalks;
    //private LinkedList<Walk> reportedWalks;
    private String reportMessage;

    public TestSubject(String subjectID, Gender gender, int age, double weight, int heightFeet, int heightInches) {
        this.subjectID = subjectID;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.heightFeet = heightFeet;
        this.heightInches = heightInches;
        this.booleanWalksList = new LinkedList<>();
        //successfulWalks = new LinkedList<>();
        //reportedWalks = new LinkedList<>();
        reportMessage = "";
    }

    public String getSubjectID() {
        return subjectID;
    }

    public Gender getGender() {
        return gender;
    }

    public int getAge() {
        return age;
    }

    public double getWeight() {
        return weight;
    }

    public int getHeightFeet() {
        return heightFeet;
    }

    public int getHeightInches() {
        return heightInches;
    }

    public String getReportMessage() {
        return reportMessage;
    }

    public void setReportMessage(String reportMessage) {
        this.reportMessage = reportMessage;
    }

    public LinkedList<Boolean> getBooleanWalksList() {
        return booleanWalksList;
    }

    public void setBooleanWalksList(LinkedList<Boolean> list) {
        booleanWalksList = list;
    }

    public WalkHolder getCurrentWalkHolder() {
        return currentWalkHolder;
    }

    public void setCurrentWalkHolder(WalkHolder walkHolder) {
        this.currentWalkHolder = walkHolder;
    }

    /*public void addNewWalkHolder(WalkHolder currentWalkHolder) {
        addSamplesSize(this.currentWalkHolder.getWalkNumber(), this.currentWalkHolder.getSampleSize());
        setCurrentWalkHolder(currentWalkHolder);
        booleanWalksList.add(false);
    }*/

    public void setWalkTypeDialog(SensorRecorder sensorRecorder, Context context) {
        AtomicReference<WalkType> walkType = new AtomicReference<WalkType>();

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.walk_type_dialog);
        dialog.setCancelable(false);
        final TextView title = dialog.findViewById(R.id.wtamt_title);
        final RadioButton rd_sbs = dialog.findViewById(R.id.rd_sbs);
        final RadioButton rd_ts = dialog.findViewById(R.id.rd_ts);
        final RadioButton rd_ols = dialog.findViewById(R.id.rd_ols);
        final RadioButton rd_wt = dialog.findViewById(R.id.rd_wt);
        final RadioButton rd_crt = dialog.findViewById(R.id.rd_crt);
        final AppCompatTextView okButton = dialog.findViewById(R.id.okButton);

        title.setText("Select Type Of Walk To Record");

        okButton.setOnClickListener(view -> {
            if (rd_sbs.isChecked()) {
                walkType.set(WalkType.SBS);
            }
            if (rd_ts.isChecked()) {
                walkType.set(WalkType.TS);
            }
            if (rd_ols.isChecked()) {
                walkType.set(WalkType.OLS);
            }
            if (rd_wt.isChecked()) {
                walkType.set(WalkType.WT);
            }
            if (rd_crt.isChecked()) {
                walkType.set(WalkType.CRT);
            }
            if(walkType.get()==null){
                return;
            }
            currentWalkHolder.setWalkType(walkType.get());
            if(currentWalkHolder.hasWalk(walkType.get())){
                sensorRecorder.rePurposeStartButton();
            }
            dialog.dismiss();
            sensorRecorder.updateWalkNumberDisplay();
            sensorRecorder.updateTitleAndInstructions(walkType.get());
        });
        dialog.show();
    }

    public void replaceWalkHolder(WalkHolder currentWalkHolder) {
        this.currentWalkHolder = currentWalkHolder;
    }

    public String printInfo() {
        return "Subject ID: " + subjectID + "\nGender: " + gender.toString()
                + "\nAge: " + String.valueOf(age) + "\nWeight: " + String.valueOf(weight)
                + "\nHeight(ft and inches): " + String.valueOf(heightFeet) + "' "
                + String.valueOf(heightInches) + "''\n";
    }

    public void addToBooleanWalkList(){
        booleanWalksList.add(false);
    }

    public void removeFromBooleanWalkList(){
        booleanWalksList.removeLast();
    }
}
