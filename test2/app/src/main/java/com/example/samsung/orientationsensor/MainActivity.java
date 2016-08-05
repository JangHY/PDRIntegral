package com.example.samsung.orientationsensor;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Collections;


public class MainActivity extends Activity {
    SensorManager sm;
    SensorEventListener accL;
    SensorEventListener oriL;
    SensorEventListener pressL;
    Sensor pressSensor;
    Sensor accSensor;//단위 : m/s^2
    Sensor oriSensor;

    TextView ax, ay, az;
    TextView gx, gy, gz;
    TextView kx, ky, kz;
    TextView ox, oy, oz;
    TextView px, py, pz, pAltitude;

    TextView distance;
//    FileTest mTextFileManager;
    float[] gravity = new float[3];
    float[] linear_acceleration = new float[3];
    float[] average = new float[3];
    float[] kalmanFilter_acceleration = new float[3];
   // private float mX, mY;
    private KalmanFilter mKalmanAccX;
    private KalmanFilter mKalmanAccY;
    private KalmanFilter mKalmanAccZ;

    float sumXYZAcc;//수식을 계산한 값

    Customhandler1 customHandler1;
    Customhandler2 customHandler2;

    ArrayList[] accValue = new ArrayList[3];

    private final int MY_PERMISSION_REQUEST_STORAGE = 100;

    double velocity,velocityX,velocityY,velocityZ = 0;
    double dist = 0;
    double time = 0.02;
    double distance_result=0;
    private double lastX;
    private double lastY;
    private double lastZ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mTextFileManager = new FileTest();

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);    // SensorManager 인스턴스를 가져옴
        oriSensor = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);    // 방향 센서
        accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);    // 가속도 센서
        pressSensor = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);    //기압센서

        oriL = new oriListener();        // 방향 센서 리스너 인스턴스
        accL = new accListener();       // 가속도 센서 리스너 인스턴스
        pressL = new pressListener();       // 기압 센서 리스너 인스턴스

        ax = (TextView)findViewById(R.id.acc_x);
        ay = (TextView)findViewById(R.id.acc_y);
        az = (TextView)findViewById(R.id.acc_z);

        ox = (TextView)findViewById(R.id.ori_x);
        oy = (TextView)findViewById(R.id.ori_y);
        oz = (TextView)findViewById(R.id.ori_z);

        px = (TextView)findViewById(R.id.press_x);
        py = (TextView)findViewById(R.id.press_y);
        pz = (TextView)findViewById(R.id.press_z);
        pAltitude = (TextView)findViewById(R.id.acc_altitude);


        gx = (TextView)findViewById(R.id.subGrvAcc_x);
        gy = (TextView)findViewById(R.id.subGrvAcc_y);
        gz = (TextView)findViewById(R.id.subGrvAcc_z);

        kx = (TextView)findViewById(R.id.kalmanFilter_x);
        ky = (TextView)findViewById(R.id.kalmanFilter_y);
        kz = (TextView)findViewById(R.id.kalmanFilter_z);
        //mTextFileManager.save("짱이당");

        distance = (TextView)findViewById(R.id.distance);

        accValue[0] = new ArrayList();
        accValue[1] = new ArrayList();
        accValue[2] = new ArrayList();

