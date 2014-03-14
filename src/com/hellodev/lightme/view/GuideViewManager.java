package com.hellodev.lightme.view;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;

import com.hellodev.lightme.FlashApp;
import com.hellodev.lightme.R;

public class GuideViewManager implements OnClickListener , OnTouchListener{
	private WindowManager wm;
	private LayoutParams winParams;
	private Context appContext;
	private Timer timer = new Timer();
	private TimerTask autoCloseTask;
	private View mGuideView;
	
	public final static int AUTO_CLOSE_NOT_NEED = 0;
	private final static int AUTO_CLOSE_MSG = 1;
	private boolean closeAble;
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if( msg.what == AUTO_CLOSE_MSG) {
				close();
			}
		};
	};

	public GuideViewManager(WindowManager wm, int windowType) {
		appContext = FlashApp.getContext();

		this.wm = wm;

		winParams = new LayoutParams();
		winParams.type = windowType;
		winParams.format = PixelFormat.RGBA_8888;
		winParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
				| LayoutParams.FLAG_NOT_FOCUSABLE
				| LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
		winParams.windowAnimations = R.style.info_popu_anim;
	}
	
	public void add(View guideView, int gravity, int posX, int posY, boolean closeAble ) {
		add(guideView, gravity, posX, posY, 0, closeAble);
	}

	public void add(View guideView, int gravity, int posX, int posY, long autoCloseTimeDelay, boolean closeAble) {
		mGuideView = guideView;
		mGuideView.measure(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		
		winParams.width = mGuideView.getMeasuredWidth();
		winParams.height = mGuideView.getMeasuredHeight();
		winParams.gravity = gravity;

		winParams.x = posX;
		winParams.y = posY;

		wm.addView(guideView, winParams);
		guideView.setOnTouchListener(this);
		guideView.setOnClickListener(this);
		
		if(autoCloseTimeDelay > 0) {
			autoCloseTask = new TimerTask() {
				
				@Override
				public void run() {
					handler.sendEmptyMessage(AUTO_CLOSE_MSG);
					autoCloseTask = null;
				}
			};
			timer.schedule(autoCloseTask, autoCloseTimeDelay);
		}
	}
	
	public void close() {
		if(autoCloseTask != null) {
			autoCloseTask.cancel();
		}
		if(mGuideView != null) {
			wm.removeView(mGuideView);
			mGuideView = null;
		}
	}

	@Override
	public void onClick(View view) {
		close();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		boolean isHandled = false;
		if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			close();
			isHandled = true;
		}
		return isHandled;
	}
}
