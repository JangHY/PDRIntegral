package com.example.sohee.myapplication;

/**
 * Created by sohee on 16. 8. 5..
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.SurfaceHolder;

public class MyView extends android.view.SurfaceView implements SurfaceHolder.Callback{

    private String mapSrc;
    private Canvas canvas;
    private Bitmap floorMap;
    private MyThread thread;
    Bitmap currentPoint= BitmapFactory.decodeResource(getResources(), R.drawable.current_point);
    private CurrentPoint cp;

    private float initX,initY;

    private static boolean first=true;

    private boolean xFlag=false;
    private boolean yFlag=false;

    public MyView(Context context){
        super(context);
        this.initX=0;
        this.initY=0;
        cp=new CurrentPoint(currentPoint);//랜덤한 수만큼 눈송이 객체배열 생성

        SurfaceHolder holder = getHolder(); // 서피스 뷰의 홀더를 얻는다.
        holder.addCallback(this); // 콜백 메소드를 처리한다.
        thread = new MyThread(holder); // 스레드를 생성한다.


    }
    public MyView(Context context, String src,float x, float y) {
        // TODO Auto-generated constructor stub
        super(context);
        this.mapSrc = src;
        this.initX=x;
        this.initY=y;
        SurfaceHolder holder = getHolder(); // 서피스 뷰의 홀더를 얻는다.
        holder.addCallback(this); // 콜백 메소드를 처리한다.
        thread = new MyThread(holder); // 스레드를 생성한다.
        cp=new CurrentPoint(currentPoint);//랜덤한 수만큼 눈송이 객체배열 생성
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub

        thread.setRunning(true);
        thread.start();
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        thread.setSurfaceSize(width, height);//서피스가 변경
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        boolean retry = true;
        thread.setRunning(false); // 스레드를 중지시킨다.
        while (retry) {
            try { thread.join(); // 메인 스레드와 합친다.
                retry = false;
                break;
            } catch (InterruptedException e) {
                e.getStackTrace();
            }
        }
    }


    class MyThread extends Thread{//스레드를 돌리기위한 내부클래스
        private SurfaceHolder surfaceHolder;
        private boolean running;
        private boolean init;//초기화 �榮쩝� 확인
        private int canvasWidth, canvasHeight;
        private float x, y;//현위치 저장
        private float vx, vy;//사용자현재위치 속도를 제어
        private int widthR, heightR;

        public MyThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
            init=false;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public void run(){
            //floorMap=loadBitmap(mapSrc);
            //floorMap=Bitmap.createScaledBitmap(floorMap,canvasWidth,floorMap.getHeight()*canvasWidth/floorMap.getWidth(), true);
            widthR=canvasWidth;
            heightR=canvasHeight;
            //heightR=floorMap.getHeight()*canvasWidth/floorMap.getWidth();

            initialize();//초기화

            while(running){
                canvas = null;

                try {
                    canvas = surfaceHolder.lockCanvas(null);//캔버스를 가져온다

                    //canvas.drawBitmap(floorMap, 0,0, null);//배경 그리기

                    synchronized (surfaceHolder) {
                        this.doUpdate();//움직이는 눈송이

                        canvas.drawBitmap(currentPoint, x, y,null);
                        if(this.checkDirection(x, y)){
                            setRunning(false); //충돌했다면, 스레드를 중지시킨다.
                            break;
                        }

                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }

            }
        }

        public void setSurfaceSize(int width, int height){//화면크기를 얻어옴
            canvasWidth=width;
            canvasHeight=height;
        }

        public void doUpdate(){//vx, vy만큼씩 이동해주고 화면끝에 튕길경우 90도 반대방향으로 튕기게
            //눈송이 각각이 가지고 있는 x,y,vx,vy 값
            //initialize();
            x=cp.getXpos();
            vx=cp.getVx();
            y=cp.getYpos();
            vy=cp.getVy();

            float dstX=(float) 0.76*canvas.getWidth(); //입력받은 최종 목적지 x좌표
            //float dstY=(float) 0.79*floorMap.getHeight()*canvasWidth/floorMap.getWidth();; //입력받은 최종 목적지 y좌표
            float dstY=(float) 0.79*canvas.getHeight(); //입력받은 최종 목적지 y좌표


            if((first&&(Math.abs(dstX-x)>Math.abs(dstY-y)))||xFlag==true){//x축 방향으로 이동 , y값이 같은 비콘
                //현재 x위치와 가장 가까운 비콘의 위치까지 이동
                xFlag=true;
                first=false;
                if(dstX-x<0){//목적지가 출발지보다 왼쪽에 있을 때
                    vx=-1f;	//왼쪽으로 이동
                    x += vx;
                }
                else if(dstX-x>0){  //목적지가 출발지보다 오른쪽에 있을 때
                    vx=1f;	//오른쪽으로 이동
                    x += vx;
                }
                if(Math.abs(x-dstX)<1){
                    xFlag=false;
                }
            }
            else if((first&&(Math.abs(dstX-x)<Math.abs(dstY-y)))||xFlag==false){//y축 방향으로 이동
                first=false;
                if(dstY-y<0){ //위쪽으로 이동
                    vy=-1f;
                    y += vy;
                }
                else if(dstY-y>0){ //아래쪽으로 이동
                    vy=1f;
                    y += vy;
                }
                if(Math.abs(y-dstY)<1){
                    xFlag=true;
                }
            }

            //업데이트한 x,y,vx,vy값을 저장
            cp.setXpos(x);
            cp.setYpos(y);
            cp.setVx(vx);
            cp.setVy(vy);
        }

        public boolean checkDirection(float sX,float sY){//출발지점에서 목적지까지 최단거리로 가는 방향을 찾는다.
            float dstX=(float) 0.76*canvas.getWidth(); //입력받은 최종 목적지 x좌표
            //float dstY=(float) 0.79*floorMap.getHeight()*canvas.getWidth()/floorMap.getWidth(); //입력받은 최종 목적지 y좌표
            float dstY=(float) 0.79*canvas.getHeight();

            if((Math.abs(sX-dstX)<1)&&(Math.abs(sY-dstY)<1))
                return true;
            return false;
        }

        private void initialize() {//랜덤값으로 눈송이 속도, 위치 초기화
            if(init == false){
                //x=initX*canvasWidth;
                //y=initY*floorMap.getHeight()*canvasWidth/floorMap.getWidth();
                x=initX;
                y=initY;
                vx = 0;
                vy = 0;
                cp.setXpos(x*widthR);
                cp.setYpos(y*heightR);
                cp.setVx(vx);
                cp.setVy(vy);
                init = true;
            }
        }
    }
    public Bitmap loadBitmap(String url) {
        URL newurl = null;
        Bitmap bitmap = null;
        try {
            newurl = new URL(url);
            bitmap = BitmapFactory.decodeStream(newurl.openConnection()
                    .getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        return bitmap;
    }
}
