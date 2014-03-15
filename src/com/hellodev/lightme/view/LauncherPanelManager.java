package com.hellodev.lightme.view;

import com.hellodev.lightme.FlashController;
import com.hellodev.lightme.R;
import com.hellodev.lightme.service.ServiceHelper;
import com.hellodev.lightme.util.MPreferenceManager;

import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class LauncherPanelManager extends BasePanelManager implements
		OnTouchListener {
	private final static String TAG = "LauncherPanelManager";

	private int PANEL_VIEW_NORMAL_MOVE_Y_MAX;
	private int mLastX = -1;
	private int mLastY = -1;

	public LauncherPanelManager() {
		super();
		mPanelView = new View(mContext);
		mPanelView.setOnTouchListener(this);
		mPanelView.setBackgroundResource(R.drawable.launcher_off);

		Drawable panelDrawable = mContext.getResources().getDrawable(
				R.drawable.launcher_off);
		PANEL_VIEW_WIDTH = panelDrawable.getIntrinsicWidth();
		PANEL_VIEW_HEIGHT = panelDrawable.getIntrinsicHeight();
		PANEL_VIEW_NORMAL_MOVE_Y_MAX = SCREEN_HEIGHT - CLEAR_PANEL_VIEW_HEIGHT
				- PANEL_VIEW_HEIGHT - PANEL_PRESSED_INCREMENT;
		mPanelParams.width = PANEL_VIEW_WIDTH;
		mPanelParams.height = PANEL_VIEW_HEIGHT;
	}

	@Override
	public void showPanel() {
		if (!isPanelShown) {
			if (mLastX == -1 || mLastY == -1) {
				mLastX = 0;
				mLastY = SCREEN_HEIGHT / 2;
			}

			setWindowPosition(mLastX, mLastY);
			flashController.addObserver(this);
		}
		setFlashLevel(flashController.getCurrentLevel());
	}

	/*
	 * hide的时候不需要removeObserver，减少release
	 */
	@Override
	public void hidePanel() {
		if (isPanelShown) {
			removeWindow();
			PANEL_VIEW_NORMAL_MOVE_Y_MAX = SCREEN_HEIGHT
					- CLEAR_PANEL_VIEW_HEIGHT - PANEL_VIEW_HEIGHT;

			closeClearPanel();
			mLastX = mPanelParams.x;
			mLastY = mPanelParams.y;
			flashController.removeObserver(this);

			// 关闭引导页
//			closeGuideView();
		}
	}

	private void dock() {
		int curX = mPanelParams.x;
		int centerMark = (SCREEN_WIDTH - PANEL_VIEW_WIDTH) / 2;
		if (curX < centerMark) {
			smoothMoveToLeft();
		} else {
			smoothMoveToRight();
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
			float x = mInitialX + (event.getRawX() - mDownX);
			float y = mInitialY + (event.getRawY() - mDownY);

			if (mIsLongPressing) {
				// show clear panel
				float panelBottom = y + PANEL_VIEW_HEIGHT;
				toggleClearPanelFocusChanged(panelBottom);

				if (flashController.isFlashOn())
					changeFlashLightWithMove(mDownY, y);
			} else if (y > PANEL_VIEW_NORMAL_MOVE_Y_MAX) {
				// 普通移动无法移动到clearPanel区域
				y = PANEL_VIEW_NORMAL_MOVE_Y_MAX;
			}
			moveTo(x, y);
			break;
		case MotionEvent.ACTION_UP:

			if (mIsLongPressing) {
				if (isClearPanelFocused) {
					MPreferenceManager.getInstance().toggleLauncherPanel(false);
					MPreferenceManager.getInstance()
							.setNeedRefreshSetting(true);
					ServiceHelper.stopLauncherPanelService();
				} else {
					smoothMoveTo((int) mInitialX, (int) mInitialY);
				}
				onLongPressStateEnd();
			} else {
				dock();
			}
			break;
		}
		return mPanelGestureDetector == null ? true : mPanelGestureDetector
				.onTouchEvent(event);
	}

	@Override
	public void setFlashLevel(int currentLevel) {
		if(currentLevel != mCurrentLevel) {
			mCurrentLevel = currentLevel;
			if (currentLevel == FlashController.LEVEL_OFF)
				mPanelView.setBackgroundResource(R.drawable.launcher_off);
			else if (currentLevel > FlashController.LEVEL_OFF)
				mPanelView.setBackgroundResource(R.drawable.launcher_on);
		}
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
