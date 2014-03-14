package com.hellodev.lightme.activity;

import com.hellodev.lightme.FlashController;
import com.hellodev.lightme.R;
import com.hellodev.lightme.service.ServiceHelper;
import com.hellodev.lightme.util.CommonDataHelper;
import com.hellodev.lightme.util.MConnectHelper;
import com.hellodev.lightme.util.MNotificationHelper;
import com.hellodev.lightme.util.MPreferenceManager;
import com.hellodev.lightme.util.SmartBarUtils;
import com.hellodev.lightme.view.SeekPreference;
import com.umeng.analytics.MobclickAgent;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.CheckBoxPreference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class SettingActivity extends PreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener, OnClickListener {
	private CheckBoxPreference showLauncherPanel, showKeyguardPanel,
			enableKeyguardShock, enableSwitchSound;
	private PreferenceScreen about, market, version;
	private SeekPreference autoCloseTime;

	public final static String KEY_ABOUNT = "about";
	public final static String KEY_MARKET = "market";
	public final static String KEY_VERSION = "version";

	private MConnectHelper connectHelper;
	private MNotificationHelper mNotifyHelper;
	private FlashController flashController;
	private MPreferenceManager mPrefsMgr;
	
	private boolean lisenseEnable = true;
	private ImageButton btnLock;
	private AlertDialog mLockDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		flashController = FlashController.getInstance();
		initActionBar();
		initPreference();
		initPreferenceWidget();

		connectHelper = new MConnectHelper(this);
		mNotifyHelper = new MNotificationHelper();
		mPrefsMgr = MPreferenceManager.getInstance();
		mPrefsMgr.setNeedRefreshSetting(false);
	}

	@Override
	protected void onResume() {
		MobclickAgent.onResume(this);
		refreshWhenPanelSettingChange();
		refreshWhenLisenseChange(flashController.islisenseEnable());
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		flashController = null;
	}

	@SuppressWarnings("deprecation")
	private void initPreference() {
		addPreferencesFromResource(R.xml.setting);
	}

	@SuppressWarnings("deprecation")
	private void initPreferenceWidget() {
		showLauncherPanel = (CheckBoxPreference) findPreference(MPreferenceManager.KEY_SHOW_LAUNCHER_PANEL);
		showLauncherPanel.setOnPreferenceChangeListener(this);

		showKeyguardPanel = (CheckBoxPreference) findPreference(MPreferenceManager.KEY_SHOW_KEYGUARD_PANEL);
		showKeyguardPanel.setOnPreferenceChangeListener(this);

		enableKeyguardShock = (CheckBoxPreference) findPreference(MPreferenceManager.KEY_KEYGURAD_SHOCK_ENABLE);
		enableKeyguardShock.setOnPreferenceChangeListener(this);

		enableSwitchSound = (CheckBoxPreference) findPreference(MPreferenceManager.KEY_ENABLE_SWITCH_SOUND);
		enableSwitchSound.setOnPreferenceChangeListener(this);

		autoCloseTime = (SeekPreference) findPreference(MPreferenceManager.KEY_AUTO_CLOSE_TIME);
		autoCloseTime.setOnPreferenceChangeListener(this);

		about = (PreferenceScreen) findPreference(KEY_ABOUNT);
		about.setOnPreferenceClickListener(this);
		
		market = (PreferenceScreen) findPreference(KEY_MARKET);
		market.setOnPreferenceClickListener(this);
		
		version = (PreferenceScreen) findPreference(KEY_VERSION);
		version.setOnPreferenceClickListener(this);
		String versionStr = CommonDataHelper.getCurrentAppVersionInfo(this, 
				getResources().getString(R.string.format_version_banben));
		version.setTitle(versionStr);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		boolean hasHandled = true;
		if (preference == showLauncherPanel) {
			if (newValue.equals(true)) {
				MobclickAgent.onEvent(this,
						MPreferenceManager.KEY_SHOW_LAUNCHER_PANEL, "true");
				ServiceHelper.startLauncherPanelService();
				mNotifyHelper
						.notifyPanelOpen(MNotificationHelper.NOTIFICATION_TYPE_LAUCHER_PANEL);
			} else {
				MobclickAgent.onEvent(this,
						MPreferenceManager.KEY_SHOW_LAUNCHER_PANEL, "false");
				ServiceHelper.stopLauncherPanelService();
				mNotifyHelper
						.cancelPanelOpenNotify(MNotificationHelper.NOTIFICATION_TYPE_LAUCHER_PANEL);
			}
		} else if (preference == showKeyguardPanel) {
			if (newValue.equals(true)) {
				MobclickAgent.onEvent(this,
						MPreferenceManager.KEY_SHOW_KEYGUARD_PANEL, "true");
				ServiceHelper.startKeyguardPanelService();
				mNotifyHelper
						.notifyPanelOpen(MNotificationHelper.NOTIFICATION_TYPE_KEYGUARD_PANEL);
			} else {
				MobclickAgent.onEvent(this,
						MPreferenceManager.KEY_SHOW_KEYGUARD_PANEL, "false");
				enableKeyguardShock.setChecked(false);
				ServiceHelper.stopKeyguardPanelService();
				mNotifyHelper
						.cancelPanelOpenNotify(MNotificationHelper.NOTIFICATION_TYPE_KEYGUARD_PANEL);
			}
		} else if (preference == enableKeyguardShock) {
			if (newValue.equals(true)) {
				MobclickAgent.onEvent(this,
						MPreferenceManager.KEY_KEYGURAD_SHOCK_ENABLE, "true");
				mNotifyHelper
						.notifyPanelOpen(MNotificationHelper.NOTIFICATION_TYPE_KEYGUARD_SHOCK);
			} else {
				MobclickAgent.onEvent(this,
						MPreferenceManager.KEY_KEYGURAD_SHOCK_ENABLE, "false");
				mNotifyHelper
						.cancelPanelOpenNotify(MNotificationHelper.NOTIFICATION_TYPE_KEYGUARD_SHOCK);
			}
		} else if (preference == enableSwitchSound) {
			if (newValue.equals(true)) {
				MobclickAgent.onEvent(this,
						MPreferenceManager.KEY_ENABLE_SWITCH_SOUND, "true");
				flashController.enableSound();
			} else {
				MobclickAgent.onEvent(this,
						MPreferenceManager.KEY_ENABLE_SWITCH_SOUND, "false");
				flashController.disabelSound();
			}
		} else if (preference == autoCloseTime) {
			MobclickAgent.onEvent(this, MPreferenceManager.KEY_AUTO_CLOSE_TIME,
					String.valueOf(newValue));
			long autoCloseTimeMins = Long.parseLong(newValue
					.toString());
			flashController.requestAutoCloseTask(autoCloseTimeMins * 60000);
		} else {
			hasHandled = false;
		}
		return hasHandled;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		boolean hasHandled = true;
		if (preference == about) {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		} else if (preference == market) {
			Intent intent = new Intent(this, GuideActivity.class);
			intent.putExtra(GuideActivity.KEY_FLAG_FROM, SettingActivity.class.getName());
			startActivity(intent);
		} else if (preference == version) {
			Intent intent = new Intent(this, GuideActivity.class);
			intent.putExtra(GuideActivity.KEY_FLAG_FROM, SettingActivity.class.getName());
			startActivity(intent);			
		} else{
			hasHandled = false;
		}
		return hasHandled;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	private void initActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_CUSTOM);

		/* 初始化时，认为侧边栏关闭，允许点击标题栏图标,同时返回主界面 */
		actionBar.setDisplayHomeAsUpEnabled(true);
		int icon = flashController.islisenseEnable()? R.drawable.ic_logo: R.drawable.ic_logo_locked;
		actionBar.setIcon(icon);
		SmartBarUtils.setBackIcon(actionBar,
				getResources().getDrawable(R.drawable.ic_back));

		actionBar.setCustomView(R.layout.action_view_setting);
		LinearLayout customView = (LinearLayout) actionBar.getCustomView();
		ImageButton ibFeedback = (ImageButton) customView.findViewById(R.id.action_feedback);
		ibFeedback.setOnClickListener(this);
		
		btnLock = (ImageButton) customView.findViewById(R.id.btn_lock);
		btnLock.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.action_feedback:
			connectHelper.sendFeedbackWithMail();
			break;
		case R.id.btn_lock:
			initLockAlert();
			mLockDialog.show();
			break;
		default:
			break;
		}
	}
	
	private void refreshWhenLisenseChange(boolean lisenseEnable) {
		if(this.lisenseEnable != lisenseEnable) {
			this.lisenseEnable = lisenseEnable;
		
			if(lisenseEnable) {
				showLauncherPanel.setEnabled(true);
				showKeyguardPanel.setEnabled(true);
				enableKeyguardShock.setEnabled(true);
				
				btnLock.setVisibility(View.GONE);
			} else {
				showLauncherPanel.setEnabled(false);
				showKeyguardPanel.setEnabled(false);
				enableKeyguardShock.setEnabled(false);
				
				btnLock.setVisibility(View.VISIBLE);
			}
		}
	}
	
	private void refreshWhenPanelSettingChange() {
		if (mPrefsMgr.needRefreshSetting()) {
			// 为了防止在锁屏移除panel，然后再回到设置界面
			// FIXME 会闪烁一下，能不能局部刷新
			boolean dataShowLauncher = mPrefsMgr.isLauncherPanelShown();
			boolean uiShowLauncher = showLauncherPanel.isChecked();
			if(dataShowLauncher != uiShowLauncher) {
				showLauncherPanel.setOnPreferenceChangeListener(null);
				showLauncherPanel.setChecked(dataShowLauncher);
				showLauncherPanel.setOnPreferenceChangeListener(this);
			}
			
			boolean dataShowKeyguard = mPrefsMgr.isKeyguardPanelShown();
			boolean uiShowKeyguard = showKeyguardPanel.isChecked();
			if(dataShowKeyguard != uiShowKeyguard) {
				showKeyguardPanel.setOnPreferenceChangeListener(null);
				showKeyguardPanel.setChecked(dataShowKeyguard);
				showKeyguardPanel.setOnPreferenceChangeListener(this);

				if(dataShowKeyguard == false && enableKeyguardShock.isChecked()) {
					enableKeyguardShock.setOnPreferenceChangeListener(null);
					enableKeyguardShock.setChecked(false);
					enableKeyguardShock.setOnPreferenceChangeListener(this);
				}
			}
			
			mPrefsMgr.setNeedRefreshSetting(false);
		}
	}
	
	private void initLockAlert() {
		if (mLockDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					this,
					android.R.style.Theme_Holo_Light_Panel);
			builder.setIcon(R.drawable.ic_logo_locked);
			builder.setTitle(R.string.alert_lock_title);
			builder.setMessage(R.string.alert_lock_message);
			
			builder.setPositiveButton(R.string.alert_lock_positive,
					new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							//跳转到应用中心
						}
					});
			
				builder.setNeutralButton(R.string.alert_lock_neutral,
						new android.content.DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								//联系开发者
								connectHelper.sendLockMsgWithMail(flashController.getLisenseState());
							}
						});
				

				builder.setNegativeButton(R.string.alert_lock_negetive,
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});

			mLockDialog = builder.create();
			mLockDialog.getWindow().setType(
					WindowManager.LayoutParams.TYPE_APPLICATION);
		}
	}
}
