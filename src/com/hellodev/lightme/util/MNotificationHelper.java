package com.hellodev.lightme.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;

import com.hellodev.lightme.FlashApp;
import com.hellodev.lightme.R;
import com.hellodev.lightme.activity.SettingActivity;

public class MNotificationHelper {
	public final static int NOTIFICATION_TYPE_LAUCHER_PANEL = 0;
	public final static int NOTIFICATION_TYPE_KEYGUARD_PANEL = 1;
	public final static int NOTIFICATION_TYPE_KEYGUARD_SHOCK = 2;
	public final static int NOTIFICATION_TYPE_CLOSE_PANEL = 3;
	
	private NotificationManager mManager;
	
	public MNotificationHelper() {
		Context context = FlashApp.getContext();
		mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void notifyPanelOpen(int notifyType) {
		Context context = FlashApp.getContext();
		Resources resource = context.getResources();
		String title = null;
		String content = null;
		String ticker = null;
		switch(notifyType) {
			case NOTIFICATION_TYPE_LAUCHER_PANEL:
				title = resource.getString(R.string.launcher_panel);
				content = resource.getString(R.string.notification_launcher_panel_open_content);
				ticker = resource.getString(R.string.notification_keyguard_shock_enable_ticker);
				break;
			case NOTIFICATION_TYPE_KEYGUARD_PANEL:
				title = resource.getString(R.string.keyguard_panel);
				content = resource.getString(R.string.notification_keyguard_panel_open_content);
				ticker = resource.getString(R.string.notification_keyguard_panel_open_ticker);
				break;	
			case NOTIFICATION_TYPE_KEYGUARD_SHOCK:
				title = resource.getString(R.string.keyguard_shock);
				content = resource.getString(R.string.notification_keyguard_shock_enable_content);
				ticker = resource.getString(R.string.notification_keyguard_shock_enable_ticker);
				break;
		}
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				FlashApp.getContext()).setContentTitle(title)
				.setSmallIcon(R.drawable.ic_logo)
				.setContentText(content).setTicker(ticker).setAutoCancel(true);
		
		Intent intent = new Intent(context, SettingActivity.class);
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pi);
		mManager.notify(notifyType, builder.build());
	}
	
	public void notifyPanelCloseWhenLock() {
		Context context = FlashApp.getContext();
		Resources resource = context.getResources();
		String title = resource.getString(R.string.notification_panel_close_when_lock_title);
		String content = resource.getString(R.string.notification_panel_close_when_lock_content);
		String ticker = resource.getString(R.string.notification_panel_close_when_lock_ticker);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				FlashApp.getContext()).setContentTitle(title)
				.setSmallIcon(R.drawable.ic_logo_locked)
				.setContentText(content).setTicker(ticker).setAutoCancel(true);
		
		Intent intent = new Intent(context, SettingActivity.class);
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pi);
		mManager.notify(NOTIFICATION_TYPE_CLOSE_PANEL, builder.build());
		cancelNotify(NOTIFICATION_TYPE_LAUCHER_PANEL);
		cancelNotify(NOTIFICATION_TYPE_KEYGUARD_PANEL);
		cancelNotify(NOTIFICATION_TYPE_KEYGUARD_SHOCK);
	}
	
	public void cancelNotify(int notifyType) {
		mManager.cancel(notifyType);
	}
}
