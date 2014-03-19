package com.hellodev.lightme.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.hellodev.lightme.FlashController;
import com.hellodev.lightme.R;
import com.hellodev.lightme.util.MDisplayHelper;

public class FlashView extends View {
	private final static String TAG = "FlashView";
	private final static int LEVEL_COUNT = 7;

	private float LEVEL_DISTANCE_INTERVAL = 20;
	private int SCREEN_WIDTH;

	//flash
	private int FLASH_BITMAP_START_X;
	
	// Flash Light
	private int FLASH_END_X_MAX;
	private int FLASH_END_X_INTERVAL;
	private int FLASH_END_X_MIN;
	private final static int FLASH_END_Y = 0;

	private int FLASH_START_SIZE;
	private int FLASH_START_Y;
	private int FLASH_START_X;

	//emotion
	private int EMOTION_POS_Y;
	private int[] emotionReses = {
			R.drawable.emotion_7,
			R.drawable.emotion_7,
			R.drawable.emotion_7,
			R.drawable.emotion_7,
			R.drawable.emotion_7,
			R.drawable.emotion_5,
			R.drawable.emotion_6,
			R.drawable.emotion_7};
	private Bitmap[] emotionBitmaps;
	
	private int[] emotionLockedReses = {};
	private Bitmap[] emotionLockedBitmaps;
	
	//flash
	private Bitmap flashOffBitmap, flashOnBitmap;

	private final static float FLASH_ON_COLOR_H;
	private final static float FLASH_ON_COLOR_S;
	static {
		float[] hsv = new float[3];
		Color.colorToHSV(0xffd200, hsv);
		FLASH_ON_COLOR_H = hsv[0];
		FLASH_ON_COLOR_S = hsv[1];
	};
	private final static float FLASH_ON_COLOR_V_MAX = 1;
	private final static float FLASH_ON_COLOR_V_INTERVAL = 0.08f;// light
	private final static int FLASH_OFF_COLOR = 0x161616;

	private float[] mCurrentColorHSV;

	private Paint mFlashPaint;
	private Path mFlashPath;

	private int mCurrentLevel;
	private boolean lisenseEnable = true;

	private OnFlashStateChangeListener mListener;
	private GestureDetector mGestureDetector;
	private MDisplayHelper displayHelper;

	public FlashView(Context context, AttributeSet attrs) {
		super(context, attrs);

		displayHelper = new MDisplayHelper();
		DisplayMetrics metrics = displayHelper.getDisplayMetrics();
		SCREEN_WIDTH = metrics.widthPixels;

		// Flash Light
		FLASH_END_X_MAX = SCREEN_WIDTH;
		FLASH_END_X_INTERVAL = displayHelper.dpiToPx(40);
		FLASH_END_X_MIN = FLASH_END_X_MAX - FLASH_END_X_INTERVAL
				* (LEVEL_COUNT - 1);

		FLASH_START_SIZE = displayHelper.dpiToPx(28);
		FLASH_START_Y = displayHelper.dpiToPx(345);

		Resources resource = getResources();
		// Eye
		EMOTION_POS_Y = displayHelper.dpiToPx(90);
		emotionBitmaps = new Bitmap[LEVEL_COUNT + 1];
		for(int i = 0; i < LEVEL_COUNT + 1; ++i) {
			emotionBitmaps[i] = BitmapFactory.decodeResource(resource,
					emotionReses[i]);
		}
		// Flash bitmap
		flashOffBitmap = BitmapFactory.decodeResource(resource,
				R.drawable.flash_off);
		flashOnBitmap = BitmapFactory.decodeResource(resource,
				R.drawable.flash_on);
		
		mFlashPaint = new Paint();
		mFlashPaint.setAntiAlias(true);
		mFlashPaint.setColor(Color.YELLOW);
		mFlashPaint.setStyle(Paint.Style.FILL);

		mFlashPath = new Path();
		mCurrentColorHSV = new float[3];
		Color.colorToHSV(FLASH_OFF_COLOR, mCurrentColorHSV);

		mCurrentLevel = FlashController.LEVEL_OFF;
		mGestureDetector = new GestureDetector(context,
				mFlashViewGestureListener);
	}
	
	public void setLisenseState(boolean isLisenseEnable, int currentLevel) {
		this.lisenseEnable = isLisenseEnable;
		
		if (currentLevel > FlashController.LEVEL_OFF) {
			turnFlashLevel(currentLevel);
		} else {
			turnFlashOff();
		}
		mCurrentLevel = currentLevel;
	}

	public void setFlashLevel(int currentLevel) {
		if (mCurrentLevel != currentLevel) {
			if (currentLevel > FlashController.LEVEL_OFF) {
				turnFlashLevel(currentLevel);
			} else {
				turnFlashOff();
			}
			mCurrentLevel = currentLevel;
		}
	}

