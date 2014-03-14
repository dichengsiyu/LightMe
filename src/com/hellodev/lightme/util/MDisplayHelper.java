package com.hellodev.lightme.util;

import android.content.Context;
import android.util.DisplayMetrics;

import com.hellodev.lightme.FlashApp;

public class MDisplayHelper {
	
	private DisplayMetrics displayMetrics;
	private int screenWidth = 0;
	private float density = 0;

	public MDisplayHelper() {
		Context context = FlashApp.getContext();
		displayMetrics = context.getResources().getDisplayMetrics();
	}
	
	public DisplayMetrics getDisplayMetrics() {
		return displayMetrics;
	}
	
	public int getScreenWidth() {
		return displayMetrics.widthPixels;
	}
	
	public int getScreenHeight() {
		return displayMetrics.widthPixels;
	}
	
	public float getDensity() {
		if(density == 0)
			density = displayMetrics.density;
		return density;
	}
	
	public int dpiToPx(float dip) {
		return (int)(dip * getDensity() + 0.5f);
	}
	
	public int pxToDpi(float px) {
		return (int)(px / getDensity() + 0.5f);
	}
}
