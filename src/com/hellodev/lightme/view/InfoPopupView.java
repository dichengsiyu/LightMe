package com.hellodev.lightme.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.hellodev.lightme.R;
import com.hellodev.lightme.util.MDisplayHelper;

public class InfoPopupView {
	private TextView mContentView;
	private int POPUP_OFFSET;
	private WindowManager wm;
	private WindowManager.LayoutParams winParams;
	private boolean isShowing = false;
	private MDisplayHelper displayHelper;
	
	public InfoPopupView(Context context) {
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mContentView = (TextView) LayoutInflater.from(context).inflate(R.layout.info_popup_view, null);
		
		displayHelper = new MDisplayHelper();
		
		POPUP_OFFSET = displayHelper.dpiToPx(100);
		winParams = new LayoutParams();
		winParams.width = LayoutParams.WRAP_CONTENT;
		winParams.height = LayoutParams.WRAP_CONTENT;
		winParams.type = WindowManager.LayoutParams.TYPE_TOAST;
		winParams.format = PixelFormat.RGBA_8888;
		winParams.flags = LayoutParams.FLAG_NOT_TOUCHABLE | LayoutParams.FLAG_NOT_FOCUSABLE;
		winParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		winParams.x = 0;
		winParams.y = POPUP_OFFSET;
		winParams.windowAnimations = R.style.info_popu_anim;
	}
	
	public void show(View parentView, String info) {
		if(!TextUtils.isEmpty(info) && isShowing == false) {
			mContentView.setText(info);
			wm.addView(mContentView, winParams);
			isShowing = true;
		}
	}
	
	public void close() {
		if(isShowing) {
			wm.removeView(mContentView);
			isShowing = false;
		}
	}
	
	public boolean isShowing() {
		return isShowing;
	}
}
