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
    File fileOriginal;
    File fileGravity;
    File fileKalman;
    public FileTest(){
        /*SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyyMMdd_HHMM", java.util.Locale.getDefault());
        Date date = new Date();
        String strDate = dateFormat.format(date);
        */
        String path = Environment.getExternalStorageDirectory()+"/";
        String fileName = "sensor_original.csv";
        fileOriginal = new File(path + fileName);

        fileName = "sensor_gravity.csv";
        fileGravity = new File(path + fileName);

        fileName = "sensor_kalman.csv";
        fileKalman = new File(path + fileName);
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
            outKalman.print(originData);
            outKalman.print(",");
            outKalman.close();

            Log.d("finish","저장완료");
        }
        catch(Exception e){
            e.printStackTrace();
        }

/*
        try{
            fos = new FileOutputStream(file);
            fos.write((strData).getBytes());
            fos.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }*/
    }
}
