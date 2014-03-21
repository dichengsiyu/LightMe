package com.hellodev.lightme.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.hellodev.lightme.FlashApp;
import com.hellodev.lightme.FlashController;
import com.hellodev.lightme.receiver.SystemReceiver;
import com.hellodev.lightme.util.MLisenseMangaer;
import com.hellodev.lightme.util.MNotificationHelper;
import com.hellodev.lightme.util.MPreferenceManager;
import com.hellodev.lightme.util.ShakeDetector;
import com.hellodev.lightme.util.MLisenseMangaer.OnLisenseStateChangeListener;
import com.hellodev.lightme.util.ShakeDetector.OnShakeListener;
import com.hellodev.lightme.view.KeyguardPanelManager;
import com.hellodev.lightme.view.LauncherPanelManager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PanelService extends Service implements OnShakeListener, OnLisenseStateChangeListener {
	private final static String TAG = "ControlService";

	public static final String CONTROL_TYPE_KEY = "control_type";
	public static final int CONTROL_TYPE_DEFAULT = 0;
	public static final int CONTROL_TYPE_START = 1;
	public static final int CONTROL_TYPE_STOP = 2;
	public static final int CONTROL_TYPE_SCREEN_OFF = 3;
	public static final int CONTROL_TYPE_SCREEN_ON = 4;
	public static final int CONTROL_TYPE_USER_PRESENT = 5;

	public final static String ACTION_LAUNCHER = "hellodev.service.action.LAUNCHER";
	public final static String ACTION_KEYGUARD = "hellodev.service.action.KEYGUARD";
	public final static String ACTION_PANEL_SERVICE = "hellodev.service.action.PANEL_SERVICE";

	private LauncherPanelManager mLauncherPanelManager;
	private KeyguardPanelManager mKeyguardPanelManager;
	private Timer mLauncherRefreshTimer;
	private LauncherRefreshTask mLauncherRefreshTask;
	private ActivityManager mActivityManager;
	private Vibrator mVibrator;
	private SystemReceiver mSystemReceiver;
	private MPreferenceManager mPrefsManager;
	private ShakeDetector mShakeDetector;
	private TelephonyManager mTelephonyManager;
	private PhoneStateListener mPhoneStateListener;
	private KeyguardManager mKeyguardManager;
	private Handler mHandler = new Handler();
	private FlashController flashController;

	private boolean isKeyguardServiceAlive, isLauncherServiceAlive;
	private boolean isHomeLastInterval = false;
	
	private MLisenseMangaer lisenseManager;

	@Override
	public void onCreate() {
		super.onCreate();

		Context appContext = FlashApp.getContext();
		flashController = FlashController.getInstance();
		mPrefsManager = MPreferenceManager.getInstance();
		
		mActivityManager = (ActivityManager) appContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		mVibrator = (Vibrator) appContext
				.getSystemService(Context.VIBRATOR_SERVICE);
		
		isKeyguardServiceAlive = isLauncherServiceAlive = false;
		mLauncherRefreshTimer = new Timer();
		mShakeDetector = new ShakeDetector(appContext);
		mShakeDetector.registerOnShakeListener(this);
		mKeyguardManager = (KeyguardManager) appContext
				.getSystemService(Context.KEYGUARD_SERVICE);
		//这个只在锁屏界面才需要监听
		mPhoneStateListener = new PhoneStateListener() {
			int lastState = TelephonyManager.CALL_STATE_IDLE;
			@Override
			public void onCallStateChanged(int state,
					String incomingNumber) {
				if(state != lastState && isKeyguardServiceAlive) {
					if(state == TelephonyManager.CALL_STATE_IDLE) {
						mKeyguardPanelManager.showPanel();
						if (mPrefsManager.isKeyguardShockEnable())
							mShakeDetector.start();//FIXME需要测试
					} else if(state == TelephonyManager.CALL_STATE_RINGING) {
						mKeyguardPanelManager.hidePanel();
						if (mPrefsManager.isKeyguardShockEnable())
							mShakeDetector.stop();
						
					}
				}
				lastState = state;
				super.onCallStateChanged(state, incomingNumber);
			}
		};
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand()");
		if (intent != null) {
			String action = intent.getAction();
			int controlType = intent.getIntExtra(CONTROL_TYPE_KEY,
					CONTROL_TYPE_DEFAULT);
			if (CONTROL_TYPE_START == controlType)
				handleStart(action);
			else if (CONTROL_TYPE_STOP == controlType)
				handleStop(action);
			else if (controlType == CONTROL_TYPE_SCREEN_ON)
				handleScreenOn();
			else if (controlType == CONTROL_TYPE_SCREEN_OFF)
				handleScreenOff();
			else if (controlType == CONTROL_TYPE_USER_PRESENT)
				handleUserPresent();
		} else {
			Log.v(TAG, "onStartCommand() restart after kill");
			handleStart(ACTION_PANEL_SERVICE);
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		releaseData();
	}

	private void releaseData() {
		stopLauncherPanel();
		stopKeyguardPanel();
		flashController = null;
		mActivityManager = null;
		mVibrator = null;
		mSystemReceiver = null;
		mPrefsManager = null;
		
		if(mShakeDetector != null) {
			mShakeDetector.removeOnShakeListener(this);
		}
		
		//关闭监听
		if(mTelephonyManager != null) {
			mTelephonyManager.listen(mPhoneStateListener
					, PhoneStateListener.LISTEN_NONE);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/*
	 * 调用时机 1. 重启的时候：BootComplete 2. 设置：SettingActivity
	 * 
	 * * 需要根据preference值，因为可能是重启时的调用
	 */
	private void handleStart(String action) {
		if (!isLauncherServiceAlive && !isKeyguardServiceAlive) {
			Log.v(TAG, "start receiver");
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_USER_PRESENT);
			mSystemReceiver = new SystemReceiver();
			FlashApp.getContext().registerReceiver(mSystemReceiver, filter);
		}

		if (ACTION_LAUNCHER.equals(action))
			startLauncherPanel();
		else if (ACTION_KEYGUARD.equals(action))
			startKeyguardPanel();
		else {
			if (mPrefsManager.isLauncherPanelShown())
				startLauncherPanel();
			if (mPrefsManager.isKeyguardPanelShown())
				startKeyguardPanel();
		}
	}

	/*
	 * 调用时机 1. 设置 * 覆盖安装之后service没有起来，没有执行过start，这个时候不能执行unregisterReceiver
	 */
	private void handleStop(String action) {
		if (ACTION_LAUNCHER.equals(action))
			stopLauncherPanel();
		else if (ACTION_KEYGUARD.equals(action))
			stopKeyguardPanel();
		else {
			// 暂时未使用
			stopLauncherPanel();
			stopKeyguardPanel();
		}

		if (!isLauncherServiceAlive && !isKeyguardServiceAlive) {
			if (mSystemReceiver != null)
				FlashApp.getContext().unregisterReceiver(mSystemReceiver);
			mSystemReceiver = null;
			
			stopSelf();
		}
	}

	/*
	 * 调用时机： 1. 屏幕点亮的时候：（仍然处在锁屏幕） 2. 呼吸灯上滑解锁的时候：（发生在user_present之后）
	 * 
	 *  不在锁屏都关闭：电话的时候判断状态 * 如果在锁屏界面则打开锁屏panel
	 *  如果lisense过期，关闭Keyguard
	 *  
	 *  如果在锁屏界面才监听电话
	 */
	private void handleScreenOn() {
		initLisense();
		if (!isKeyguardScreen()) {
			if (isKeyguardServiceAlive)
				mKeyguardPanelManager.hidePanel();
		} else {
			if (isKeyguardServiceAlive) {
				if(isTelephoneCalling()) {
					mKeyguardPanelManager.hidePanel();
				} else {
					mKeyguardPanelManager.showPanel();//灭屏、亮屏都show
					mKeyguardPanelManager.showHint();
					if (mPrefsManager.isKeyguardShockEnable()) {
						mShakeDetector.start();
					}
				}
				
				//锁屏界面需要监听电话状态
				mTelephonyManager.listen(mPhoneStateListener
					, PhoneStateListener.LISTEN_CALL_STATE);
			}
		}
		
		//FIXME 需要优化的策略，原则1. 尽量节省资源 2. 不要影响效果
		if(!flashController.isFlashOn()) {
			flashController.initCameraSync();
		}
	}

	/*
	 * 调用时机： 1. 灭屏的时候 2. 通话的时候
	 * 
	 * * 灭屏幕的时候是否会被杀掉，杀掉之后的处理方式
	 */
	private void handleScreenOff() {
		if (isLauncherServiceAlive && !isTelephoneCalling()) {
			mLauncherRefreshTask.cancelSelf();
			hideLauncherPanel();
		}

		if (isKeyguardServiceAlive) {
			mShakeDetector.stop();
			if (!isTelephoneCalling())
				mKeyguardPanelManager.showPanel();
			
			//关闭监听
			if(mTelephonyManager != null) {
				mTelephonyManager.listen(mPhoneStateListener
						, PhoneStateListener.LISTEN_NONE);
			}
		}
		
		//FIXME 需要优化的策略
		if(!flashController.isFlashOn()) {
			flashController.releaseCamera();
		}
	}

	/*
	 * 调用时机： 1. 解锁的时候：先screen_on再user_present 2.
	 * 直接滑动呼吸灯解锁：先user_present再screen_on
	 * 
	 * * 覆盖安装会kill掉process，不会调用onDestroy，service也不会自动重启
	 */
	private void handleUserPresent() {
		if (isKeyguardServiceAlive) {
			mKeyguardPanelManager.hidePanel();
			if (mPrefsManager.isKeyguardShockEnable())
				mShakeDetector.stop();
			
			//关闭监听
			if(mTelephonyManager != null) {
				mTelephonyManager.listen(mPhoneStateListener
						, PhoneStateListener.LISTEN_NONE);
			}
		}
		
		if(isLauncherServiceAlive)
			startLauncherRefreshTask(0, 1000);
	}

	private void startLauncherPanel() {
		if (!isLauncherServiceAlive) {
			isLauncherServiceAlive = true;
			if (mLauncherPanelManager == null)
				mLauncherPanelManager = new LauncherPanelManager();
			if (!isKeyguardScreen()) {
				// 如果覆盖安装了，这个时候启动LauncherPanel不应该开始轮询，而应该等到user_present的时候
				startLauncherRefreshTask(0, 1000);
			}
			
			//foreground
			startForeground(1000, new Notification());
		}
	}

	private void stopLauncherPanel() {
		if (isLauncherServiceAlive) {
			isLauncherServiceAlive = false;
			mLauncherRefreshTask.cancelSelf();
			// closePanel
			mLauncherPanelManager.closePanel();
			mLauncherPanelManager = null;
			
			// stopService
			//FIXME 这样会导致只打开锁屏的时候会有一段时间关闭锁屏开关
			stopForeground(true);
		}
	}

	/*
	 * 如果lisense过期，关闭Launcher
	 */
	private void requestShowLauncherPanel() {
		// 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。postRunnable的原因是因为timerTask
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (isLauncherServiceAlive) {
					showLauncherPanel();
				}
			}
		});
	}

	// 灭屏幕的时候应该是cancel掉任务，present的时候应该是show那个view，ok了之后启动桌面轮询task
	private void requestHideLauncherPanel() {
		// 当前界面不是桌面，且有悬浮窗显示，则移除悬浮窗。
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (isLauncherServiceAlive) {
					hideLauncherPanel();
				}
			}
		});
	}
	
	private void showLauncherPanel() {
		mLauncherPanelManager.showPanel();
		isHomeLastInterval = true;
	}
	
	private void hideLauncherPanel() {
		mLauncherPanelManager.hidePanel();
		isHomeLastInterval = false;
	}

	/*
	 * 调用时机： 1. 打开设置项 2. 开机启动的时候
	 */
	private void startKeyguardPanel() {
		if (isKeyguardServiceAlive == false) {
			if (mKeyguardPanelManager == null)
				mKeyguardPanelManager = new KeyguardPanelManager();
			isKeyguardServiceAlive = true;
		}
	}

	/*
	 * 调用时机 1. 关闭设置项的时候 2.长按移除的时候
	 */
	private void stopKeyguardPanel() {
		if (isKeyguardServiceAlive) {
			isKeyguardServiceAlive = false;
			mKeyguardPanelManager.closePanel();
			mKeyguardPanelManager = null;
			
			//如果是可以摇动的时候
			mShakeDetector.stop();
		}
	}

	private boolean isHome() {
		boolean isHome = false;
//		if(mActivityManager != null) {
			List<RunningTaskInfo> tasks = mActivityManager.getRunningTasks(1);
			isHome = getHomes().contains(tasks.get(0).topActivity.getClassName());
//		}
		return isHome;
	}

	private List<String> getHomes() {
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = this.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(
				intent, PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo ri : resolveInfo) {
			names.add(ri.activityInfo.name);
		}
		return names;
	}

	@Override
	public void onShake() {
		mVibrator.vibrate(200);
		flashController.toggleFlash();
		flashController.notifyFlashLevelChanged();//这是一个全局的操作，虽然现在只是keyguard在observer中
	}

	private boolean isTelephoneCalling() {
		if (mTelephonyManager == null)
			mTelephonyManager = (TelephonyManager) FlashApp.getContext()
					.getSystemService(Context.TELEPHONY_SERVICE);
		boolean calling = false;
		int status = mTelephonyManager.getCallState();
		if (status == TelephonyManager.CALL_STATE_OFFHOOK
				|| status == TelephonyManager.CALL_STATE_RINGING) {
			calling = true;
		}
		return calling;
	}

	private boolean isKeyguardScreen() {
		return mKeyguardManager.isKeyguardLocked();
	}
	
	//FIXME 这个地方还是有闪动的情况
	private void startLauncherRefreshTask(long delay, long period) {
		if(mLauncherRefreshTask == null) {
			mLauncherRefreshTask = new LauncherRefreshTask();
		} else {
			if(mLauncherRefreshTask.isRunning())
				mLauncherRefreshTask.cancelSelf();
			mLauncherRefreshTask = new LauncherRefreshTask();
		}
		
		mLauncherRefreshTimer.schedule(mLauncherRefreshTask, delay, period);
	}
	
	private class LauncherRefreshTask extends TimerTask {
		boolean isLauncherTaskCanceledOrStoped = false;
		@Override
		public void run() {
			if (isLauncherTaskCanceledOrStoped) {
				requestHideLauncherPanel();
				this.cancel();
			} else {
				boolean isHome = isHome();
				if (isHome && isHomeLastInterval == false)
					requestShowLauncherPanel();
				else if(isHome == false && isHomeLastInterval == true)
					requestHideLauncherPanel();
			}
		}
		
		//isCurrent标记当前task是否需要被取代
		void cancelSelf() {
			isLauncherTaskCanceledOrStoped = true;
		}
		
		boolean isRunning() {
			return !isLauncherTaskCanceledOrStoped;
		}
	}
	
	private void initLisense() {
		boolean purchased = flashController.isPurchased();
		if(!purchased) {
			lisenseManager = new MLisenseMangaer(this);
			lisenseManager.bindRemoteService();
		}
	}

	@Override
	public void onRemoteServiceConnected() {
		int lisenseState = lisenseManager.doRemoteCheck();
		flashController.setLisenseState(lisenseState);
		lisenseManager.unbindRemoteService();
		
		if(flashController.islisenseEnable() == false) {
			handleStop(ACTION_PANEL_SERVICE);
			mPrefsManager.setNeedRefreshSetting(true);
			MNotificationHelper notify = new MNotificationHelper();
			notify.notifyPanelCloseWhenLock();
		}
	}

	@Override
	public void onRemoteServiceDisconnected() {
		lisenseManager = null;
	}
}