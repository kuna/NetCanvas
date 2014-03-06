package com.kuna.netcanvas;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.TooManyListenersException;

import com.kuna.netcanvas.DrawView.History;
import com.kuna.netcanvas.brush.BrushModel;
import com.kuna.netcanvas.brush.Brush_Bitmap;
import com.kuna.netcanvas.brush.Brush_Normal;
import com.kuna.netcanvas.brush.Brush_Rand;
import com.kuna.netcanvas.brush.Brush_Round;
import com.kuna.netcanvas.brush.EraserModel;
import com.kuna.netcanvas.brush.Eraser_Normal;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

/***
 * 
 * @author kuna
 * need to add - Screen rotation (UI changing)
 * 
4. 레이어 생긴거 제대로 되도록 고치기 (레이어 지원시키기)
5. 이미지브러시 제대로 나오도록 하기 및 알고리즘 구현 (브러시 종류 일단 3가지로 만들기.)
 - http://www.tricedesigns.com/2012/01/04/sketching-with-html5-canvas-and-brush-images/
http://stackoverflow.com/questions/16650419/draw-in-canvas-by-finger-android
http://perfectionkills.com/exploring-canvas-drawing-techniques/
 *
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class DrawView extends View {
	// default
	int wHei, wWid;
	int cWid, cHei;
	private Context c;
	
	// default drawing option
	Paint mp_layer;
	Bitmap b_layer[] = new Bitmap[config.MAXLAYER];
	Canvas c_layer[] = new Canvas[config.MAXLAYER];
	int m_layercnt = 1;
	int m_layerind = 0;
	public static int m_brushind = 0;
	public static int m_brushClr = Color.BLACK;
	int m_brushWid = 3;
	int m_brushAlpha = 150;
	
	Bitmap bTemp;
	Canvas cTemp;
	
	// drawing
	Path m_path = null;
	
	// network setting
	int isNetwork = 0;
	int ownLayer[] = new int[config.MAXNETLAYER];
	
	// multitouch
	int isTouched = 0;
	int isStartedDrawing = 0;
	int TouchIndex = 0;
	int TouchIndex2 = 0;
	PointF m_p;
	float fZoomLen;
	PointF fPosTmp;
	
	// zoom & pos
	float fZoom = 1;	// zoom
	float fOrgZoom = 1;
	PointF fPos;		// offset
	
	// menus
	boolean isMore = false;
	boolean isLayer = false;
	int selMenu = -1;
	int selCtrl = -1;
	int selTool = 0;	// 0: brush, 1: eraser
	
	// resource
	Bitmap b_bgBtm;
	Bitmap b_bgUp;
	Bitmap b_btnUndo;
	Bitmap b_btnRedo;
	Bitmap b_btnBrush;
	Bitmap b_btnColor;
	Bitmap b_btnEraser;
	Bitmap b_btnMore;
	Bitmap b_btnSave;
	Bitmap b_btnLayer;
	Bitmap b_sBrush;
	Bitmap b_sTrans;
	
	// brush / erasers
	BrushModel brush;
	EraserModel eraser;
	
	// brush bitmap
	Bitmap b_texture;
	
	// dialog
	BrushDialog dialog_brush;
	ColorPickerDialog dialog_color;
	
	// history / undo / redo
	public class History {
		int layerNum;
		Bitmap layerData;
		
		public void applyHistory() {
			if (b_layer[layerNum] != null) {
				b_layer[layerNum].recycle();
				b_layer[layerNum] = layerData.copy(Bitmap.Config.ARGB_8888, true);
				c_layer[layerNum] = new Canvas(b_layer[layerNum]);
				m_layerind = layerNum;
			}
		}
		
		public void destory() {
			if (layerData != null) {
				layerData.recycle();
			}
		}
	}
	LinkedList<History> q_history = new LinkedList<History>();
	LinkedList<History> q_redo = new LinkedList<History>();
	
	public DrawView(Context context, int width, int height, int layers) {
		super(context);
		InitView(context, width, height, layers);
	}

	public DrawView(Context context, int width, int height, int layers, String ip) {
		super(context);
		
		// create default view
		InitView(context, width, height, layers);
		
		// set network settings
	}
	
	public void InitView(Context context, int width, int height, int layers) {
		// default
		c = context;
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		wWid = display.getWidth();
		wHei = display.getHeight();
		fPos = new PointF();
		fPos.set((wWid-width)/2, (wHei-height)/2);
		cWid = width;
		cHei = height;
		
		// init draw
		m_path = new Path();
		
		// make new palettes (layer)
		m_layercnt = layers;
		for (int i=0; i<m_layercnt; i++) {
			b_layer[i] = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			c_layer[i] = new Canvas(b_layer[i]);
		}
		bTemp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		cTemp = new Canvas(bTemp);

		// for layer alpha
		mp_layer = new Paint();
		
		// make multitouch init
		
		// load resource
		b_bgBtm = BitmapFactory.decodeResource(getResources(), R.drawable.bg_bottom);
		b_bgUp = BitmapFactory.decodeResource(getResources(), R.drawable.bg_up);
		b_btnUndo = BitmapFactory.decodeResource(getResources(), R.drawable.undo);
		b_btnRedo = BitmapFactory.decodeResource(getResources(), R.drawable.redo);
		b_btnBrush = BitmapFactory.decodeResource(getResources(), R.drawable.brush);
		b_btnColor = BitmapFactory.decodeResource(getResources(), R.drawable.color);
		b_btnEraser = BitmapFactory.decodeResource(getResources(), R.drawable.eraser);
		b_btnMore = BitmapFactory.decodeResource(getResources(), R.drawable.more_small);
		b_btnSave = BitmapFactory.decodeResource(getResources(), R.drawable.save_small);
		b_btnLayer = BitmapFactory.decodeResource(getResources(), R.drawable.layer_small);
		b_sBrush = BitmapFactory.decodeResource(getResources(), R.drawable.brush_small);
		b_sTrans = BitmapFactory.decodeResource(getResources(), R.drawable.trans_small);
		
		// load bitmap brush
		b_texture = BitmapFactory.decodeResource(getResources(), R.drawable.brush_star);
		
		// init dialog
		dialog_brush = new BrushDialog(context, m_brushind);
		dialog_color = new ColorPickerDialog(context, m_brushClr);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		/***
		 * Draw Canvas
		 */
		
		// draw undrawable area first
		canvas.drawColor(Color.GRAY);
		
		// draw drawable area
		Rect r = new Rect((int)fPos.x, (int)fPos.y, (int)(fPos.x+cWid*fZoom), (int)(fPos.y+cHei*fZoom));
		Paint mp_r = new Paint();
		mp_r.setColor(Color.WHITE);
		canvas.drawRect(r, mp_r);
		
		// draw layers
		for (int i=0; i<m_layercnt; i++) {
			canvas.drawBitmap(b_layer[i], null, r, null);
			if (m_layerind == i) {
				// draw temp path
				mp_layer.setAlpha(m_brushAlpha);
				canvas.drawBitmap(bTemp, null, r, mp_layer);
			}
		}
		
		/***
		 * Draw Menus
		 */		
		if (isLayer) {
			canvas.drawBitmap(b_bgUp, null, new Rect(0, 0, wWid, 340), null);
			
			// draw layers
			int x = 30;
			for (int i = 0; i<m_layercnt; i++) {
				int nLayer = (i+m_layerind) % m_layercnt;
				Paint bLayer = new Paint();
				Paint bRect = new Paint();
				bRect.setStyle(Paint.Style.STROKE);
				bRect.setColor(Color.BLACK);
				bRect.setStrokeWidth(1);
				Rect rArea = new Rect(x, 90, x+160, 290);
				Rect rSelArea = null;
				if (i == 0) {
					bLayer.setAlpha(200);
					bRect.setAlpha(200);
					
					// draw visible area to red rect
					rSelArea = new Rect((int)(x +80-80*fZoom),
							(int)(90 +100-100*fZoom),
							(int)(x+160 +80+80*fZoom), 
							(int)(290 +100+100*fZoom));
					if (rSelArea.left < rArea.left) rSelArea.left = rArea.left; 
					if (rSelArea.right > rArea.right) rSelArea.right = rArea.right; 
					if (rSelArea.top < rArea.top) rSelArea.top = rArea.top; 
					if (rSelArea.bottom > rArea.bottom) rSelArea.bottom = rArea.bottom; 
				} else {
					bLayer.setAlpha(100);
					bRect.setAlpha(100);
				}
				
				canvas.drawBitmap(b_layer[nLayer], null, rArea, bLayer);
				canvas.drawRect(rArea, bRect);
				if (rSelArea != null) {
					bRect.setStrokeWidth(3);
					bRect.setColor(Color.RED);
					canvas.drawRect(rSelArea, bRect);
				}
				
				x += 160 + 50;
			}
		}

		if (isMore) {
			canvas.drawBitmap(b_bgBtm, null, new Rect(0, wHei-232, wWid, wHei), null);
			canvas.drawBitmap(b_btnUndo, null, new Rect(wWid/8*1-40, wHei-200, wWid/8*1+40, wHei-120), null);
			canvas.drawBitmap(b_btnBrush, null, new Rect(wWid/8*3-40, wHei-200, wWid/8*3+40, wHei-120), null);
			canvas.drawBitmap(b_btnEraser, null, new Rect(wWid/8*5-40, wHei-200, wWid/8*5+40, wHei-120), null);
			canvas.drawBitmap(b_btnColor, null, new Rect(wWid/8*7-40, wHei-200, wWid/8*7+40, wHei-120), null);
			Paint bClr = new Paint();
			bClr.setColor(m_brushClr);
			canvas.drawRect(new Rect(wWid/4*3 + 20, wHei-80, wWid/4*3 + 100, wHei-60), bClr);// add brush color rect
			
			// draw transparency & width
			canvas.drawBitmap(b_sBrush, 60, wHei-380, null);
			canvas.drawBitmap(b_sTrans, 60, wHei-310, null);
			Paint bline = new Paint();
			bline.setStrokeWidth(3);
			bline.setColor(Color.BLACK);
			bline.setAlpha(50);
			canvas.drawLine(130, wHei-370, wWid-60, wHei-370, bline);
			canvas.drawLine(130, wHei-300, wWid-60, wHei-300, bline);
			bline.setAlpha(150);
			canvas.drawLine((wWid-250)*((float)m_brushWid/100)+ 130, wHei-370, (wWid-250)*((float)m_brushWid/100)+ 190, wHei-370, bline);
			canvas.drawLine((wWid-250)*((float)m_brushAlpha/255)+ 130, wHei-300, (wWid-250)*((float)m_brushAlpha/255)+ 190, wHei-300, bline);
			
			// draw default menu
			canvas.drawBitmap(b_btnLayer, 20, 20, null);
			canvas.drawBitmap(b_btnSave, wWid - 160, 20, null);
		}
		
		// draw default menus
		canvas.drawBitmap(b_btnMore, wWid - 80, 20, null);
		
		super.onDraw(canvas);
	}

	
	// Add: if canceled once, then dont respond to any event (zooming mode can cause error)
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    // get pointer index from the event object
	    int pointerIndex = event.getActionIndex();
	
	    // get pointer ID
	    int pointerId = event.getPointerId(pointerIndex);
	
	    // get masked (not specific to a pointer) action
	    int maskedAction = event.getActionMasked();
	    
	    // get pointer count
	    int ptCnt = event.getPointerCount();
	    
    	PointF f = new PointF();
    	f.x = event.getX(pointerIndex);
    	f.y = event.getY(pointerIndex);
	    switch (maskedAction) {
		    case MotionEvent.ACTION_DOWN:
		    case MotionEvent.ACTION_POINTER_DOWN: {
		    	// if menu area? --> turn in menu mode
		    	// if drawing area? --> turn in drawing mode
		    	// if (startdrawing=0,touched=0) -> add it to 
		    	// (startdrawing=0,touched>0) -> change touch mode
		    	// (else - drawing1,touch>1) -> dont update more touch point 
	
		    	if (ptCnt == 1) {
		    		if ( f.x > wWid-100 && f.y < 80 ) {		// <more> button
		    			isMore = !isMore;
		    			if (isMore == false)
		    				isLayer = false;
		    		} else {
		    			boolean drawingmode = true;
		    			if (isMore) {						// need more examation...
		    				if (f.x < 100 && f.y < 80) {		// layer button
		    					isLayer = !isLayer;
		    					drawingmode = false;
		    				}
		    				if (f.x > wWid - 180 && f.x < wWid - 100 && f.y < 80) {		// save button
		    					Log.v("MENU", "SAVE");
		    					drawingmode = false;
		    				}
		    				if (f.x > 0 && f.x < wWid/4*1 && f.y > wHei-232) {	// undo (1)
		    					Log.v("MENU", "UNDO");
		    					selMenu = 0;
		    					drawingmode = false;
		    				}
		    				if (f.x > wWid/4*1 && f.x < wWid/4*2 && f.y > wHei-232) {	// brush (2)
		    					Log.v("MENU", "BRUSH");
		    					selMenu = 1;
		    					drawingmode = false;
		    				}
		    				if (f.x > wWid/4*2 && f.x < wWid/4*3 && f.y > wHei-232) {	// erase (3)
		    					Log.v("MENU", "ERASE");
		    					selMenu = 2;
		    					drawingmode = false;
		    				}
		    				if (f.x > wWid/4*3 && f.x < wWid && f.y > wHei-232) {	// colorpick (4)
		    					Log.v("MENU", "COLORPICK");
		    					selMenu = 3;
		    					drawingmode = false;
		    				}
		    				if (f.y > wHei - 440 && f.y < wHei - 360) {
		    					// brush size control
		    					selCtrl = 0;
		    					drawingmode = false;
		    				}
		    				if (f.y > wHei - 360 && f.y < wHei - 280) {
		    					// opacity control
		    					selCtrl = 1;
		    					drawingmode = false;
		    				}
		    			}
		    			
		    			if (isLayer) {
		    				if (f.y > 50 && f.y < 240) {	// layer switch
		    					Log.v("MENU", "LAYER SWITCH");
		    					m_layerind++;
		    					if (m_layerind >= m_layercnt)
		    						m_layerind = 0;
		    					drawingmode = false;
		    				}
		    			}
		    			
		    			if (drawingmode) {
				    		TouchIndex = pointerId;	// save pointer id
				    		Log.i("START", Integer.toString(TouchIndex));
				    		isStartedDrawing = 1;
			    	    	m_p = f;
			    	    	switch (selTool) {
			    	    	case 0:
				    	    	draw_start(f.x, f.y);
			    	    		break;
			    	    	case 1:
				    	    	erase_start(f.x, f.y);
			    	    		break;
			    	    	}
		    			}
		    		}
		    	} else {
	    			if (ptCnt == 2 && isStartedDrawing == 1) {
	    				// turn off drawing
	    				isStartedDrawing = 0;
	    				
	    				// save org zoom size
	    				fOrgZoom = fZoom;
	    				
	    				// get first zoomsize
	    				fZoomLen = getDist(f, m_p);
			    		Log.v("ZOOMSTART", Float.toString(fZoomLen));
			    		
			    		// set zoomer index
			    		TouchIndex2 = pointerId;
			    		
			    		// set canvas move point
			    		fPosTmp = new PointF((m_p.x+f.x)/2, (m_p.y+f.y)/2);
		    	    	m_p = f;
	    			}
		    	}

	    		invalidate();
		    	break;
		    }
		    case MotionEvent.ACTION_MOVE: { // a pointer was moved
		    	if (ptCnt == 1) {	// draw only when 1 touch mode
		    		if (isStartedDrawing == 1) {
		    			switch (selTool) {
		    			case 0:
			    	    	draw_move(f.x, f.y);
		    				break;
		    			case 1:
		    				erase_move(f.x, f.y);
		    				break;
		    			}
		    	    	m_p = f;
			    		invalidate();
		    		} else {
		    			// controller part
		    			if (selCtrl >= 0) {
		    				float ctrlVal = (f.x - 130) / (wWid - 190);
		    				if (ctrlVal < 0) ctrlVal = 0;
		    				if (ctrlVal > 1) ctrlVal = 1;
		    				
		    				switch (selCtrl) {
		    				case 0:	// brush size
		    					m_brushWid = (int) (ctrlVal*100);
		    					break;
		    				case 1:	// opac
		    					m_brushAlpha = (int) (ctrlVal * 255);
		    					break;
		    				}
		    			}
		    			invalidate();
		    		}
		    	} else if (ptCnt == 2) {
		    		// zoom mode
		    		if (TouchIndex == pointerId) {
		    			// move screen
			    		PointF fPosTmp2 = new PointF((m_p.x+f.x)/2, (m_p.y+f.y)/2);
		    	    	m_p = f;
		    			fPos.x += fPosTmp2.x-fPosTmp.x;
		    			fPos.y += fPosTmp2.y-fPosTmp.y;
		    			fPosTmp = fPosTmp2;
		    	    	
		    			// calculate zoom size
		    	    	PointF bf = new PointF();
		    	    	bf.x = event.getX(TouchIndex2);
		    	    	bf.y = event.getY(TouchIndex2);
		    			fZoom = fOrgZoom * getDist(f, bf) / fZoomLen;
			    		Log.v("ZOOM", Float.toString(fZoom));
		    		}
		    		invalidate();
		    	}
		    	break;
		    }
		    case MotionEvent.ACTION_UP:
		    case MotionEvent.ACTION_POINTER_UP:
		    case MotionEvent.ACTION_CANCEL: {
		    	if (selMenu >= 0) {
		    		if (f.y < wHei-232) {
		    			// open custom menu
				    	switch (selMenu) {
				    	case 0:		// UNDO
				    		doRedo();
				    		break;
				    	case 1:		// BRUSH
				    		Log.v("DIALOG", "BRUSH DLG");
				    		openBrushSelectDialog();
				    		break;
				    	case 2:		// ERASE
				    		break;
				    	case 3:		// COLORPICK
				    		Log.v("DIALOG", "COLOR DLG");
				    		openColorDialog();
				    		break;
				    	}
		    		} else {
		    			// switch tool
				    	switch (selMenu) {
				    	case 0:		// UNDO
				    		Log.v("MENU", "SELECT UNDO");
				    		doUndo();
				    		break;
				    	case 1:		// BRUSH
				    		Log.v("MENU", "SELECT BRUSH");
				    		selTool = 0;
				    		break;
				    	case 2:		// ERASE
				    		Log.v("MENU", "SELECT ERASE");
				    		selTool = 1;
				    		break;
				    	case 3:		// COLORPICK
				    		Log.v("MENU", "SELECT COLORPICK");
				    		break;
				    	}
		    		}
		    		
			    	// init selmenu
			    	selMenu = -1;
		    	}
		    	
		    	if (selCtrl >= 0) {
			    	// init selctrl
			    	selCtrl = -1;
		    	}
		    	
		    	if (isStartedDrawing == 1) {
		    		switch (selTool) {
		    		case 0:
			    		draw_end(f.x, f.y);
			    		break;
		    		case 1:
		    			erase_end(f.x, f.y);
		    			break;
		    		}
		    		
		    		isStartedDrawing = 0;
		    	}
		    	
		    	invalidate();
		    	break;
		    }
	    }
	    
	    return true;

		// a finger: draw
		// two fingers: zoom
		// three fingers: move
		
		//return super.onTouchEvent(event);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
	}

	
	//
	public void draw_start(float x, float y) {
		// locate brush before start
		switch (m_brushind) {
		case 0:		// rnd
			brush = new Brush_Rand(cTemp);
			break;
		case 1:		// normal
			brush = new Brush_Normal(cTemp);
			break;
		case 2:		// round gradient
			brush = new Brush_Round(cTemp);
			break;
		case 3:		// bitmap
			brush = new Brush_Bitmap(cTemp, b_texture);
			break;
		}
		
		if (brush != null) {
			// add canvas state to UNDO (bitmap, layernum)
			addHistory();
			
			// set color and width before drawing
			brush.setColor(m_brushClr);
			brush.setWidth(m_brushWid);
			
			// draw path
			brush.draw_start(convX(x), convY(y));
		}
	}
	
	public void draw_move(float x, float y) {
		brush.draw_move(convX(x), convY(y));
	}
	
	public void draw_end(float x, float y) {
		brush.draw_end(convX(x), convY(y));
		
		// apply canvas layer
		mp_layer.setAlpha(m_brushAlpha);
		c_layer[m_layerind].drawBitmap(bTemp, 0, 0, mp_layer);
		
		// clean temp layer
		cTemp.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
	}
	
	public void erase_start(float x, float y) {
		addHistory();
		
		eraser = new Eraser_Normal(c_layer[m_layerind]);
		eraser.setWidth(m_brushWid);
		eraser.draw_start(convX(x), convY(y));
	}
	
	public void erase_move(float x, float y) {
		eraser.draw_move(convX(x), convY(y));
	}
	
	public void erase_end(float x, float y) {
		eraser.draw_end(convX(x), convY(y));
	}
	
	public void openBrushSelectDialog() {
		dialog_brush.show();
	}
	
	public void openColorDialog() {
		dialog_color.show();
	}
	
	// convert pos to draw on bitmap
	public float convX(float x) {
		return (x-fPos.x) / fZoom;
	}
	
	public float convY(float y) {
		return (y-fPos.y) / fZoom;
	}
	
	public PointF convPoint(PointF p) {
		return new PointF(convX(p.x), convY(p.y));
	}
	
	public float getDist(PointF a, PointF b) {
		return (float)Math.sqrt( Math.pow(a.x-b.x, 2) + Math.pow(a.y-b.y, 2) );
	}
	
	public void addHistory() {
		if (q_history.size() >= config.MAXHISTORY) q_history.pollLast();
		if (q_redo.size() > 0) q_redo.clear();
		History h = new History();
		h.layerNum = m_layerind;
		h.layerData = b_layer[m_layerind].copy(Bitmap.Config.ARGB_8888, true);
		q_history.push(h);
	}
	
	public void doUndo() {
		if (q_history.size() > 0) {
			History h = q_history.pop();
			h.applyHistory();
			q_redo.push(h);
		}
		
		invalidate();
	}
	
	public void doRedo() {
		if (q_redo.size() > 0) {
			History h = q_redo.pop();
			h.applyHistory();
			q_history.push(h);
		}
		
		invalidate();
	}
}
