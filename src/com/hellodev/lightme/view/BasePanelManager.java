package com.hellodev.lightme.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.hellodev.lightme.FlashApp;
import com.hellodev.lightme.FlashController;
import com.hellodev.lightme.FlashController.OnFlashLevelChangedListener;
import com.hellodev.lightme.R;
import com.hellodev.lightme.util.MDisplayHelper;
import com.hellodev.lightme.util.MathHelper;

import java.lang.reflect.Field;

public abstract class BasePanelManager implements OnFlashLevelChangedListener{
	private final static String TAG = "BasePanelManager";
	protected int SCREEN_WIDTH, SCREEN_HEIGHT, STATUS_BAR_HEIGHT;
	protected int CLEAR_PANEL_VIEW_HEIGHT;
	protected int PANEL_VIEW_WIDTH;
	protected int PANEL_VIEW_HEIGHT;
	protected int PANEL_PRESSED_INCREMENT;
	protected int LEVEL_DISTANCE = 20;

	protected Context mContext;
	protected WindowManager wm;
	protected WindowManager.LayoutParams mPanelParams, mClearPanelParams;

	protected View mPanelView;
	protected TextView mClearPanelView;
	protected Vibrator mVibrator;
	protected MDisplayHelper mDisplayHelper;

	protected boolean isPanelShown = false;
	protected boolean isClearPanelShown = false;
	protected boolean isClearPanelFocused = false;
	protected int mCurrentLevel = FlashController.LEVEL_OFF;

	protected float mInitialX, mInitialY;
	protected float mDownX, mDownY;
	protected boolean mIsLongPressing;
	protected GestureDetector mPanelGestureDetector;
	protected FlashController flashController;
	
	protected GuideViewManager guideViewMgr;
	
	protected BasePanelManager() {
		mContext = FlashApp.getContext();
		mVibrator = (Vibrator) mContext
				.getSystemService(Context.VIBRATOR_SERVICE);
		wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

		mDisplayHelper = new MDisplayHelper();
		PANEL_PRESSED_INCREMENT = mDisplayHelper.dpiToPx(6);
		CLEAR_PANEL_VIEW_HEIGHT = mDisplayHelper.dpiToPx(32);
		STATUS_BAR_HEIGHT = getStatusBarHeight();
		SCREEN_WIDTH = wm.getDefaultDisplay().getWidth();
		SCREEN_HEIGHT = wm.getDefaultDisplay().getHeight() - STATUS_BAR_HEIGHT;

		mPanelParams = new WindowManager.LayoutParams();
		mPanelParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		mPanelParams.format = PixelFormat.RGBA_8888;
		mPanelParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
				| LayoutParams.FLAG_NOT_FOCUSABLE;
		mPanelParams.gravity = getPanelLayoutGravity();
		mPanelParams.windowAnimations = R.style.panel_anim;

		mClearPanelView = (TextView) LayoutInflater.from(mContext).inflate(
				R.layout.layout_clear_panel, null);
		mClearPanelParams = new WindowManager.LayoutParams();
		mClearPanelParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		mClearPanelParams.format = PixelFormat.RGBA_8888;
		mClearPanelParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
				| LayoutParams.FLAG_NOT_FOCUSABLE;
		mClearPanelParams.height = CLEAR_PANEL_VIEW_HEIGHT;
		mClearPanelParams.x = 0;
		mClearPanelParams.y = SCREEN_HEIGHT;
		mClearPanelParams.windowAnimations = R.style.clear_panel_anim;

		mPanelGestureDetector = new GestureDetector(mContext,
				mPanelGestureListener);
		
		flashController = FlashController.getInstance();
	}
	
	@Override
	public void onFlashLevelChanged(int currentLevel) {
		setFlashLevel(currentLevel);
	}

	public boolean isPanelShown() {
		// 只是用来标记Panel是否隐藏了
		return isPanelShown;
	}

	protected void toggleClearPanelFocusChanged(float panelBottom) {
		float clearPanelTop = SCREEN_HEIGHT - CLEAR_PANEL_VIEW_HEIGHT;
		if (isClearPanelFocused && panelBottom < clearPanelTop) {
			mClearPanelView.setBackgroundResource(R.color.clear_panel_normal);
			isClearPanelFocused = false;
		} else if (!isClearPanelFocused && panelBottom >= clearPanelTop) {
			mClearPanelView.setBackgroundResource(R.color.clear_panel_focus);
			isClearPanelFocused = true;
		}
	}

	protected void showClearPanel() {
		if (!isClearPanelShown) {
			wm.addView(mClearPanelView, mClearPanelParams);
			isClearPanelShown = true;
		}
	}

	protected void closeClearPanel() {
		if (isClearPanelShown) {
			wm.removeView(mClearPanelView);
			isClearPanelShown = false;
			isClearPanelFocused = false;
		}
	}
	
	protected void onLongPressStateBegin() {
		mVibrator.vibrate(100);
		mPanelParams.width = PANEL_VIEW_WIDTH + PANEL_PRESSED_INCREMENT;
		mPanelParams.height = PANEL_VIEW_HEIGHT + PANEL_PRESSED_INCREMENT;
		updatePanel();

		showClearPanel();
	}

