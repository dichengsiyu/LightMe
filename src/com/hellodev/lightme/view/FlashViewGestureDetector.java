package com.hellodev.lightme.view;

import android.util.Log;
import android.view.MotionEvent;

public class FlashViewGestureDetector {
	private final static String TAG = "FlashViewGestureDetector";
	private FlashViewGestureListener mListener;
	private float mLastY;
	private final static float LEVEL_DISTANCE = 20;
	
	public FlashViewGestureDetector(FlashViewGestureListener listener) {
		mListener = listener;
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		boolean isHandleEvent = true;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = event.getRawY();
			isHandleEvent = true;
			break;
		case MotionEvent.ACTION_MOVE:
			float currentY = event.getRawY();
			float distance = currentY - mLastY;
			Log.v(TAG, "distance:"+distance);
			if(distance >= LEVEL_DISTANCE) {
				//plus
				mListener.onScrollDown();
				mLastY = event.getRawY();
			} else if(distance <= -LEVEL_DISTANCE) {
				//minus
				mListener.onScrollUp();
				mLastY = event.getRawY();
			} else
				isHandleEvent = false;
			break;
		}
		return isHandleEvent;
	}
	
	public interface FlashViewGestureListener {
		public void onScrollUp();
		public void onScrollDown();
	}
}
