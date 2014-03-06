package com.kuna.netcanvas.brush;

import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

public class Eraser_Normal implements EraserModel {
	// just draws line with CLEAR mode
	
	private Canvas c;
	private Paint p;
	private float bx, by;
	
	public Eraser_Normal(Canvas c) {
		p = new Paint();
		p.setAntiAlias(true);
		p.setDither(true);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
		p.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
		p.setPathEffect(new CornerPathEffect(10) );   // set the path effect when they join.
		p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		
		this.c = c;
	}

	@Override
	public void draw_start(float x, float y) {
		c.drawLine(x, y, x, y, p);
		bx = x;
		by = y;
	}

	@Override
	public void draw_move(float x, float y) {
		c.drawLine(bx, by, x, y, p);
		bx = x;
		by = y;
	}

	@Override
	public void draw_end(float x, float y) {
		c.drawLine(bx, by, x, y, p);
	}

	@Override
	public void setWidth(float size) {
		p.setStrokeWidth(size);
	}
}
