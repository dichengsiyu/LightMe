package com.hellodev.lightme.util;

import com.hellodev.lightme.FlashApp;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.preference.PreferenceManager;

public class MPreferenceManager {
	private static MPreferenceManager mInstance;
	private SharedPreferences mPrefs;
	
	public final static String KEY_AUTO_CLOSE = "auto_close";
	public final static String KEY_AUTO_CLOSE_TIME = "auto_close_time";
	
	public final static String KEY_SHOW_LAUNCHER_PANEL = "show_launcher_panel";
	public final static String KEY_PANEL_X = "panel_x";
	public final static String KEY_PANEL_Y = "panel_y";
	public final static String KEY_SHOW_KEYGUARD_PANEL = "show_keyguard_panel";
	public final static String KEY_KEYGURAD_SHOCK_ENABLE = "enable_keyguard_shock";
	public final static String KEY_VERSION_CODE = "version_code";
	public final static String KEY_FIRST_START_DATE = "first_start_date";
	public final static String KEY_FIRST_SHOW_LAUNCHER = "first_show_launcher";
	public final static String KEY_FIRST_SHOW_KEYGUARD = "first_show_keyguard";
	public final static String KEY_ENABLE_SWITCH_SOUND = "enable_switch_sound";
	public final static String KEY_NEED_REFRESH_SETTING = "key_need_refresh_setting";
	
	public final static String KEY_LOCAL_LISENSE_STATE = "local_lisense_state";
	public final static String KEY_LISENSE_EXPIRED_TIMEMILLS = "lisense_expired_timemills";
	
	private MPreferenceManager() {
		mPrefs =PreferenceManager.getDefaultSharedPreferences(FlashApp.getContext());
	}
	
	public static MPreferenceManager getInstance() {
		if(mInstance == null)
			mInstance = new MPreferenceManager();
		return mInstance;
	}
	
	public void toggleLauncherPanel(boolean setShown) {
		Editor edit = mPrefs.edit();
		edit.putBoolean(KEY_SHOW_LAUNCHER_PANEL, setShown);
		edit.commit();
	}
	
	public boolean isLauncherPanelShown() {
		return mPrefs.getBoolean(KEY_SHOW_LAUNCHER_PANEL, false);
	}
	
	public void setFirstShowLauncherDate() {
		Editor edit = mPrefs.edit();
		edit.putLong(KEY_FIRST_SHOW_LAUNCHER, System.currentTimeMillis());
		edit.commit();
	}
	
	public long getFirstShowLauncherDate() {
		return mPrefs.getLong(KEY_FIRST_SHOW_LAUNCHER, 0);
	}
	
	public void toggleKeyguardPanel(boolean setShown) {
		Editor edit = mPrefs.edit();
		edit.putBoolean(KEY_SHOW_KEYGUARD_PANEL, setShown);
		edit.commit();
	} 
	
	public boolean isKeyguardPanelShown() {
		return mPrefs.getBoolean(KEY_SHOW_KEYGUARD_PANEL, false);
	}
	
	public void toggleKeyguardShock(boolean enable) {
		Editor edit = mPrefs.edit();
		edit.putBoolean(KEY_KEYGURAD_SHOCK_ENABLE, enable);
		edit.commit();
	}
	
	public boolean isKeyguardShockEnable() {
		return mPrefs.getBoolean(KEY_KEYGURAD_SHOCK_ENABLE, false);
	}
	
	public void setFirstShowKeyguardDate() {
		Editor edit = mPrefs.edit();
		edit.putLong(KEY_FIRST_SHOW_KEYGUARD, System.currentTimeMillis());
		edit.commit();
	}
	
	public long getFirstShowKeyguardDate() {
		return mPrefs.getLong(KEY_FIRST_SHOW_KEYGUARD, 0);
	}
	
	public void setPanelPos(int x, int y) {
		Editor edit = mPrefs.edit();
		edit.putInt(KEY_PANEL_X, x);
		edit.putInt(KEY_PANEL_Y, y);
		edit.commit();
	}
	
	public Point getPanelPos() {
		int x = mPrefs.getInt(KEY_PANEL_X, -1);
		int y = mPrefs.getInt(KEY_PANEL_Y, -1);
		if(x == -1 || y == -1)
			return null;
		else
			return new Point(x, y);
	}
	
	public void clearPanelPos() {
		Editor edit = mPrefs.edit();
		edit.remove(KEY_PANEL_X);
		edit.remove(KEY_PANEL_Y);
		edit.commit();
	}
	
	public long getFirtStartDate() {
		return mPrefs.getLong(KEY_FIRST_START_DATE, 0);
	}
	
	public void setVersionCode(int versionCode) {
		Editor edit = mPrefs.edit();
		edit.putInt(KEY_VERSION_CODE, versionCode);
		edit.commit();
	}
	
	public int getVersionCode() {
		return mPrefs.getInt(KEY_VERSION_CODE, 0);
	}
	
	public void setFirstStartDate() {
		Editor edit = mPrefs.edit();
		edit.putLong(KEY_FIRST_START_DATE, System.currentTimeMillis());
		edit.commit();
	}
	
	public boolean isAutoClose() {
		return mPrefs.getBoolean(KEY_AUTO_CLOSE, false);
	}
	
	public int getAutoCloseTime() {
		return mPrefs.getInt(KEY_AUTO_CLOSE_TIME, 2);
	}
	
	public void setAutoCloseTime(int autoCloseTime) {
		Editor edit = mPrefs.edit();
		edit.putInt(KEY_AUTO_CLOSE_TIME, autoCloseTime);
		edit.commit();
	}
	
	public boolean isSwitchSoundOn() {
		return mPrefs.getBoolean(KEY_ENABLE_SWITCH_SOUND, true);
	}
	
	public int getLocalLisenseState() {
		return mPrefs.getInt(KEY_LOCAL_LISENSE_STATE, MLisenseMangaer.STATE_UNKNOWN);
	}
	
	public void setLocalLisenseState(int lisenseState) {
		Editor edit = mPrefs.edit();
		edit.putInt(KEY_LOCAL_LISENSE_STATE, lisenseState);
		edit.commit();
	}
	
	public long getExpiredTimeMills() {
		return mPrefs.getLong(KEY_LISENSE_EXPIRED_TIMEMILLS, 0);
	}
	
	public void setExpiredTimeMills(long expiredTimeMills) {
		Editor edit = mPrefs.edit();
		edit.putLong(KEY_LISENSE_EXPIRED_TIMEMILLS, expiredTimeMills);
		edit.commit();
	}
	
	public boolean needRefreshSetting() {
		return mPrefs.getBoolean(KEY_NEED_REFRESH_SETTING, false);
	}
	
	public void setNeedRefreshSetting(boolean needRefresh) {
		Editor edit = mPrefs.edit();
		edit.putBoolean(KEY_NEED_REFRESH_SETTING, needRefresh);
		edit.commit();
	}
}
