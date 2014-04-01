package com.hellodev.lightme.activity;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hellodev.lightme.FlashController;
import com.hellodev.lightme.FlashController.OnFlashLevelChangedListener;
import com.hellodev.lightme.R;
import com.hellodev.lightme.service.ServiceHelper;
import com.hellodev.lightme.util.LightmeConstants;
import com.hellodev.lightme.util.MConnectHelper;
import com.hellodev.lightme.util.MDisplayHelper;
import com.hellodev.lightme.util.MLisenseMangaer;
import com.hellodev.lightme.util.MPreferenceManager;
import com.hellodev.lightme.util.SmartBarUtils;
import com.hellodev.lightme.util.MLisenseMangaer.OnLisenseStateChangeListener;
import com.hellodev.lightme.view.FlashView;
import com.hellodev.lightme.view.GuideViewManager;
import com.hellodev.lightme.view.OnFlashStateChangeListener;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends Activity implements
		OnFlashStateChangeListener, OnFlashLevelChangedListener,
		OnLisenseStateChangeListener, OnClickListener {
	private final static String TAG = "MainActivity";

	private FlashView flashView;
	private ImageButton btnLock;
	private FlashController flashController;

	private MPreferenceManager prefsMgr;
	private GuideViewManager guideViewMgr;
	private boolean firstSetup = false;

	private MConnectHelper connector;
	private MLisenseMangaer lisenseManager;
	private boolean isLisenseEnable = true;
	private AlertDialog mLockDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ServiceHelper.startPanelService();

		initData();
		initSmartBar();
		initView();

		showGuideView();

		MobclickAgent.updateOnlineConfig(this);// 更新在线发送策略
		MobclickAgent.setDebugMode(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		flashController.addObserver(this);
		initLisense();
		if(flashController.hasCameraReleased()) {
			flashController.turnFlashOffIfCameraReleased();
			flashController.initCameraSync();
		}
		flashView.setFlashLevel(flashController.getCurrentLevel());
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		flashController.removeObserver(this);
		MobclickAgent.onPause(this);
		closeGuideView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseData();
	}
	
	@Override
	public void onBackPressed() {
		if (!prefsMgr.isLauncherPanelShown()) {
			flashController.turnFlashOff();
		}
		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.settings) {
			Intent intent = new Intent(this, SettingActivity.class);
			startActivity(intent);
			return true;
		} else
			return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSwitchClick() {
		flashView.setFlashLevel(flashController.toggleFlash());
		closeGuideView();
	}

	@Override
	public void onSwitchTurnUp() {
		flashView.setFlashLevel(flashController.turnFlashUp());
	}

	@Override
	public void onSwitchTurnDown() {
		flashView.setFlashLevel(flashController.turnFlashDown());
	}

	@Override
	public void onFlashLevelChanged(int currentLevel) {
		flashView.setFlashLevel(currentLevel);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_lock:
			initLockAlert();
			mLockDialog.show();
			break;
		default:
			break;
		}
	}

	private void initView() {
		flashView = (FlashView) findViewById(R.id.btn_toggle);
		flashView.setOnSwitchStateChangeListener(this);

		btnLock = (ImageButton) findViewById(R.id.btn_lock);
		btnLock.setOnClickListener(this);
	}

	private void initSmartBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(0);
		SmartBarUtils.setActionBarViewCollapsable(actionBar, true);
		// 替换back键图标
		SmartBarUtils.setBackIcon(actionBar,
				getResources().getDrawable(R.drawable.ic_back));
	}

	private void initData() {
		// 相机和声音都在GuideActivity里面去初始化了
		prefsMgr = MPreferenceManager.getInstance();
		flashController = FlashController.getInstance();
		connector = new MConnectHelper(this);
	}

	private void releaseData() {
		if (mLockDialog != null && mLockDialog.isShowing())
			mLockDialog.cancel();

		if(!prefsMgr.isKeyguardPanelShown()
				&& !prefsMgr.isLauncherPanelShown()) {
			flashController.releaseInstance();
		}
		
		prefsMgr = null;
		flashController = null;
		flashView.releaseData();
	}

	private void showGuideView() {
		firstSetup = prefsMgr.getFirtStartDate() == 0;
		if (firstSetup) {

			prefsMgr.setFirstStartDate();
			firstSetup = false;
			guideViewMgr = new GuideViewManager(this.getWindowManager(),
					WindowManager.LayoutParams.TYPE_TOAST);
			MDisplayHelper displayHelper = new MDisplayHelper();
			int flashViewGuideY = displayHelper.dpiToPx(25);

			TextView guideView = new TextView(this);
			guideView.setBackgroundResource(R.drawable.guide_text);
			guideViewMgr.add(guideView, Gravity.CENTER_HORIZONTAL
					| Gravity.BOTTOM, 0, flashViewGuideY, true);
		}
	}

	private void closeGuideView() {
		if (guideViewMgr != null) {
			guideViewMgr.close();
			guideViewMgr = null;
		}
	}

	private void initLisense() {
		boolean purchased = flashController.isPurchased();
		if (!purchased) {
			lisenseManager = new MLisenseMangaer(this);
			lisenseManager.bindRemoteService();
		}
	}

	@Override
	public void onRemoteServiceConnected() {
		int lisenseState = lisenseManager.doRemoteCheck();
		flashController.setLisenseState(lisenseState);
		lisenseManager.unbindRemoteService();

		refreshWhenLisenseChanged(flashController.islisenseEnable());
	}

	@Override
	public void onRemoteServiceDisconnected() {
		lisenseManager = null;
	}

	private void refreshWhenLisenseChanged(boolean isLisenseEnable) {
		if (this.isLisenseEnable != isLisenseEnable) {
			// 之后就直接setFlashLevel就好
			flashView.setLisenseState(isLisenseEnable,
					flashController.getCurrentLevel());
			if (isLisenseEnable) {
				btnLock.setVisibility(View.GONE);
			} else {
				btnLock.setVisibility(View.VISIBLE);
			}
			this.isLisenseEnable = isLisenseEnable;
		}
	}

	private void initLockAlert() {
		if (mLockDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this,
					android.R.style.Theme_Holo_Light_Panel);
			builder.setIcon(R.drawable.ic_logo_locked);
			builder.setTitle(R.string.alert_lock_title);
			builder.setMessage(R.string.alert_lock_message);

			builder.setPositiveButton(R.string.alert_lock_positive,
					new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 跳转到应用中心
							connector
									.jumpToMarket(LightmeConstants.APP_IDENTIFY);
						}
					});

			builder.setNeutralButton(R.string.alert_lock_neutral,
					new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 联系开发者
							connector.sendLockMsgWithMail(flashController
									.getLisenseState());
						}
					});

			builder.setNegativeButton(R.string.alert_lock_negetive,
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							initLisense();
						}
					});

			mLockDialog = builder.create();
			mLockDialog.getWindow().setType(
					WindowManager.LayoutParams.TYPE_APPLICATION);
		}
	}
}
