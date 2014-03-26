package com.hellodev.lightme.view;

import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.hellodev.lightme.FlashController;
import com.hellodev.lightme.R;
import com.hellodev.lightme.service.ServiceHelper;
import com.hellodev.lightme.util.MPreferenceManager;

public class KeyguardPanelManager extends BasePanelManager implements
		OnTouchListener {
	private final static String TAG = "KeyguardPanelManager";

	private int PANEL_VIEW_DOCK_OFFSET_Y;
	private final int PANEL_VIEW_DOCK_X;
	private final int PANEL_VIEW_DOCK_Y;
	private Animation hintAnimation;

	public KeyguardPanelManager() {
		super();
		mPanelView = new KeyguardPanelView(mContext);
		mPanelView.setOnTouchListener(this);
		mPanelView.setBackgroundResource(R.drawable.keyguard_off);

		Drawable panelDrawable = mContext.getResources().getDrawable(
				R.drawable.keyguard_off);
		PANEL_VIEW_WIDTH = panelDrawable.getIntrinsicWidth();
		PANEL_VIEW_HEIGHT = panelDrawable.getIntrinsicHeight();
		PANEL_VIEW_DOCK_OFFSET_Y = mDisplayHelper.dpiToPx(163);
		PANEL_VIEW_DOCK_X = (SCREEN_WIDTH - PANEL_VIEW_WIDTH) / 2;
		PANEL_VIEW_DOCK_Y = SCREEN_HEIGHT - PANEL_VIEW_DOCK_OFFSET_Y;

		mPanelParams.windowAnimations = R.style.keyguard_panel_anim;
		mPanelParams.width = PANEL_VIEW_WIDTH;
		mPanelParams.height = PANEL_VIEW_HEIGHT;

		mPanelParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		mClearPanelParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
	}

	@Override
	public void showPanel() {
		if (!isPanelShown) {
			setWindowPosition(PANEL_VIEW_DOCK_X, PANEL_VIEW_DOCK_Y);
			flashController.addObserver(this);
		}
		
		setFlashLevel(flashController.getCurrentLevel());
	}
	
	//FIXME 需要判断动画是否已经执行完毕
	public void updatePanelWhenVisiable() {
		if(hintAnimation == null)
			hintAnimation = AnimationUtils.loadAnimation(mContext, R.anim.keyguard_shock_hint);
		mPanelView.startAnimation(hintAnimation);
	}

	// 发生在进入桌面时，屏幕关闭应该是要show出来，on的时候应该是shake的重新绑定，on的时候还需要做什么么，把power守护关掉
	@Override
	public void hidePanel() {
		if (isPanelShown) {
			removeWindow();
			closeClearPanel();
			flashController.removeObserver(this);
		}
	}

	@Override
	public void setFlashLevel(int currentLevel) {
		if(currentLevel != mCurrentLevel) {
			mCurrentLevel = currentLevel;
			if (mCurrentLevel == FlashController.LEVEL_OFF)
				mPanelView.setBackgroundResource(R.drawable.keyguard_off);
			else if (mCurrentLevel > FlashController.LEVEL_OFF)
				mPanelView.setBackgroundResource(R.drawable.keyguard_on);
		}
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownX = event.getRawX();
			mDownY = event.getRawY();

			mInitialX = mPanelParams.x;
			mInitialY = mPanelParams.y;

			mIsLongPressing = false;
			break;

		case MotionEvent.ACTION_MOVE:
			if (mIsLongPressing) {
				float x = mInitialX + (event.getRawX() - mDownX);
				float y = mInitialY + (event.getRawY() - mDownY);

				// show clear panel
				float panelBottom = y + PANEL_VIEW_HEIGHT;
				toggleClearPanelFocusChanged(panelBottom);
				moveTo(x, y);
				
				if (flashController.isFlashOn())
					changeFlashLightWithMove(mDownY, y);
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mIsLongPressing) {

				if (isClearPanelFocused) {
					MPreferenceManager mPrefsMgr = MPreferenceManager
							.getInstance();
					mPrefsMgr.toggleKeyguardPanel(false, true);
					ServiceHelper.stopKeyguardPanelService();
				} else {
					dock();
				}
				onLongPressStateEnd();
			}
			break;
		}
		return mPanelGestureDetector == null ? true : mPanelGestureDetector
				.onTouchEvent(event);
	}

	private void dock() {
		smoothMoveTo(PANEL_VIEW_DOCK_X, PANEL_VIEW_DOCK_Y);
	}

	@Override
	protected int getPanelWidth() {
		return PANEL_VIEW_WIDTH;
	}

	@Override
	protected int getPanelHeight() {
		return PANEL_VIEW_HEIGHT;
	}

	@Override
	protected int getPanelLayoutGravity() {
		return Gravity.LEFT | Gravity.TOP;
	}
}
