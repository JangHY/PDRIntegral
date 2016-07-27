package com.example.samsung.orientationsensor;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.Timer;
import java.util.TimerTask;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class TestActivity extends Activity {


    static final int TIMER_DONE = 2;
    static final int START = 3;
    static final int CAL_TIMER_DONE = 4;
    static final int ERROR = 5;

    private StartCatcher mStartListener;

    private XYZAccelerometer xyzAcc;
    private SensorManager mSensorManager;
    private static final long UPDATE_INTERVAL = 500;
    private static final long MEASURE_TIMES = 20;
    private Timer timer;
    private TextView tv;
    private Button testBtn;
    int counter;
    private MeasureData mdXYZ;


    /** handler for async events*/
    Handler hRefresh = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIMER_DONE:

                    onMeasureDone();
                    String es1 = Float.toString(Math.round(mdXYZ.getLastSpeedKm()*100)/100f);
                    tv.append(" END SPEED " + es1 + " " + es2 + " \n");
                    enableButtons();
                    break;
                case START:
                    tv.append(" START");
                    timer = new Timer();
                    timer.scheduleAtFixedRate(
                            new TimerTask() {

                                public void run() {
                                    dumpSensor();
                                }
                            },
                            0,
                            UPDATE_INTERVAL);

                    break;
                case ERROR:
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tv = (TextView) findViewById(R.id.txt);
        testBtn = (Button) findViewById(R.id.btn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tv.append("\n ..");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        setAccelerometer();
        setStartCatcher();
        mSensorManager.registerListener(xyzAcc,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(xyzAcc);
        super.onPause();
    }


    public void onButtonTest(View v) {
        disableButtons();
        mdXYZ = new MeasureData(UPDATE_INTERVAL);
        counter = 0;
        tv.setText("");
        tv.append("Calibrating");
        Calibrator cal = new Calibrator(hRefresh, xyzAcc, START);
        cal.calibrate();

    }

    void dumpSensor() {
        ++counter;
        mdXYZ.addPoint(xyzAcc.getPoint());

        hRefresh.sendEmptyMessage(TICK);

        if (counter > MEASURE_TIMES) {
            timer.cancel();
            hRefresh.sendEmptyMessage(TIMER_DONE);
        }

    }

    private void enableButtons() {
        testBtn.setEnabled(true);

    }


    private void setAccelerometer() {
        xyzAcc = new XYZAccelerometer();
        mSensorManager.registerListener(xyzAcc,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }


    private void disableButtons() {
        testBtn.setEnabled(false);
    }

    private void onMeasureDone() {
        try {
            mdXYZ.process();
            long now = System.currentTimeMillis();
            mdXYZ.saveExt(this, Long.toString(now) + ".csv");
        } catch (Throwable ex) {
            Toast.makeText(this, ex.getMessage().toString(), Toast.LENGTH_SHORT);
        }
    }
}
