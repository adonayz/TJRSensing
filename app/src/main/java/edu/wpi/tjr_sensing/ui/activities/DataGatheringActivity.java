package edu.wpi.tjr_sensing.ui.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.LinkedList;

import edu.wpi.tjr_sensing.R;
import edu.wpi.tjr_sensing.models.SensorRecorder;
import edu.wpi.tjr_sensing.models.TestSubject;
import edu.wpi.tjr_sensing.models.WalkType;
import edu.wpi.tjr_sensing.ui.CountDownAnimation;
import edu.wpi.tjr_sensing.ui.fragments.WalkReportFragment;
import edu.wpi.tjr_sensing.ui.receivers.AdminReceiver;
import it.sephiroth.android.library.tooltip.Tooltip;

import static android.view.View.LAYER_TYPE_HARDWARE;
import static android.view.View.LAYER_TYPE_NONE;

public class DataGatheringActivity extends AppCompatActivity implements WalkReportFragment.ReportFragmentListener{

    public static final int RECORD_TIME_IN_SECONDS = 30;

    private TextView countdownTextField, countdown_title, overlayCountDownTextField;
    private Button startButton;
    private Button stopButton;
    private TextView walkNumberDisplay;
    private AppCompatTextView changeTypeButton, finishButton;
    private TextView walkLogDisplay;
    private FrameLayout progressBarHolder;
    private RelativeLayout bottomBarLayout;

