package com.kuna.netcanvas.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.util.Log;

public class Brush_Round implements BrushModel {

	private float brushWidth;
	private Paint p;
	private Canvas c;
	
	private float lastx, lasty;
	private double dist, angle;
	private int clr;
	
	private int[] mColors;
	
	public Brush_Round(Canvas c) {
		p = new Paint();
		p.setStyle(Paint.Style.FILL);
		//p.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
		
		this.c = c;
		brushWidth = 1;	//default size
	}

	@Override
	public void draw_start(float x, float y) {
	    // Create a radial gradient
	    RadialGradient gradient = new android.graphics.RadialGradient(
	    		x, y,
	    		brushWidth/2, mColors, null,
	            TileMode.CLAMP);
		p.setShader(gradient);
		c.drawCircle(x, y, brushWidth/2, p);
		lastx = x;
		lasty = y;
	}

	@Override
	public void draw_move(float x, float y) {
		dist = (int) Math.sqrt(Math.pow(x-lastx, 2) + Math.pow(y-lasty, 2));
		angle = Math.atan2(x-lastx, y-lasty);
		
		float nx = lastx, ny = lasty;
		for (int i=1; i<=dist; i+=(int)(brushWidth/10)+1) {
			nx = (float) (lastx + i*Math.sin(angle));
			ny = (float) (lasty + i*Math.cos(angle));
		    RadialGradient gradient = new android.graphics.RadialGradient(
		    		nx, ny,
		    		brushWidth/2, mColors, null,
		            TileMode.CLAMP);
			p.setShader(gradient);
			c.drawCircle(nx, ny, brushWidth/2, p);
		}
		
		lastx = nx;
		lasty = ny;
	}

	@Override
	public void draw_end(float x, float y) {
		// just an end...
		draw_move(x,y);
	}

	@Override
	public void setWidth(float size) {
		brushWidth = size;
		
	}

	@Override
	public void setColor(int color) {
		clr = color;
		
		mColors = new int[] {Color.argb(255, Color.red(clr), Color.green(clr), Color.blue(clr)),
				Color.argb(64, Color.red(clr), Color.green(clr), Color.blue(clr)),
				Color.argb(32, Color.red(clr), Color.green(clr), Color.blue(clr)),
				Color.argb(0, Color.red(clr), Color.green(clr), Color.blue(clr))};
	}

}
