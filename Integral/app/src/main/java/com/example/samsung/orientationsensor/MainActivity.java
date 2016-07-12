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
    TextView gx, gy, gz;
    TextView kx, ky, kz;
    FileTest mTextFileManager;

    float[] gravity = new float[3];
    float[] linear_acceleration = new float[3];
    float[] kalmanFilter_acceleration = new float[3];
   // private float mX, mY;
    private KalmanFilter mKalmanAccX;
    private KalmanFilter mKalmanAccY;
    private KalmanFilter mKalmanAccZ;


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

        gx = (TextView)findViewById(R.id.subGrvAcc_x);
        gy = (TextView)findViewById(R.id.subGrvAcc_y);
        gz = (TextView)findViewById(R.id.subGrvAcc_z);

        kx = (TextView)findViewById(R.id.kalmanFilter_x);
        ky = (TextView)findViewById(R.id.kalmanFilter_y);
        kz = (TextView)findViewById(R.id.kalmanFilter_z);
        //mTextFileManager.save("짱이당");
        checkPermission();

        //칼만필터 초기화
        mKalmanAccX = new KalmanFilter(0.0f);
        mKalmanAccY = new KalmanFilter(0.0f);
        mKalmanAccZ = new KalmanFilter(0.0f);


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
        public void onSensorChanged(SensorEvent event) {  // 가속도 센서 값이 "바뀔 때"마다 호출됨
           //https://developer.android.com/reference/android/hardware/SensorEvent.html#values

            // alpha is calculated as t / (t + dT)
            // with t, the low-pass filter's time-constant
            // and dT, the event delivery rate

            final float alpha = (float)0.8;

            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]; //먼저 중력데이터를 계산함
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            linear_acceleration[0] = event.values[0] - gravity[0]; // 순수 가속도센서값에 중력값을 빼줌
            linear_acceleration[1] = event.values[1] - gravity[1]; // 아니면 약 9.81 어쩌고 하는값이 더해짐
            linear_acceleration[2] = event.values[2] - gravity[2];

            //칼만필터를 적용한다
            kalmanFilter_acceleration[0] = (float) mKalmanAccX.update(linear_acceleration[0]);
            kalmanFilter_acceleration[1] = (float) mKalmanAccY.update(linear_acceleration[1]);
            kalmanFilter_acceleration[2] = (float) mKalmanAccZ.update(linear_acceleration[2]);

            ax.setText(Float.toString(event.values[0]));
            ay.setText(Float.toString(event.values[1]));
            az.setText(Float.toString(event.values[2]));

            gx.setText(Float.toString(linear_acceleration[0]));
            gy.setText(Float.toString(linear_acceleration[1]));
            gz.setText(Float.toString(linear_acceleration[2]));

            kx.setText(Float.toString(kalmanFilter_acceleration[0]));
            ky.setText(Float.toString(kalmanFilter_acceleration[1]));
            kz.setText(Float.toString(kalmanFilter_acceleration[2]));

            mTextFileManager.save(Float.toString(event.values[0]), Float.toString(linear_acceleration[0]), Float.toString(kalmanFilter_acceleration[0]));

            /*
            * 부모 레이아웃을 스크롤시켜 마치 뷰객체(오브젝트)가 움직이는것처럼 보이게 한다
              저장해둔 예전값과 현재값의 차를 넣어 변화를 감지한다
              여기에 100을 곱하는것은 차의 숫자가 워낙 작아 움직임이 보이지 않기 때문이다.
              즉, 스피드라고도 보면 된다ㅎㅎ 더 큰숫자를 넣으면 더 빠르게 움직인다.

		      mLayout.scrollBy((int)((mX - filteredX) * 100), (int)((mY - filteredY) * 100));
		      mX = filteredX;
              mY = filteredY;
            */

            Log.i("SENSOR", "Acceleration changed.");
            Log.i("SENSOR Original", "  Acceleration X: " + event.values[0]
                     + ", Acceleration Y: " + event.values[1]
                     + ", Acceleration Z: " + event.values[2]);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}