	private void turnFlashOff() {
		Color.colorToHSV(FLASH_OFF_COLOR, mCurrentColorHSV);
		postInvalidate();
	}

	private void turnFlashLevel(int level) {
		mCurrentColorHSV[0] = FLASH_ON_COLOR_H;
		mCurrentColorHSV[1] = FLASH_ON_COLOR_S;
		mCurrentColorHSV[2] = FLASH_ON_COLOR_V_MAX - (FlashController.LEVEL_MAX - level) * FLASH_ON_COLOR_V_INTERVAL;
		postInvalidate();
	}

	public void setOnSwitchStateChangeListener(
			OnFlashStateChangeListener listener) {
		mListener = listener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector != null)
			return mGestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	private OnGestureListener mFlashViewGestureListener = new OnGestureListener() {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			boolean isHandleEvent = false;
			if (mListener != null) {
				mListener.onSwitchClick();
				isHandleEvent = true;
			}
			return isHandleEvent;
		}

		@Override
		public void onShowPress(MotionEvent e) {

		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			boolean isHandleEvent = false;
			if (mCurrentLevel > FlashController.LEVEL_OFF && mListener != null) {
				if (distanceY >= LEVEL_DISTANCE_INTERVAL) {
					mListener.onSwitchTurnUp();
				} else if (distanceY <= -LEVEL_DISTANCE_INTERVAL) {
					mListener.onSwitchTurnDown();
				}
				isHandleEvent = true;
			}
			return isHandleEvent;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
	};

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		float centerX = SCREEN_WIDTH / 2;

		boolean isFlashOn = mCurrentLevel > FlashController.LEVEL_OFF;
		int level = isFlashOn ? mCurrentLevel : mCurrentLevel + 1;// 特殊绘制FlashOff
		float flashEndSize = FLASH_END_X_MIN + FLASH_END_X_INTERVAL
				* (level - 1);
		float flashStartXLeft = centerX - FLASH_START_SIZE / 2;
		float flashStartXRight = centerX + FLASH_START_SIZE / 2;
		float flashEndXLeft = (FLASH_END_X_MAX - flashEndSize) / 2;
		float flashEndXRight = FLASH_END_X_MAX - flashEndXLeft;

		// draw Flash
		if (!isFlashOn)
			canvas.drawBitmap(flashOffBitmap, flashStartXLeft, FLASH_START_Y, mFlashPaint);
		else
			canvas.drawBitmap(flashOnBitmap, flashStartXLeft, FLASH_START_Y, mFlashPaint);

		// draw Flash light
		mFlashPaint.setStyle(Paint.Style.FILL);
		mFlashPaint.setColor(Color.HSVToColor(mCurrentColorHSV));
		
		mFlashPath.reset();
		mFlashPath.moveTo(flashStartXLeft, FLASH_START_Y);
		mFlashPath.lineTo(flashStartXRight, FLASH_START_Y);
		mFlashPath.lineTo(flashEndXRight, FLASH_END_Y);
		mFlashPath.lineTo(flashEndXLeft, FLASH_END_Y);
		mFlashPath.close();
		canvas.drawPath(mFlashPath, mFlashPaint);

		// draw Emotion
		Bitmap emotionBitmap = getCurrentEmotionBitmap(lisenseEnable, mCurrentLevel);
		float emotionPosX = centerX - emotionBitmap.getWidth()/2;
		canvas.drawBitmap(emotionBitmap, emotionPosX, EMOTION_POS_Y, mFlashPaint);
		
		if(!lisenseEnable) {
			//draw tear
		}
	};
	
	private Bitmap getCurrentEmotionBitmap(boolean isLisenseEnable, int flashLevel) {
		Bitmap eyeBitmap;
		if(isLisenseEnable) {
			eyeBitmap = emotionBitmaps[flashLevel];
		} else {
			boolean isFlashOn = flashLevel > FlashController.LEVEL_OFF;
			eyeBitmap = isFlashOn? emotionLockedBitmaps[1]: emotionLockedBitmaps[0];
		}
		return eyeBitmap;
	}
	
	public void releaseData() {
		int sizeOfEmotion = emotionBitmaps != null? emotionBitmaps.length: 0;
		for(int i = 0; i < sizeOfEmotion; ++i) {
			emotionBitmaps[i].recycle();
		}
		
		int sizeOfEmotionLocked = emotionLockedBitmaps != null? emotionLockedBitmaps.length: 0;
		for(int i = 0; i < sizeOfEmotionLocked; ++i) {
			emotionLockedBitmaps[i].recycle();
		}
		
		if(flashOnBitmap != null) {
			flashOnBitmap.recycle();
		}
		
		if(flashOffBitmap != null) {
			flashOffBitmap.recycle();
		}
	}
}
