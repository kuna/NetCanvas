package com.kuna.netcanvas.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;

public class Brush_Rand implements BrushModel {
	private Canvas c;
	private Paint p;
	private float bx, by;
	private float brushSize;
	
	public Brush_Rand(Canvas c) {
		this.c = c;
		p = new Paint();
		p.setAntiAlias(true);
		p.setDither(true);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
		p.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
		p.setPathEffect(new CornerPathEffect(10) );   // set the path effect when they join.
		brushSize = 1;	// default
	}
	

	@Override
	public void draw_start(float x, float y) {
		p.setStrokeWidth((int)(brushSize * (Math.random()*0.5+0.5)));
		c.drawLine(x, y, x, y, p);
		bx = x;
		by = y;
	}

	@Override
	public void draw_move(float x, float y) {
		p.setStrokeWidth((int)(brushSize * (Math.random()*0.5+0.5)));
		c.drawLine(bx, by, x, y, p);
		bx = x;
		by = y;
	}

	@Override
	public void draw_end(float x, float y) {
		p.setStrokeWidth((int)(brushSize * (Math.random()*0.5+0.5)));
		c.drawLine(bx, by, x, y, p);
	}

	@Override
	public void setWidth(float size) {
		brushSize = size;
		p.setStrokeWidth(brushSize);
	}

	@Override
	public void setColor(int color) {
		p.setColor(color);
	}

}