	protected void onLongPressStateEnd() {
		mPanelParams.width = PANEL_VIEW_WIDTH;
		mPanelParams.height = PANEL_VIEW_HEIGHT;
		updatePanel();

		mIsLongPressing = false;
		closeClearPanel();
	}

	/**
	 * 
	 * 与其他的move方法互斥
	 * 
	 */
	protected void smoothMoveToLeft() {
		final int currentX = mPanelParams.x;
		new CountDownTimer(500, 5) {
			@Override
			public void onTick(long millisUntilFinished) {
				long step = (500 - millisUntilFinished) / 5;
				updatePanel((int) MathHelper.bounceValue(step, currentX),
						mPanelParams.y);
			}

			@Override
			public void onFinish() {
				updatePanel(0, mPanelParams.y);
			}
		}.start();
	}

	/**
	 * 
	 * 与其他的move方法互斥
	 * 
	 */
	protected void smoothMoveToRight() {
		final int endX = SCREEN_WIDTH - PANEL_VIEW_WIDTH;
		final int distanceX = endX - mPanelParams.x;
		new CountDownTimer(500, 5) {
			@Override
			public void onTick(long millisUntilFinished) {
				long step = (500 - millisUntilFinished) / 5;
				updatePanel(
						endX - (int) MathHelper.bounceValue(step, distanceX),
						mPanelParams.y);
			}

			@Override
			public void onFinish() {
				updatePanel(SCREEN_WIDTH - PANEL_VIEW_WIDTH,
						mPanelParams.y);
			}
		}.start();
	}

	/**
	 * 
	 * 与其他的move方法互斥
	 * 
	 */
	protected void smoothMoveTo(final int dstX, final int dstY) {
		final int currentX = mPanelParams.x;
		final int currentY = mPanelParams.y;
		final int distanceX = (int) Math.abs(currentX - dstX);
		final int distanceY = (int) Math.abs(currentY - dstY);

		new CountDownTimer(500, 5) {
			@Override
			public void onTick(long millisUntilFinished) {
				long step = (500 - millisUntilFinished) / 5;
				int x = 0;
				int y = 0;
				if (currentX >= dstX) {
					x = dstX + (int) MathHelper.bounceValue(step, distanceX);
				} else {
					x = dstX - (int) MathHelper.bounceValue(step, distanceX);
				}

				if (currentY >= dstY) {
					y = dstY + (int) MathHelper.bounceValue(step, distanceY);
				} else {
					y = dstY - (int) MathHelper.bounceValue(step, distanceY);
				}
				updatePanel(x, y);
			}

			@Override
			public void onFinish() {
				updatePanel(dstX, dstY);
			}
		}.start();
	}

	/**
	 * 
	 * 与其他的move方法互斥
	 * 
	 */
	protected void moveTo(float dstX, float dstY) {
		updatePanel((int) dstX, (int) dstY);
	}

	protected void changeFlashLightWithMove(float startY, float endY) {
		float distanceY = endY - startY;
		if (distanceY > LEVEL_DISTANCE) {
			flashController.turnFlashDown();
		} else if (distanceY < -LEVEL_DISTANCE) {
			flashController.turnFlashUp();
		}
	}

	// 这个是需要的，但是其他的关联操作呢，主要是smoothMove的线程和其他的线程的矛盾
	protected synchronized void updatePanel() {
		if (isPanelShown)
			wm.updateViewLayout(mPanelView, mPanelParams);
	}
	
	protected synchronized void updatePanel(int x, int y) {
		mPanelParams.x = x;
		mPanelParams.y = y;
		if (isPanelShown)
			wm.updateViewLayout(mPanelView, mPanelParams);
	}
	
	protected synchronized void addPanel(int x, int y) {
		mPanelParams.x = x;
		mPanelParams.y = y;
		wm.addView(mPanelView, mPanelParams);
		isPanelShown = true;
	}
	
	protected synchronized void removeWindow() {
		wm.removeView(mPanelView);
		isPanelShown = false;
	}

	/*
	 * 在对应service关闭的时候，有没有更高效的方式不用这么逐层传递
	 */
	public void closePanel() {
		hidePanel();
		onDestroy();
	}
	
	protected void onDestroy() {
		flashController = null;
	}

	private OnGestureListener mPanelGestureListener = new OnGestureListener() {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			mIsLongPressing = true;

			onLongPressStateBegin();
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			setFlashLevel(flashController.toggleFlash());
			
			//关闭引导页
//			closeGuideView();
			return true;
		}
	};
	
	protected void closeGuideView() {
		if(guideViewMgr != null) {
			guideViewMgr.close();
			guideViewMgr = null;
		}
	}
	
	private int getStatusBarHeight() {
		int statusBarHeight = 0;
		try {
			Class<?> c = Class.forName("com.android.internal.R$dimen");
			Object o = c.newInstance();
			Field field = c.getField("status_bar_height");
			int x = (Integer) field.get(o);
			statusBarHeight = mContext.getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusBarHeight;
	}

	protected abstract int getPanelWidth();

	protected abstract int getPanelHeight();

	protected abstract int getPanelLayoutGravity();

	protected abstract void showPanel();

	protected abstract void hidePanel();
	
	protected abstract void setFlashLevel(int currentLevel);
}
