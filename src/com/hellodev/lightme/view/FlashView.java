package com.hellodev.lightme.view;

import android.content.Context;
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

	// Flash Light
	private int FLASH_END_X_MAX;
	private int FLASH_END_X_INTERVAL;
	private int FLASH_END_X_MIN;
	private final static int FLASH_END_Y = 0;

	private int FLASH_START_SIZE;
	private int FLASH_START_Y;

	// Eye
	private int EYE_DISTANCE_MIN;
	private int EYE_DISTANCE_INTERVAL;
	private int EYE_Y_MIN;
	private int EYE_Y_INTERVAL;

	private int EYE_SIZE_X_MAX;
	private int EYE_SIZE_X_INTERVAL;
	private int EYE_SIZE_Y_MAX;
	private int EYE_SIZE_Y_INTERVAL;
	
	private int TEAR_SIZE_Y;

	// Mouth
	private int MOUTH_SIZE_X_MAX;
	private int MOUTH_SIZE_X_INTERVAL;
	private int MOUTH_SIZE_Y_MAX;
	private int MOUTH_SIZE_Y_INTERVAL;

	private int MOUTH_Y_MIN;
	private int MOUTH_Y_INTERVAL;

	private final static float FACE_STROKE_SIZE_INTERVAL = 0.05f;
	private int FACE_STROKE_SIZE_X;
	private int FACE_STROKE_SIZE_Y;

	private int FLASH_BITMAP_OFFSET;
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
	private final static float FLASH_ON_COLOR_V_INTERVAL = 0.05f;// light
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

		FLASH_START_SIZE = displayHelper.dpiToPx(30);
		FLASH_START_Y = displayHelper.dpiToPx(345);

		// Eye
		EYE_DISTANCE_MIN = displayHelper.dpiToPx(68);
		EYE_DISTANCE_INTERVAL = displayHelper.dpiToPx(4);
		EYE_Y_MIN = displayHelper.dpiToPx(100);
		EYE_Y_INTERVAL = displayHelper.dpiToPx(2);

		EYE_SIZE_X_MAX = displayHelper.dpiToPx(34);
		EYE_SIZE_X_INTERVAL = displayHelper.dpiToPx(2);
		EYE_SIZE_Y_MAX = displayHelper.dpiToPx(12);
		EYE_SIZE_Y_INTERVAL = displayHelper.dpiToPx(0.6f);

		TEAR_SIZE_Y = displayHelper.dpiToPx(70);

		// Mouth
		MOUTH_SIZE_X_MAX = displayHelper.dpiToPx(36);
		MOUTH_SIZE_X_INTERVAL = displayHelper.dpiToPx(6);
		MOUTH_SIZE_Y_MAX = displayHelper.dpiToPx(12);
		MOUTH_SIZE_Y_INTERVAL = displayHelper.dpiToPx(1);

		MOUTH_Y_MIN = displayHelper.dpiToPx(220);
		MOUTH_Y_INTERVAL = displayHelper.dpiToPx(4);

		// Stroke
		FACE_STROKE_SIZE_X = displayHelper.dpiToPx(11);
		FACE_STROKE_SIZE_Y = displayHelper.dpiToPx(7);

		// Flash bitmap
		FLASH_BITMAP_OFFSET = displayHelper.dpiToPx(6);
		flashOffBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.flash_off);
		flashOnBitmap = BitmapFactory.decodeResource(getResources(),
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

		float currentStrokeRate = 1 - FACE_STROKE_SIZE_INTERVAL
				* (LEVEL_COUNT - level);
		float strokeSizeX = FACE_STROKE_SIZE_X * currentStrokeRate;
		float strokeSizeY = FACE_STROKE_SIZE_Y * currentStrokeRate;

		// draw Flash
		if (!isFlashOn)
			canvas.drawBitmap(flashOffBitmap, flashStartXLeft, FLASH_START_Y
					+ FLASH_BITMAP_OFFSET, mFlashPaint);
		else
			canvas.drawBitmap(flashOnBitmap, flashStartXLeft, FLASH_START_Y
					+ FLASH_BITMAP_OFFSET, mFlashPaint);

		// draw Flash light
		mFlashPaint.setColor(Color.HSVToColor(mCurrentColorHSV));
		mFlashPath.reset();
		mFlashPath.moveTo(flashStartXLeft, FLASH_START_Y);
		mFlashPath.lineTo(flashStartXRight, FLASH_START_Y);
		mFlashPath.lineTo(flashEndXRight, FLASH_END_Y);
		mFlashPath.lineTo(flashEndXLeft, FLASH_END_Y);
		mFlashPath.close();
		canvas.drawPath(mFlashPath, mFlashPaint);

		if(lisenseEnable) {
			// draw Eye
			mFlashPaint.setColor(Color.BLACK);
			float eyeSizeX = (EYE_SIZE_X_MAX - (LEVEL_COUNT - level)
					* EYE_SIZE_X_INTERVAL)
					* currentStrokeRate;
			float eyeSizeY = (EYE_SIZE_Y_MAX - (LEVEL_COUNT - level)
					* EYE_SIZE_Y_INTERVAL)
					* currentStrokeRate;

			float eyeDistance = (EYE_DISTANCE_MIN + (level - 1)
					* EYE_DISTANCE_INTERVAL)
					* currentStrokeRate;
			float leftEyeStartX = SCREEN_WIDTH / 2 - eyeDistance / 2 - eyeSizeX;
			float rightEyeStartX = SCREEN_WIDTH / 2 + eyeDistance / 2;
			float eyeStartY = EYE_Y_MIN + EYE_Y_INTERVAL * (level - 1);
			float eyeBallOffset = (float) (eyeSizeX * (1.0 * level / LEVEL_COUNT));

			// draw mouth
			float mouthSizeX = (MOUTH_SIZE_X_MAX - (LEVEL_COUNT - level)
					* MOUTH_SIZE_X_INTERVAL)
					* currentStrokeRate;
			float mouthSizeY = (MOUTH_SIZE_Y_MAX - (LEVEL_COUNT - level)
					* MOUTH_SIZE_Y_INTERVAL)
					* currentStrokeRate;
			float mouthStartX = SCREEN_WIDTH / 2 - mouthSizeX / 2;
			float mouthStartY = MOUTH_Y_MIN + MOUTH_Y_INTERVAL
					* (LEVEL_COUNT - level);

			mFlashPaint.setStrokeWidth(strokeSizeX);
			canvas.drawLine(leftEyeStartX, eyeStartY, leftEyeStartX + eyeSizeX,
					eyeStartY, mFlashPaint);
			canvas.drawLine(rightEyeStartX, eyeStartY, rightEyeStartX + eyeSizeX,
					eyeStartY, mFlashPaint);
			canvas.drawLine(mouthStartX, mouthStartY, mouthStartX + mouthSizeX,
					mouthStartY, mFlashPaint);

			mFlashPaint.setStrokeWidth(strokeSizeY);
			canvas.drawLine(leftEyeStartX + eyeBallOffset, eyeStartY - strokeSizeX
					/ 2, leftEyeStartX + eyeBallOffset, eyeStartY + eyeSizeY,
					mFlashPaint);
			canvas.drawLine(rightEyeStartX + eyeBallOffset, eyeStartY - strokeSizeX
					/ 2, rightEyeStartX + eyeBallOffset, eyeStartY + eyeSizeY,
					mFlashPaint);
			canvas.drawLine(mouthStartX + mouthSizeX,
					mouthStartY + strokeSizeX / 2, mouthStartX + mouthSizeX,
					mouthStartY - mouthSizeY, mFlashPaint);
		} else {
			// draw Eye
			mFlashPaint.setColor(Color.BLACK);
			
			float eyeSizeX = isFlashOn ? EYE_SIZE_X_MAX: EYE_SIZE_X_MAX * 0.6f;
			float eyeDistance = (EYE_DISTANCE_MIN + (level - 1)
					* EYE_DISTANCE_INTERVAL)
					* currentStrokeRate;
			
			float leftEyeStartX = SCREEN_WIDTH / 2 - eyeDistance / 2 - eyeSizeX;
			float rightEyeStartX = SCREEN_WIDTH / 2 + eyeDistance / 2;
			float eyeStartY = EYE_Y_MIN + EYE_Y_INTERVAL * (level - 1);
			
			// draw mouth
			float mouthSizeX = isFlashOn? MOUTH_SIZE_X_MAX : MOUTH_SIZE_X_MAX * 0.6f;
			float mouthSizeY = isFlashOn? MOUTH_SIZE_Y_MAX : MOUTH_SIZE_Y_MAX * 0.6f;
			float mouthStartX = SCREEN_WIDTH / 2 - mouthSizeX / 2;
			float mouthStartY = MOUTH_Y_MIN + MOUTH_Y_INTERVAL
					* (LEVEL_COUNT - level);
			
			mFlashPaint.setStrokeWidth(strokeSizeX);
			canvas.drawLine(leftEyeStartX, eyeStartY, leftEyeStartX + eyeSizeX,
					eyeStartY, mFlashPaint);
			canvas.drawLine(rightEyeStartX, eyeStartY, rightEyeStartX + eyeSizeX,
					eyeStartY, mFlashPaint);
			canvas.drawLine(mouthStartX, mouthStartY, mouthStartX + mouthSizeX,
					mouthStartY, mFlashPaint);
			
			mFlashPaint.setStrokeWidth(strokeSizeY);
			canvas.drawLine(mouthStartX + mouthSizeX - strokeSizeY/2,
					mouthStartY - strokeSizeX/2, mouthStartX + mouthSizeX - strokeSizeY/2,
					mouthStartY + mouthSizeY, mFlashPaint);
			
			//draw tear
			float tearStrokeSize = eyeSizeX * 0.4f;
			mFlashPaint.setColor(Color.WHITE);
			mFlashPaint.setStrokeWidth(tearStrokeSize);
			
			float leftTearStartX = leftEyeStartX + eyeSizeX/2 ;
			float rightTearStartX = rightEyeStartX + eyeSizeX/2;
			float tearStartY = eyeStartY + strokeSizeX/2;
			float tearSizeY = isFlashOn? TEAR_SIZE_Y : TEAR_SIZE_Y * 0.6f;
			canvas.drawLine(leftTearStartX, tearStartY, leftTearStartX, tearStartY + tearSizeY,
					mFlashPaint);
			canvas.drawLine(rightTearStartX, tearStartY, rightTearStartX, tearStartY + tearSizeY,
					mFlashPaint);
		}
	};
}
