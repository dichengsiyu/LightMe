package com.hellodev.lightme.service;

import com.hellodev.lightme.FlashApp;
import com.hellodev.lightme.util.MLisenseMangaer;
import com.hellodev.lightme.util.MPreferenceManager;

import android.content.Intent;

public class ServiceHelper {
	public static void startLauncherPanelService() {
		Intent intent = new Intent(PanelService.ACTION_LAUNCHER);
		intent.putExtra(PanelService.CONTROL_TYPE_KEY,
				PanelService.CONTROL_TYPE_START);
		FlashApp.getContext().startService(intent);
	}

	public static void stopLauncherPanelService() {
		Intent intent = new Intent(PanelService.ACTION_LAUNCHER);
		intent.putExtra(PanelService.CONTROL_TYPE_KEY,
				PanelService.CONTROL_TYPE_STOP);
		FlashApp.getContext().startService(intent);
	}

	public static void startKeyguardPanelService() {
		Intent intent = new Intent(PanelService.ACTION_KEYGUARD);
		intent.putExtra(PanelService.CONTROL_TYPE_KEY,
				PanelService.CONTROL_TYPE_START);
		FlashApp.getContext().startService(intent);
	}

	public static void stopKeyguardPanelService() {
		Intent intent = new Intent(PanelService.ACTION_KEYGUARD);
		intent.putExtra(PanelService.CONTROL_TYPE_KEY,
				PanelService.CONTROL_TYPE_STOP);
		FlashApp.getContext().startService(intent);
	}

	public static void callPanelServiceWhenScreenOn() {
		Intent intent = new Intent(PanelService.ACTION_PANEL_SERVICE);
		intent.putExtra(PanelService.CONTROL_TYPE_KEY,
				PanelService.CONTROL_TYPE_SCREEN_ON);
		FlashApp.getContext().startService(intent);
	}

	public static void callPanelServiceWhenScreenOff() {
		Intent intent = new Intent(PanelService.ACTION_PANEL_SERVICE);
		intent.putExtra(PanelService.CONTROL_TYPE_KEY,
				PanelService.CONTROL_TYPE_SCREEN_OFF);
		FlashApp.getContext().startService(intent);
	}

	public static void callPanelServiceWhenUserPresent() {
		Intent intent = new Intent(PanelService.ACTION_PANEL_SERVICE);
		intent.putExtra(PanelService.CONTROL_TYPE_KEY,
				PanelService.CONTROL_TYPE_USER_PRESENT);
		FlashApp.getContext().startService(intent);
	}

	/*
	 * service启动入口
	 * 1. MainActivity界面，用户点击桌面icon进入
	 * 2. 重启，boot_complete广播
	 *
	 * 
	 */
	public static void startPanelService() {
		MPreferenceManager mPrefsMgr = MPreferenceManager.getInstance();
		boolean isLauncherPanelService = mPrefsMgr.isLauncherPanelShown();
		boolean isKeyguardPanelService = mPrefsMgr.isKeyguardPanelShown();
		if (isLauncherPanelService && isKeyguardPanelService) {
			Intent intent = new Intent(PanelService.ACTION_PANEL_SERVICE);
			intent.putExtra(PanelService.CONTROL_TYPE_KEY,
					PanelService.CONTROL_TYPE_START);
			FlashApp.getContext().startService(intent);
		} else if (isLauncherPanelService) {
			startLauncherPanelService();
		} else if (isKeyguardPanelService) {
			startKeyguardPanelService();
		}
	}
	
	public static void stopPanelService() {
		Intent intent = new Intent(PanelService.ACTION_PANEL_SERVICE);
		intent.putExtra(PanelService.CONTROL_TYPE_KEY,
				PanelService.CONTROL_TYPE_STOP);
		FlashApp.getContext().startService(intent);
	}
	
	public static Intent getAutoCloseIntent() {
		Intent requestIntent = new Intent(
				ControlService.ACTION_AUTO_CLOSE);
		requestIntent.putExtra(ControlService.CONTROL_TYPE_KEY, ControlService.CONTROL_TYPE_SHOW_ACDIALOG);
		return requestIntent;
	}
}
