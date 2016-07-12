package com.example.samsung.orientationsensor;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
    SensorManager sm;
    SensorEventListener accL;
    Sensor accSensor;//단위 : m/s^2
    TextView ax, ay, az;
    FileTest mTextFileManager;

    float[] gravity_data = new float[3];
    float[] accel_data = new float[3];
    final float alpha = (float)0.8;


    private final int MY_PERMISSION_REQUEST_STORAGE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextFileManager = new FileTest();

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);    // SensorManager 인스턴스를 가져옴
        accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);    // 가속도 센서
        accL = new accListener();       // 가속도 센서 리스너 인스턴스
        ax = (TextView)findViewById(R.id.acc_x);
        ay = (TextView)findViewById(R.id.acc_y);
        az = (TextView)findViewById(R.id.acc_z);
        //mTextFileManager.save("짱이당");
        checkPermission();
    }

    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to write the permission.
                Toast.makeText(this, "Read/Write external storage", Toast.LENGTH_SHORT).show();
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSION_REQUEST_STORAGE);

            // MY_PERMISSION_REQUEST_STORAGE is an
            // app-defined int constant

        }
        else {
            // 다음 부분은 항상 허용일 경우에 해당이 됩니다.
            //mTextFileManager.save("20000");
        }
    }



    @Override
    public void onResume() {
        super.onResume();

        sm.registerListener(accL, accSensor, SensorManager.SENSOR_DELAY_NORMAL);    // 가속도 센서 리스너 오브젝트를 등록
    }

    @Override
    public void onPause() {
        super.onPause();

        sm.unregisterListener(accL);    // unregister orientation listener
    }


    private class accListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {  // 가속도 센서 값이 바뀔때마다 호출됨
            gravity_data[0] = alpha * gravity_data[0] + (1 - alpha) * event.values[0]; //먼저 중력데이터를 계산함
            gravity_data[1] = alpha * gravity_data[1] + (1 - alpha) * event.values[1];
            gravity_data[2] = alpha * gravity_data[2] + (1 - alpha) * event.values[2];

            accel_data[0] = event.values[0] - gravity_data[0]; // 순수 가속도센서값에 중력값을 빼줌
            accel_data[1] = event.values[1] - gravity_data[1]; // 아니면 약 9.81 어쩌고 하는값이 더해짐
            accel_data[2] = event.values[2] - gravity_data[2];

            ax.setText(Float.toString(accel_data[0]));
            ay.setText(Float.toString(accel_data[1]));
            az.setText(Float.toString(accel_data[2]));
            Log.i("SENSOR", "Acceleration changed.");
            Log.i("SENSOR", "  Acceleration X: " + event.values[0]
                    + ", Acceleration Y: " + event.values[1]
                    + ", Acceleration Z: " + event.values[2]);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
