package com.example.samsung.orientationsensor;

/**
 * Created by Hyon on 2016-07-13.
 */
public class KalmanFilter {
    //initial values for the kalman filter
    double x_est_last = 0;
    double P_last = 0;

//the noise in the system

    double Q = 0.022;
    double R = 0.617;
    double K;
    double P;
    double P_temp;
    double x_temp_est;
    double x_est;//칼만필터 적용된 값

    double z_measured;
    /*double frand()
    {
        return 2*((rand()/(double)RAND_MAX) - 0.5);
    }*/
    KalmanFilter(float initValue) {
        x_est_last = initValue;
        float sum_error_kalman = 0;
        float sum_error_measure = 0;
    }

    public double update(float measurement) {

//do a prediction
            x_temp_est = x_est_last;
            P_temp = P_last + Q;
//calculate the Kalman gain
            K = P_temp * (1.0/(P_temp + R));
//measure
            z_measured = measurement;
//the real measurement plus noise
//correct
            x_est = x_temp_est + K * (z_measured - x_temp_est); // x_est 가 칼만 필터 적옹한 값.
            P = (1- K) * P_temp;
//we have our new system
           /* printf("Ideal position: %6.3f \n",z_real);
        //http://sexy.pe.kr/tc/764  주소 이상한데 반올림 정보 있음
            printf("Mesaured position: %6.3f [diff:%.3f]\n",z_measured,Math.abs(z_real-z_measured));//fabs : 반환을 float형으로
            printf("Kalman position: %6.3f [diff:%.3f]\n",x_est,fabs(z_real - x_est));
            */
//update our last's
            P_last = P;
            x_est_last = x_est;
            return x_est;
        }
}
