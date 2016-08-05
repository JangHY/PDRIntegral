package com.example.sohee.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class CurrentPoint{
	Bitmap bmp;//������ �̹���
	float xpos;//������ x��ǥ
	float ypos;//������ y��ǥ
	float vx;//�����ð� x�� �̵��Ÿ�
	float vy;//�����ð� y�� �̵��Ÿ�
	
	public CurrentPoint(Bitmap bitmap) {
		// TODO Auto-generated constructor stub
		bmp=bitmap;
		this.xpos=0;
		this.ypos=0;
		this.vx=0;
		this.vy=0;
		}

	public void paint(Canvas canvas,float x,float y){//��ǥ (x,y)��ġ�� �����̸� �׸�
		canvas.drawBitmap(bmp, x, y, null);
	}
	public float getXpos() {
		return xpos;
	}
	public void setXpos(float xpos) {
		this.xpos = xpos;
	}
	public float getYpos() {
		return ypos;
	}
	public void setYpos(float ypos) {
		this.ypos = ypos;
	}
	public float getVx() {
		return vx;
	}
	public void setVx(float vx) {
		this.vx = vx;
	}
	public float getVy() {
		return vy;
	}
	public void setVy(float vy) {
		this.vy = vy;
	}
}
