package com.kuna.netcanvas;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class BrushDialog extends Dialog {
	public int brushindex;
	
	private RadioGroup m_rgBrush;
	private Button m_bOK;
	private Dialog c;
	
	public BrushDialog(Context context, int nowIndex) {
		super(context);
		// TODO Auto-generated constructor stub
		
		brushindex = nowIndex;
		c = this;
		
		setContentView(R.layout.dialog_brush);
		
		m_rgBrush = (RadioGroup) findViewById(R.id.rg_brush);
		m_bOK = (Button) findViewById(R.id.btn_ok);
		
		m_rgBrush.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radio0:
					brushindex = 0;
					break;
				case R.id.radio1:
					brushindex = 1;
					break;
				case R.id.radio2:
					brushindex = 2;
					break;
				}
			}
		});
		
		m_bOK.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DrawView.m_brushind = brushindex;
				c.dismiss();
			}
		} );
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();    
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
	}
}
