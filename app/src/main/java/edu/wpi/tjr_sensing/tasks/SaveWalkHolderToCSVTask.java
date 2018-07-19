package edu.wpi.tjr_sensing.tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import edu.wpi.tjr_sensing.models.SensorRecorder;
import edu.wpi.tjr_sensing.models.TestSubject;

/**
 * Created by Adonay on 12/1/2017.
 */

public class SaveWalkHolderToCSVTask extends AsyncTask<Void, Integer, Boolean> {
    private ProgressDialog dialog;
    private String mFolderName;
    private TestSubject testSubject;
    private final String[] space = {""};
    private SensorRecorder sensorRecorder;
    private Context context;
    private String rootFolder;

    public SaveWalkHolderToCSVTask(SensorRecorder sensorRecorder, String mFolderName, Context context) {
        this.sensorRecorder = sensorRecorder;
        this.mFolderName = mFolderName;
        this.rootFolder = mFolderName;
        this.testSubject = sensorRecorder.getTestSubject();
        this.dialog = new ProgressDialog(context);
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        File phoneRoot = new File(mFolderName);
        phoneRoot.mkdirs();
        dialog.setCancelable(false);
        dialog.setTitle("Saving to phone internal storage");
        dialog.setTitle("Writing data to " + mFolderName);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.setMax(100);
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            /*String testSubjectTitle[] = {"Subject ID", "Gender", "Age", "Weight", "Height(ft and inches)"};

            String testSubjectInformation[] = {testSubject.getSubjectID(), testSubject.getGender().toString(),
                    String.valueOf(testSubject.getAge()), String.valueOf(testSubject.getWeight()) + " lbs",
                    String.valueOf(testSubject.getHeightFeet()) + "' " + String.valueOf(testSubject.getHeightInches()) + "''"};

            writer.writeNext(testSubjectTitle);
            writer.writeNext(testSubjectInformation);
            writer.writeNext(space);*/

            //saveBacAsFile();

            double percentProgress = 0;
            int max = testSubject.getCurrentWalkHolder().getSampleSize();
            if (testSubject.getCurrentWalkHolder().getWalkType()!=null) {
                String walkTypeFolderName = mFolderName + File.separator + testSubject.getCurrentWalkHolder().getWalkType().toNoSpaceString();
                File walkTypeRoot = new File(walkTypeFolderName);
                walkTypeRoot.mkdirs();
                LinkedList<LinkedList<String[]>> CSVFormat = testSubject.getCurrentWalkHolder().getWalk(testSubject.getCurrentWalkHolder().getWalkType()).toCSVFormat();
                File file;
                for (int i = 0; i < CSVFormat.size(); i++) {
                    String fileName = walkTypeFolderName + File.separator;
                    if (i == 0) {
                        fileName = fileName + "accelerometer.csv";
                    } else if (i == 1) {
                        fileName = fileName + "gyroscope.csv";
                    } else if (i == 2) {
                        fileName = fileName + "compass.csv";
                    }
                    file = new File(fileName);

                    CSVWriter writer;
                    FileWriter mFileWriter;
                    if (file.exists() && !file.isDirectory()) {
                        mFileWriter = new FileWriter(fileName, false);
                    } else {
                        mFileWriter = new FileWriter(fileName);
                    }

                    writer = new CSVWriter(mFileWriter);

                    writer.writeAll(CSVFormat.get(i));

                        /*if (((++savedSamples/max) * 100) > percentProgress) {
                            percentProgress = (savedSamples / max) * 100;
                            publishProgress((int)percentProgress);
                        }*/
                    writer.close();
                    MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
                }
            }
               /* String messageTitle[] = {"Report Message"};
                writer.writeNext(messageTitle);
                String reportMessage[] = {testSubject.getReportMessage()};
                writer.writeNext(reportMessage);*/
        } catch (
                IOException e)

        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        dialog.setProgress(values[0]);
        dialog.setMessage("Saving Walk Type " + testSubject.getCurrentWalkHolder().getWalkType());
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        dialog.dismiss();
        if (result) {
            sensorRecorder.saveFinished();
        } else {
            //show file save error dialog
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle("Save Error");
            alert.setMessage("An error occurred while saving the data to file. Would you like to try saving again?");
            alert.setPositiveButton("YES", (dialogInterface, i) -> new SaveWalkHolderToCSVTask(sensorRecorder, mFolderName, context).execute());
            alert.setNegativeButton("NO", null);
            alert.show();
        }
    }
}