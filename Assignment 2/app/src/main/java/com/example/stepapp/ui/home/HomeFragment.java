package com.example.stepapp.ui.home;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GnssAntennaInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.stepapp.databinding.FragmentHomeBinding;
import com.example.stepapp.R;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

public class HomeFragment extends Fragment {
    MaterialButtonToggleGroup materialButtonToggleGroup;

    // Text view and Progress Bar variables
    public TextView stepsCountTextView;
    public  ProgressBar progressBar;


    private SensorEventListener listener;
    //ACC sensors
    private Sensor mSensorACC;
    private SensorManager mSensorMangaer;
    private Sensor mSensorStepDetector;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        View root  = inflater.inflate(R.layout.fragment_home, container,  false);

        stepsCountTextView = (TextView) root.findViewById(R.id.stepsCount);
        stepsCountTextView.setText("1000");

        progressBar = (ProgressBar) root.findViewById(R.id.progressBar);
        progressBar.setMax(6000);
        progressBar.setProgress(1000);


        // TODO 2: Get an instance of the sensor manager.
        mSensorMangaer = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorACC = mSensorMangaer.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        listener = new StepCounterListener(stepsCountTextView);

        // Toggle group button
        materialButtonToggleGroup = (MaterialButtonToggleGroup) root.findViewById(R.id.toggleButtonGroup);
        materialButtonToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (group.getCheckedButtonId() == R.id.toggleStart) {

                    //Place code related to Start button
                    Toast.makeText(getContext(), "START", Toast.LENGTH_SHORT).show();

                    //register the ACC listener
                    mSensorMangaer.registerListener(listener, mSensorACC, SensorManager.SENSOR_DELAY_NORMAL);

                } else if (group.getCheckedButtonId() == R.id.toggleStop) {
                    //Place code related to Stop button
                    Toast.makeText(getContext(), "STOP", Toast.LENGTH_SHORT).show();

                    mSensorMangaer.unregisterListener(listener);
                }
            }
        });
        return root;
    }

}


// Sensor event listener
class StepCounterListener implements SensorEventListener {

    private long lastUpdate = 0;

    // ACC Step counter
    public static int mACCStepCounter = 0;
    ArrayList<Integer> mACCSeries = new ArrayList<Integer>();
    private double accMag = 0;
    private int lastXPoint = 1;
    int stepThreshold = 6;

    // Android step detector
    int mAndroidStepCount = 0;

    // TextView
    TextView stepsCountTextView;

    //TODO 10
    public StepCounterListener(TextView tv){
        stepsCountTextView = tv;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {

            // TODO 5: Get the sensor type
            case Sensor.TYPE_LINEAR_ACCELERATION:

                // TODO 6: Get sensor's values
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];


                //////////////////////////// -- PRINT ACC VALUES -- ////////////////////////////////////
                // TODO 7: Uncomment the following code


                // Timestamp
                long timeInMillis = System.currentTimeMillis() + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000;

                // Convert the timestamp to date
                SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                jdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                String date = jdf.format(timeInMillis);

                // print a value every 1000 ms
                long curTime = System.currentTimeMillis();
                if ((curTime - lastUpdate) > 1000) {
                    lastUpdate = curTime;

                    Log.d("ACC", "X: " + String.valueOf(x) + " Y: " + String.valueOf(y) + " Z: "
                            + String.valueOf(z) + " t: " + String.valueOf(date));

                }



                ////////////////////////////////////////////////////////////////////////////////////////

                // TODO 8: Compute the ACC magnitude

                //Update the Magnitude series
                mACCSeries.add((int) accMag);


                /// STEP COUNTER ACC ////
                // Calculate ACC peaks and steps
                accMag = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
                peakDetection();

                break;

            // case Step detector
            //case Sensor.TYPE_STEP_DETECTION:
                // Calculate the number of steps
                //countSteps(event.values[0]);


        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void peakDetection() {
        int windowSize = 20;

        /* Peak detection algorithm derived from: A Step Counter Service for Java-Enabled Devices Using a Built-In Accelerometer, Mladenov et al.
         */
        int highestValX = mACCSeries.size(); // get the length of the series
        if (highestValX - lastXPoint < windowSize) { // if the segment is smaller than the processing window skip it
            return;
        }

        List<Integer> valuesInWindow = mACCSeries.subList(lastXPoint,highestValX);

        lastXPoint = highestValX;

        int forwardSlope = 0;
        int downwardSlope = 0;

        List<Integer> dataPointList = new ArrayList<Integer>();

        for (int p =0; p < valuesInWindow.size(); p++){
            dataPointList.add(valuesInWindow.get(p));
        }


        for (int i = 0; i < dataPointList.size(); i++) {
            if (i == 0) {
            }
            else if (i < dataPointList.size() - 1) {
                forwardSlope = dataPointList.get(i + 1) - dataPointList.get(i);
                downwardSlope = dataPointList.get(i)- dataPointList.get(i - 1);

                if (forwardSlope < 0 && downwardSlope > 0 && dataPointList.get(i) > stepThreshold ) {
                    mACCStepCounter += 1;
                    Log.d("ACC STEPS: ", String.valueOf(mACCStepCounter));

                    //TODO 12: update the text view
                    stepsCountTextView.setText(String.valueOf(mACCStepCounter));

                }
            }
        }
    }

    // Calculate the number of steps from the step detector
    private void countSteps(float step) {

        int mstepCount = (int) step;

        Log.d("NUM STEPS ANDROID:", "Num.steps: " + String.valueOf(mstepCount));



    }
}