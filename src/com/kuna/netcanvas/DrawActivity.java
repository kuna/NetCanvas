package com.kuna.netcanvas;

import android.app.Activity;
import android.os.Bundle;

public class DrawActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DrawView vw = new DrawView(this, 400, 300, 3);
		setContentView(vw);
	}
	
}
