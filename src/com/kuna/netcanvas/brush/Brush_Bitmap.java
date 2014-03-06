package com.kuna.netcanvas.brush;

import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class Brush_Bitmap implements BrushModel {


	private int brushWidth;
	private Paint p;
	private Canvas c;
	private Bitmap brushBitmap;
	
	private float lastx, lasty;
	private RectF r;
	private int wid, hei;
	private int defSize;
	private float ratio = 1;
	
	private double dist;
	private double angle;
	
	public Brush_Bitmap(Canvas c, Bitmap b) {
		this.c = c;
		brushBitmap = b;
		// get default diameter of bitmap
		wid = b.getWidth();
		hei = b.getHeight();
		defSize = (wid>hei)?wid:hei;
		
		p = new Paint();
	}
	
	@Override
	public void draw_start(float x, float y) {
		r = new RectF(x-wid*ratio/2, y-hei*ratio/2, x+wid*ratio/2, y+hei*ratio/2);
		c.drawBitmap(brushBitmap, null, r, p);
		lastx = x;
		lasty = y;
	}

	@Override
	public void draw_move(float x, float y) {
		dist = (int) Math.sqrt(Math.pow(x-lastx, 2) + Math.pow(y-lasty, 2));
		angle = Math.atan2(x-lastx, y-lasty);
		
		float nx = lastx, ny = lasty;
		for (int i=1; i<=dist; i++) {
			nx = (float) (lastx + i*Math.sin(angle));
			ny = (float) (lasty + i*Math.cos(angle));
			r = new RectF(nx-wid*ratio/2, ny-hei*ratio/2, nx+wid*ratio/2, ny+hei*ratio/2);
			c.drawBitmap(brushBitmap, null, r, p);
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
		ratio = (float)size / defSize;
	}

	@Override
	public void setColor(int color) {
		p.setColor(color);
		// removeColor is the color that will be replaced with the pain't color
		// 0 is the tolerance (in this case, only the color to be removed is targetted)
		// Mode.TARGET means pixels with color the same as removeColor are drawn on
		p.setXfermode(new AvoidXfermode(color, 0, AvoidXfermode.Mode.TARGET));
	}

}
