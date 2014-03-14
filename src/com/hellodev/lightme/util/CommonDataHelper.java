package com.hellodev.lightme.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

public class CommonDataHelper {
//	private final static String VERSION_FORMATE ="V %1$s";
	public static String getCurrentAppVersion(Context context) {
		String version = "1.0.0";
        PackageManager packageManager = context.getPackageManager();
		try {
			PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
			version = packInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
        return version;
	}
	
	public static String getCurrentAppVersionInfo(Context context, String format) {
        return String.format(format, getCurrentAppVersion(context));
	}
	
	public static int getCurrentAppVersionCode(Context context) {
		int versionCode = 1;
        PackageManager packageManager = context.getPackageManager();
		try {
			PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
			versionCode = packInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
        return versionCode;
	}
	
	public static String getDeviceInfo(Context context) {
		try {
			org.json.JSONObject json = new org.json.JSONObject();
			android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			String device_id = tm.getDeviceId();

			android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);

			String mac = wifi.getConnectionInfo().getMacAddress();
			json.put("mac", mac);

			if (TextUtils.isEmpty(device_id)) {
				device_id = mac;
			}

			if (TextUtils.isEmpty(device_id)) {
				device_id = android.provider.Settings.Secure.getString(
						context.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);
			}

			json.put("device_id", device_id);

			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
