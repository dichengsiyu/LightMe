package com.hellodev.lightme.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hellodev.lightme.service.ServiceHelper;

public class SystemReceiver extends BroadcastReceiver {
	private final static String TAG = "SystemReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent != null ? intent.getAction(): null;
		if(Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			ServiceHelper.startPanelService();
		} else if(Intent.ACTION_SCREEN_ON.equals(action)) {
			Log.v(TAG, "SCREEN_ON");
			ServiceHelper.callPanelServiceWhenScreenOn();
		} else if(Intent.ACTION_SCREEN_OFF.equals(action)) {
			Log.v(TAG, "SCREEN_OFF");
			ServiceHelper.callPanelServiceWhenScreenOff();
		} else if(Intent.ACTION_USER_PRESENT.equals(action)) {
			Log.v(TAG, "USER_PRESENT");
			ServiceHelper.callPanelServiceWhenUserPresent();
		}
	}
}