    private String mFolderName;
    private CountDownTimer countDownTimer;
    private SensorRecorder sensorRecorder;
    private TestSubject testSubject;
    private static final int READ_WRITE_PERMISSION_CODE = 1000;
    public static final String TB_FOR_WALK_REPORT = "walk_report";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_gathering);

        Toolbar toolbar = findViewById(R.id.survey_toolbar);
        toolbar.setTitle("Record TJR Data");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent prevIntent = getIntent();
        testSubject= (TestSubject) prevIntent.getSerializableExtra("test_subject");

        initViews();

        configureButtons();

        prepareStoragePath();

        sensorRecorder = new SensorRecorder(this, mFolderName, testSubject, walkNumberDisplay,
                walkLogDisplay, startButton);

        setupTimer(null);
    }

    private void initViews(){
        countdownTextField = findViewById(R.id.countdown);
        countdownTextField.setText(Integer.toString(RECORD_TIME_IN_SECONDS));
        countdown_title = findViewById(R.id.countdown_title);
        overlayCountDownTextField = findViewById(R.id.overlayCountdownTextView);
        startButton = findViewById(R.id.start_recording);
        stopButton = findViewById(R.id.stop_recording);
        walkNumberDisplay = findViewById(R.id.walkNumberDisplay);
        changeTypeButton = findViewById(R.id.changeTypeButton);
        finishButton = findViewById(R.id.finishButton);
        walkLogDisplay = findViewById(R.id.walkLogDisplay);
        walkLogDisplay.setMovementMethod(new ScrollingMovementMethod());
        progressBarHolder = findViewById(R.id.progressBarHolder);
        bottomBarLayout = findViewById(R.id.bottomBar);
        disableBar(true);
    }

    private void configureButtons(){
        normalStartButtonFunction();

        stopButton.setOnClickListener(view -> {
            stopRecording();
        });

        finishButton.setOnClickListener(view -> {
            DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        WalkReportFragment walkReportFragment = new WalkReportFragment();
                        Bundle bundle = walkReportFragment.getArguments();
                        if (bundle == null) {
                            bundle = new Bundle();
                        }
                        bundle.putSerializable(TB_FOR_WALK_REPORT, sensorRecorder.getTestSubject());
                        walkReportFragment.setArguments(bundle);
                        setFragment(walkReportFragment);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        returnToHomeScreen();
                        break;
                }
            };
            final AlertDialog.Builder builder = new AlertDialog.Builder(DataGatheringActivity.this);
            builder.setTitle("Report Walks");
            builder.setMessage("Would you like to submit a report about any of the walks?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        });

        changeTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener3 = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            testSubject.setWalkTypeDialog(sensorRecorder, walkNumberDisplay.getContext());
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                };
                final AlertDialog.Builder builder3 = new AlertDialog.Builder(DataGatheringActivity.this);
                builder3.setTitle("Change Walk Type");
                builder3.setMessage("Would you like to change the type of test/walk you are gathering data from?").setPositiveButton("Yes", dialogClickListener3)
                        .setNegativeButton("No", dialogClickListener3).show();
            }
        });
    }

    private void prepareStoragePath() {
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/TJR_Sensing/";
        String folderName = "ID_" + testSubject.getSubjectID().trim();
        mFolderName = baseDir + folderName;
        File surveyStorageDirectory = new File(mFolderName);
        surveyStorageDirectory.mkdirs();
    }

    public void setupTimer(WalkType walkType){
        int time;
        if(walkType == null){
            time = RECORD_TIME_IN_SECONDS;
        }else{
            time = walkType.getTime();
        }
        countdownTextField.setText(Integer.toString(time));
        countDownTimer = new CountDownTimer(time * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                countdownTextField.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP,1000);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    v.vibrate(1000);
                } else {
                    showToast("UNABLE TO VIBRATE");
                }
                stopRecording();
            }
        };
    }

    private void startRecording() {
        if(!sensorRecorder.isRecording()){
            sensorRecorder.startRecording();

            startButton.setVisibility(View.GONE);
            stopButton.setVisibility(View.VISIBLE);
            countdown_title.setVisibility(View.VISIBLE);
            walkNumberDisplay.setVisibility(View.VISIBLE);
            disableBar(true);

            countDownTimer.start();
        }
    }

    private void stopRecording(){
        if(sensorRecorder.isRecording()){
            sensorRecorder.stopRecording(Integer.valueOf(countdownTextField.getText().toString()));
            countdown_title.setVisibility(View.GONE);
            countDownTimer.cancel();
            stopButton.setVisibility(View.GONE);

            resetRecordViews();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (sensorRecorder != null) {
            if (sensorRecorder.isRecording()) {
                sensorRecorder.registerListeners();
            }
        }
    }

    @Override
    protected void onPause(){
        if (sensorRecorder != null) {
            if (sensorRecorder.isRecording()) {
                sensorRecorder.unregisterListeners();
            }
        }
        super.onPause();
    }

    /**
     * Creates tooltips on the screen to guide the user through the application.
     * @param view View that the tooltip will be attached (pointing towards to).
     * @param gravity Specifies the position the tooltip will be placed relative to the attached view.
     * @param text The text that will be siplayed as a message on the tooltip.
     */
    public void createToolTip(View view, Tooltip.Gravity gravity, String text){
        Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(view, gravity)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(true, false), 3000)
                        .activateDelay(800)
                        .showDelay(300)
                        .text(text)
                        .maxWidth(700)
                        .withArrow(true)
                        .withOverlay(true)
                        .withStyleId(R.style.ToolTipLayoutCustomStyle)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .build()
        ).show();
    }

    public void requestSave(){
        sensorRecorder.prepareWalkStorage();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        READ_WRITE_PERMISSION_CODE);
            }
        }else{
            saveCurrentWalkNumber();
        }

    }

    public void startProgressBar() {
        AlphaAnimation inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        progressBarHolder.setAnimation(inAnimation);
        progressBarHolder.setVisibility(View.VISIBLE);
    }

    public void stopProgressBar() {
        AlphaAnimation outAnimation = new AlphaAnimation(1f, 0f);
        outAnimation.setDuration(200);
        progressBarHolder.setAnimation(outAnimation);
        progressBarHolder.setVisibility(View.GONE);
    }

    private void showToast(final String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    public void disableBar(boolean disableBar) {
        if (disableBar) {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0.5f);
            Paint greyscalePaint = new Paint();
            greyscalePaint.setColorFilter(new ColorMatrixColorFilter(cm));
            bottomBarLayout.setLayerType(LAYER_TYPE_HARDWARE, greyscalePaint);
            for(int i = 0; i < bottomBarLayout.getChildCount(); i++){
                (bottomBarLayout.getChildAt(i)).setEnabled(false);
            }
        } else {
            bottomBarLayout.setLayerType(LAYER_TYPE_NONE, null);
            for(int i = 0; i < bottomBarLayout.getChildCount(); i++){
                (bottomBarLayout.getChildAt(i)).setEnabled(true);
            }
        }
    }

    protected void setFragment(WalkReportFragment fragment) {
        FragmentManager manager = getFragmentManager();
        fragment.show(manager, "FRAGMENT");
    }

    private void saveCurrentWalkNumber() {
        sensorRecorder.saveCurrentWalkNumberToCSV(this);
        //createToolTip(bacInput, Tooltip.Gravity.RIGHT, "Update BAC for next walk");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_WRITE_PERMISSION_CODE : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveCurrentWalkNumber();
                }
            }
        }
    }

    private void resetRecordViews() {
        startButton.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.VISIBLE);
        // temporarily disallow changing BAC in the same walk number
        /*if (allowInput) {
            bacInput.setEnabled(true);
            createToolTip(bacInput, Tooltip.Gravity.RIGHT, "Update BAC for next walk");
        }*/
        disableBar(false);
    }

    @Override
    public void submitReport(LinkedList<Boolean> checkBoxStates, String reportMessage) {
        TestSubject testSubject = sensorRecorder.getTestSubject();

        testSubject.setBooleanWalksList(checkBoxStates);
        testSubject.setReportMessage(reportMessage);

        sensorRecorder.setTestSubject(testSubject);

        sensorRecorder.saveWalkReport(this);

        returnToHomeScreen();
    }

    void returnToHomeScreen() {
        finish();
        Intent intent = new Intent(DataGatheringActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    DataGatheringActivity.super.onBackPressed();
                    DataGatheringActivity.this.finish();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(DataGatheringActivity.this);
        builder.setTitle("Return To Subject Information Form?");
        builder.setMessage("Are you sure you want to return to the form? Data for the latest walk type will be lost (#"
                + sensorRecorder.getTestSubject().getCurrentWalkHolder().getWalkType() + ")").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.refresh_watch_icon:
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void lockPhone() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager.isScreenOn()) {
            DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            try {
                policyManager.lockNow();
            } catch (SecurityException e) {
                Toast.makeText(this, "Needs Admisnistrator Privileges", Toast.LENGTH_LONG).show();
                ComponentName admin = new ComponentName(this, AdminReceiver.class);
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
                this.startActivity(intent);
            }
        }
    }
    public void resetStartButton(){
        normalStartButtonFunction();
        startButton.setText("START");
    }
    public void normalStartButtonFunction(){
        startButton.setOnClickListener(v -> {
            startProgressBar();
            CountDownAnimation countDownAnimation = new CountDownAnimation(overlayCountDownTextField, 5);
            countDownAnimation.start();

            countDownAnimation.setCountDownListener(new CountDownAnimation.CountDownListener() {
                @Override
                public void onCountDownEnd(CountDownAnimation animation) {
                    startRecording();
                    stopProgressBar();
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_ONE_MIN_BEEP,1000);
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (v != null) {
                        v.vibrate(1000);
                    } else {
                        showToast("UNABLE TO VIBRATE");
                    }
                }
            });
        });
    }
}
