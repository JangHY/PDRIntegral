package com.example.samsung.orientationsensor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Hyon on 2016-05-02.
 */
public class FileTest {
    private final int MY_PERMISSION_REQUEST_STORAGE = 100;

    FileOutputStream fos = null;
    FileWriter writeOriginal;
    PrintWriter outOriginal;
    FileWriter writeGravity;
    PrintWriter outGravity;
    FileWriter writeKalman;
    PrintWriter outKalman;
    FileWriter writeAverage;
    PrintWriter outAverage;
    File fileOriginal;
    File fileGravity;
    File fileKalman;
    File fileAverage;
    public FileTest(){
        /*SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyyMMdd_HHMM", java.util.Locale.getDefault());
        Date date = new Date();
        String strDate = dateFormat.format(date);
        */

        String path = Environment.getExternalStorageDirectory()+"/";
        String fileName = "sensor_original.csv";

        fileOriginal = new File(path + fileName);
        if (fileOriginal.exists()) {
            fileOriginal.delete();
        }
        fileOriginal = new File(path + fileName);

        fileName = "sensor_gravity.csv";
        fileGravity = new File(path + fileName);
        if (fileGravity.exists()) {
            fileGravity.delete();
        }
        fileGravity = new File(path + fileName);

        fileName = "sensor_kalman.csv";
        fileKalman = new File(path + fileName);
        if (fileKalman.exists()) {
            fileKalman.delete();
        }
        fileKalman = new File(path + fileName);

        fileName = "sensor_average.csv";
        fileAverage = new File(path + fileName);
        if (fileAverage.exists()) {
            fileAverage.delete();
        }
        fileAverage = new File(path + fileName);

        try{
            writeOriginal = new FileWriter(fileOriginal,true);
            outOriginal = new PrintWriter(writeOriginal);
            outOriginal.print("originData");
            outOriginal.print(",");
            outOriginal.close();

            writeGravity = new FileWriter(fileGravity,true);
            outGravity = new PrintWriter(writeGravity);
            outGravity.print("gravityData");
            outGravity.print(",");
            outGravity.close();

            writeKalman = new FileWriter(fileKalman,true);
            outKalman = new PrintWriter(writeKalman);
            outKalman.print("kalmanData");
            outKalman.print(",");
            outKalman.close();

            writeAverage = new FileWriter(fileAverage,true);
            outAverage = new PrintWriter(writeAverage);
            outAverage.print("averageData");
            outAverage.print(",");
            outAverage.close();

//            Log.d("finish","저장완료");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void save(String originData, String gravityData, String kalmanData){
        try{
            writeOriginal = new FileWriter(fileOriginal,true);
            outOriginal = new PrintWriter(writeOriginal);
            outOriginal.print(originData);
            outOriginal.print(",");
            outOriginal.close();

            writeGravity = new FileWriter(fileGravity,true);
            outGravity = new PrintWriter(writeGravity);
            outGravity.print(gravityData);
            outGravity.print(",");
            outGravity.close();

            writeKalman = new FileWriter(fileKalman,true);
            outKalman = new PrintWriter(writeKalman);
            outKalman.print(kalmanData);
            outKalman.print(",");
            outKalman.close();

//            Log.d("finish","저장완료");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void save(String averageData, String kalmanData){
        try{
            writeAverage = new FileWriter(fileAverage,true);
            outAverage = new PrintWriter(writeAverage);
            outAverage.print(averageData);
            outAverage.print(",");
            outAverage.close();

            writeKalman = new FileWriter(fileKalman,true);
            outKalman = new PrintWriter(writeKalman);
            outKalman.print(kalmanData);
            outKalman.print(",");
            outKalman.close();

//            Log.d("finish","저장완료");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void save(String originData){
        try{
            writeOriginal = new FileWriter(fileOriginal,true);
            outOriginal = new PrintWriter(writeOriginal);
            outOriginal.print(originData);
            outOriginal.print(",");
            outOriginal.close();

//            Log.d("finish","저장완료");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