//        Log.i("버전 정보 : ", String.valueOf(Build.VERSION.SDK_INT));
        if(Build.VERSION.SDK_INT >= 23) {
            checkPermission();
        }



        // checkPermission();//버전으로 나눠서 해줘야 됨

        //칼만필터 초기화
        mKalmanAccX = new KalmanFilter(0.0f);
        mKalmanAccY = new KalmanFilter(0.0f);
        mKalmanAccZ = new KalmanFilter(0.0f);

        TimerTask timerTask1 = new TimerTask() {
            @Override
            public void run() {//0.001초 마다 센서 값받아서 소팅하고 리스트에 넣기
                Message msg = customHandler1.obtainMessage();
                customHandler1.sendMessage(msg);
            }
        };

        Timer timer1 = new Timer();
        timer1.schedule(timerTask1,0,2);//delay : 0, 주기 : 0.002초
        //timer.schedule(timer, delayTime, period)
        // delay:처음에 딜레이 되는 시간  period:주기(ms) 1초->1000   1->0.001초
        customHandler1 = new Customhandler1();

        TimerTask timerTask2 = new TimerTask() {
            @Override
            public void run() {//0.02초 마다 센서 필터링 해서 칼만필터에 넣기기
               Message msg = customHandler2.obtainMessage();
                customHandler2.sendMessage(msg);
            }
        };

        Timer timer2 = new Timer();
        timer2.schedule(timerTask2,18,20);//delay : 0, 주기 : 0.02초
        //timer.schedule(timer, delayTime, period)
        // delay:처음에 딜레이 되는 시간  period:주기(ms) 1초->1000   1->0.001초
        customHandler2 = new Customhandler2();

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

        sm.registerListener(oriL, oriSensor, SensorManager.SENSOR_DELAY_NORMAL);    // 방향 센서 리스너 오브젝트를 등록
        sm.registerListener(pressL, pressSensor, SensorManager.SENSOR_DELAY_NORMAL);    // 기압 센서 리스너 오브젝트를 등록
    }

    @Override
    public void onPause() {
        super.onPause();

        sm.unregisterListener(accL);    // unregister orientation listener
        sm.unregisterListener(accL);    // unregister orientation listener
        sm.unregisterListener(pressL);    // unregister press listener
    }



    private class accListener implements SensorEventListener {


        public void onSensorChanged(SensorEvent event) {  // 가속도 센서 값이 "바뀔 때"마다 호출됨.
           // 1초에 40번 정도 불러짐 -> 0.2초면 8번 정도 -> 가장 큰 값, 작은 값 뺴고 6개 평균내서 칼만필터 적용
           //https://developer.android.com/reference/android/hardware/SensorEvent.html#values

            // alpha is calculated as t / (t + dT)
            // with t, the low-pass filter's time-constant
            // and dT, the event delivery rate

            final float alpha = (float)0.8;

            //original[0] = event.values[0];
            //original[1] = event.values[1];
            //original[2] = event.values[2];
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]; //먼저 중력데이터를 계산함
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            linear_acceleration[0] = event.values[0] - gravity[0]; // 순수 가속도센서값에 중력값을 빼줌
            linear_acceleration[1] = event.values[1] - gravity[1]; // 아니면 약 9.81 어쩌고 하는값이 더해짐
            linear_acceleration[2] = event.values[2] - gravity[2];

            //칼만필터를 적용한다
//            kalmanFilter_acceleration[0] = (float) mKalmanAccX.update(linear_acceleration[0]);
//            kalmanFilter_acceleration[1] = (float) mKalmanAccY.update(linear_acceleration[1]);
//            kalmanFilter_acceleration[2] = (float) mKalmanAccZ.update(linear_acceleration[2]);



            ax.setText(Float.toString(event.values[0]));
            ay.setText(Float.toString(event.values[1]));
            az.setText(Float.toString(event.values[2]));

            gx.setText(Float.toString(linear_acceleration[0]));
            gy.setText(Float.toString(linear_acceleration[1]));
            gz.setText(Float.toString(linear_acceleration[2]));

//            kx.setText(Float.toString(kalmanFilter_acceleration[0]));
//            ky.setText(Float.toString(kalmanFilter_acceleration[1]));
//            kz.setText(Float.toString(kalmanFilter_acceleration[2]));

//            mTextFileManager.save(Float.toString(event.values[0]), Float.toString(linear_acceleration[0]), Float.toString(kalmanFilter_acceleration[0]));
//            Log.i("SENSOR", "Acceleration changed.");
//            Log.i("SENSOR Original", "  Acceleration X: " + event.values[0]
//                     + ", Acceleration Y: " + event.values[1]
//                     + ", Acceleration Z: " + event.values[2]);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    private class oriListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {  // 방향 센서 값이 바뀔때마다 호출됨
            ox.setText(Float.toString(event.values[0]));    //방위각   0=북쪽, 90=동쪽, 180=남쪽, 270=서쪽
            //기기를 수평으로 두었을 때 기기의 머리부분이 어느방향을 가리키고 있는가.
            oy.setText(Float.toString(event.values[1]));    //경사도 : 기기의 수직 기울기
            //기기의 머리부분과 아래부분이 수평을 이룰 때 0값을 가지며 머리부분의 높이가 높아지면 수치값이 감소한다
            //머리부분의 높이가 낮아지면 수치값이 증가한다.
            oz.setText(Float.toString(event.values[2]));    //좌우회전 : 기기의 수평 기울기
            //화면이 하늘을 향하고 있을 때 기기의 좌, 우 부분이 수평이면 0
            //기기의 좌측위치가 높아지면 증가, 우측 위치가 높아지면 감소
            Log.i("SENSOR", "Orientation changed.");
            Log.i("SENSOR", "  Orientation X: " + event.values[0]
                    + ", Orientation Y: " + event.values[1]
                    + ", Orientation Z: " + event.values[2]);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private class pressListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {  //기압 센서 값이 바뀔때마다 호출됨
            px.setText(Float.toString(event.values[0]));    //values[0] : 대기압(Atmospheric pressure)
            py.setText(Float.toString(event.values[1]));    //values[1] : 고도(Altitude)
            pz.setText(Float.toString(event.values[2]));

            Log.i("SENSOR", "Pressure changed.");
            Log.i("SENSOR", "Pressure X: " + event.values[0] + ", Pressure Y: " + event.values[1] + ", Pressure Z: " + event.values[2]);

            float altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, event.values[0]);
            System.out.println("altitude = " + altitude);
            pAltitude.setText(Float.toString(altitude));
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    public class Customhandler1 extends android.os.Handler{
        @Override
        public void handleMessage(Message msg) {//0.002초마다 가속도센서 값 받아와서 소팅하여 리스트에 넣기.
            super.handleMessage(msg);
            //이곳에 실행할 작업내용을 넣습니다. (메인 스레드 작업이 가능!)
          //  Toast.makeText(getApplication(),"1초 마다"+accValue[0].size()+"  "+accValue[1].size()+"  "+accValue[2].size()+"  ",Toast.LENGTH_LONG).show();
 //           Log.i("array value","0.001초 마다"+accValue[0].size()+"  "+accValue[1].size()+"  "+accValue[2].size());
            //큰값, 작은값 빼고 평균 내기

            accValue[0].add(linear_acceleration[0]);
            accValue[1].add(linear_acceleration[1]);
            accValue[2].add(linear_acceleration[2]);//리스트 값 삽입
            String a = "";
            for(int i = 0; i < accValue[0].size(); i++){
                a = a +" "+ accValue[0].get(i);
            }
//            Log.i("array list",a);
            a = "";
            Collections.sort(accValue[0]);
            Collections.sort(accValue[1]);
            Collections.sort(accValue[2]);//리스트 오름차순으로 소팅
            for(int i = 0; i < accValue[0].size(); i++){
                a = a +" "+ accValue[0].get(i);
            }
//            Log.i("array list sort",a);
        }
    }

    public class Customhandler2 extends android.os.Handler{
        @Override
        public void handleMessage(Message msg) {//0.02초마다 리스트에 있는 값들 필터링 해서 칼만필터로 보내기
            super.handleMessage(msg);
            //이곳에 실행할 작업내용을 넣습니다. (메인 스레드 작업이 가능!)
            //  Toast.makeText(getApplication(),"1초 마다"+accValue[0].size()+"  "+accValue[1].size()+"  "+accValue[2].size()+"  ",Toast.LENGTH_LONG).show();
//            Log.i("array value","1초 마다"+accValue[0].size()+"  "+accValue[1].size()+"  "+accValue[2].size());
            //큰값, 작은값 빼고 평균 내기

//            Log.i("array size ",accValue[0].size()+"");//가장 작은 값 삭제
            if(accValue[0].size()>=1) {
                accValue[0].remove(0);
                accValue[1].remove(0);
                accValue[2].remove(0);
            }
//            Log.i("array size 2",accValue[0].size()+"");

            if(accValue[0].size() >= 1) {//가장 큰 값 삭제
                accValue[0].remove(accValue[0].size() - 1);
                accValue[1].remove(accValue[1].size() - 1);
                accValue[2].remove(accValue[2].size() - 1);
//                Log.i("array size 3", accValue[0].size() + "");
            }

            float value0 = 0, value1 = 0, value2 = 0;
            for(int i = 0; i < accValue[0].size(); i++){//리스트 값들 다 더하기
                value0 = value0 + (float)accValue[0].get(i);
                value1 = value1 + (float)accValue[1].get(i);
                value2 = value2 + (float)accValue[2].get(i);
            }

//            Log.d("size0","size0 = "+accValue[0].size());
//            Log.d("size1","size1 = "+accValue[1].size());
//            Log.d("size2","size2 = "+accValue[2].size());

            if(accValue[0].size()!=0 && accValue[1].size()!=0 && accValue[2].size()!=0) {
                value0 = value0 / accValue[0].size();//평균 값 구하기
                value1 = value1 / accValue[1].size();
                value2 = value2 / accValue[2].size();
            }

            average[0] = value0;
            average[1] = value1;
            average[2] = value2;

//            Log.i("average",value0 +"  /  "+value1+"  /  "+value2);

            kalmanFilter_acceleration[0] = (float) mKalmanAccX.update(value0);//칼만 필터에 적용
            kalmanFilter_acceleration[1] = (float) mKalmanAccY.update(value1);
            kalmanFilter_acceleration[2] = (float) mKalmanAccZ.update(value2);

            sumXYZAcc = (float)Math.sqrt(kalmanFilter_acceleration[0]*kalmanFilter_acceleration[0] + kalmanFilter_acceleration[1]*kalmanFilter_acceleration[1] + kalmanFilter_acceleration[2]*kalmanFilter_acceleration[2]);
           //수식 계산
            kx.setText(Float.toString(kalmanFilter_acceleration[0]));
            ky.setText(Float.toString(kalmanFilter_acceleration[1]));
            kz.setText(Float.toString(kalmanFilter_acceleration[2]));

//            Log.i("textValue",Float.toString(average[0]) +"  /  "+Float.toString(linear_acceleration[0])+"  /  "+Float.toString(kalmanFilter_acceleration[0]));

//            mTextFileManager.save(Float.toString(average[0]) , Float.toString(kalmanFilter_acceleration[0]));




            /***********************************************************************************
             *
             *
             *
             *
             *
             * 여기서 적분 함수를 부른다
             * sumXYZAcc 가 더해진다.
             *
             *
             *
             *
             *
             * *******************************************************************************/

            if(!checkMove(kalmanFilter_acceleration[0],kalmanFilter_acceleration[1],kalmanFilter_acceleration[2])){

            }
            //double distance_result = integral(calData(kalmanFilter_acceleration[0], kalmanFilter_acceleration[1], kalmanFilter_acceleration[2]));
            else {
                velocityX=time*kalmanFilter_acceleration[0];
                velocityY=time*kalmanFilter_acceleration[1];
                velocityZ=time*kalmanFilter_acceleration[2];

                velocity=Math.sqrt(velocityX*velocityX+velocityY*velocityY+velocityZ*velocityZ);
                //distance_result=distance_result+(velocity*velocity)/2*kalmanFilter_acceleration[1];
                //distance_result=distance_result+integral(kalmanFilter_acceleration[0])+integral(kalmanFilter_acceleration[1])+integral(kalmanFilter_acceleration[2]);
                distance_result+=velocity*time;
            }
            distance.setText(Double.toString(Math.abs(distance_result)));
            //distance.setText(Double.toString(calData(kalmanFilter_acceleration[0], kalmanFilter_acceleration[1], kalmanFilter_acceleration[2])));

            lastX=kalmanFilter_acceleration[0];
            lastY=kalmanFilter_acceleration[1];
            lastZ=kalmanFilter_acceleration[2];

            accValue[0].clear();
            accValue[1].clear();
            accValue[2].clear();

        }

        private double calData(double x, double y, double z){
            double temp = (x*x)+(y*y)+(z*z);
            return Math.sqrt(temp);
        }

        public double integral(double data){
            //velocity = velocity + time * data;
            //dist = dist + velocity * time;
            dist=time*time*data;
            return dist;
        }

        public boolean checkMove(double x, double y, double z){
            double speed=Math.abs(x+y+z-lastX-lastY-lastZ)/time;
            if(speed > 10)
                return true;
            return false;
        }
    }
}

