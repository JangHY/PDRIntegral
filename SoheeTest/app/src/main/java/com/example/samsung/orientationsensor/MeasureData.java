package com.example.samsung.orientationsensor;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

/**
 * Created by sohee on 16. 7. 26..
 */
public class MeasureData {
    // points from accelerometr
    private LinkedList accData;
    private LinkedList data;
    // timer interval of generating points
    private long interval;

    public MeasureData(long interval) {
        this.interval = interval;
        accData = new LinkedList ();
        data = new LinkedList ();
    }

    public void addPoint(Point p){
        accData.add(p);
    }

    public void process(){

        for(int i = 0; i < accData.size(); ++i){
            Point p = (Point) accData.get(i);
            float speed = 0;

            if(i > 0){
                speed = data.get(i-1).getSpeedAfter();
            }
            data.add(new MeasurePoint(p.getX(), p.getY(), p.getZ(), speed, interval, getAveragePoint()));
        }
    }

    public boolean saveExt(Context con, String fname) throws Throwable {

        try {

            File file = new File(con.getExternalFilesDir(null), fname);
            FileOutputStream os = new FileOutputStream(file);
            OutputStreamWriter out = new OutputStreamWriter(os);


            for (int i = 0; i < data.size(); ++i) {
                MeasurePoint m = (MeasurePoint) data.get(i);
                out.write(m.getStoreString());
            }

            out.close();
        } catch (Throwable t) {
            throw (t);
        }
        return true;
    }

    private Point getAveragePoint() {
        float x = 0;
        float y = 0;
        float z = 0;

        for(int i = 0; i < accData.size(); ++i){
            Point p = (Point) accData.get(i);
            x += p.getX();
            y += p.getY();
            z += p.getZ();
        }

        return new Point(x, y, z, 1);
    }

    public float getLastSpeed(){
        return data.getLast().getSpeedAfter();
    }

    public float getLastSpeedKm(){
        float ms = getLastSpeed();
        return ms*3.6f;
    }
}