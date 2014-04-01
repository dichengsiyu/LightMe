package com.hellodev.lightme.service;

import com.hellodev.lightme.FlashApp;
import com.hellodev.lightme.FlashController;
import com.hellodev.lightme.R;
import com.hellodev.lightme.activity.SettingActivity;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

public class ControlService extends Service {
	private final static String TAG = "AUTO_CLOSE";
	public final static String CONTROL_TYPE_KEY = "control_type";
	public final static int CONTROL_TYPE_UNKNOWN = 0;
	public final static int CONTROL_TYPE_SHOW_ACDIALOG = 1;
	public final static int CONTROL_TYPE_CLOSE_ACDIALOG = 2;

	public final static String ACTION_AUTO_CLOSE = "hellodev.service.action.AUTO_CLOSE";

	private FlashController flashController;
	private AlertDialog mAutoCloseDialog;
	
	private boolean handlingAutoClose,checkingLisense;
	@Override
	public void onCreate() {
		super.onCreate();
		flashController = FlashController.getInstance();
		handlingAutoClose = checkingLisense = false;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String action = intent.getAction();
			if (ACTION_AUTO_CLOSE.equals(action)) {
				int controlType = intent.getIntExtra(CONTROL_TYPE_KEY, CONTROL_TYPE_UNKNOWN);
				if(controlType == CONTROL_TYPE_SHOW_ACDIALOG) {
					showAutoCloseCommand();
				}
			}
		}
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private void showAutoCloseCommand() {
		if(flashController.isFlashOn()) {
			//滑动滚动条
			flashController.turnFlashOff();
			flashController.notifyFlashLevelChanged();//自动关闭时
			if (mAutoCloseDialog == null)
				initAutoCloseAlert();
	
			mAutoCloseDialog.show();
			handlingAutoClose = true;
		}
	}
	
	private void initAutoCloseAlert() {
		if (mAutoCloseDialog == null) {
			final Context appContext = FlashApp.getContext();
			AlertDialog.Builder builder = new AlertDialog.Builder(
					appContext,
					android.R.style.Theme_Holo_Light_Panel);
			builder.setIcon(R.drawable.ic_logo);
			builder.setTitle(R.string.alert_auto_close_title);
			builder.setMessage(R.string.alert_auto_close_message);
			builder.setPositiveButton(R.string.alert_auto_close_positive,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							stopAutoCloseService();
						}
					});

			builder.setNeutralButton(R.string.alert_auto_close_neutral,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(appContext,
									SettingActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							appContext.startActivity(intent);
							dialog.cancel();
							stopAutoCloseService();
						}
					});

			builder.setNegativeButton(R.string.alert_auto_close_negetive,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							flashController.toggleFlash();
							flashController.notifyFlashLevelChanged();
							stopAutoCloseService();
						}
					});
			
			builder.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					stopAutoCloseService();//FIXME 点击其他区域关闭了dialog
				}
			});

			mAutoCloseDialog = builder.create();
			mAutoCloseDialog.getWindow().setType(
					WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		}
	}
	
	private void stopAutoCloseService() {
		handlingAutoClose = false;
		if(checkingLisense == false) {
			stopSelf();
		}
	}
}
