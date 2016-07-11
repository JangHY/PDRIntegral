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
    FileWriter write;
    PrintWriter out;
    File file;
    public FileTest(){
        SimpleDateFormat dateFormat = new  SimpleDateFormat("yyyyMMdd_HHMM", java.util.Locale.getDefault());
        Date date = new Date();
        String strDate = dateFormat.format(date);
        String path = Environment.getExternalStorageDirectory()+"/";
        String fileName = "sensor"+strDate+".txt";

        file = new File(path + fileName);
    }

    public void save(String strData){
        try{
            write = new FileWriter(file,true);
            out = new PrintWriter(write);
            out.print(strData);
            out.print(",");
            out.close();
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
