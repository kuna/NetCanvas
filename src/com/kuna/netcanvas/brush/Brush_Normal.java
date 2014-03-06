package com.kuna.netcanvas.brush;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;

public class Brush_Normal implements BrushModel {
	private Canvas c;
	private Paint p;
	private float bx, by;
	private float brushSize;
	private Path path;
	
	public Brush_Normal(Canvas c) {
		this.c = c;
		p = new Paint();
		p.setAntiAlias(true);
		p.setDither(true);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
		p.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
		p.setPathEffect(new CornerPathEffect(10) );   // set the path effect when they join.
		brushSize = 1;	// default
		path = new Path();
	}
	

	@Override
	public void draw_start(float x, float y) {
		bx = x;
		by = y;
		path.reset();
		path.moveTo(x, y);
		c.drawPath(path, p);
	}

	@Override
	public void draw_move(float x, float y) {
		path.quadTo(bx, by, (x+bx)/2, (y+by)/2);
		bx = x;
		by = y;
		c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		c.drawPath(path, p);
	}

	@Override
	public void draw_end(float x, float y) {
		path.lineTo(bx, by);
		c.drawPath(path, p);
		path.reset();
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
