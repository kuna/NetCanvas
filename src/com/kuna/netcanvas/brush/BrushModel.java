package com.kuna.netcanvas.brush;

public interface BrushModel {
	public void draw_start(float x, float y);
	public void draw_move(float x, float y);
	public void draw_end(float x, float y);
	public void setWidth(float size);
	public void setColor(int color);
}
