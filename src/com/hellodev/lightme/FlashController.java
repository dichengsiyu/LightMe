package com.hellodev.lightme;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

import com.hellodev.lightme.service.ServiceHelper;
import com.hellodev.lightme.util.MLisenseMangaer;
import com.hellodev.lightme.util.MPreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class FlashController {
	private final static String TAG = "FlashController";
	private static FlashController mInstance;

	public final static int LEVEL_MAX = 7;
	public final static int LEVEL_OFF = 0;
	private int currentLevel;

	private Parameters parameters;
	private Camera camera;

	private int switchSound;
	private SoundPool soundPool;
	private boolean isSoundInit;// 是否可以播放
	private boolean enableSound;// 是否可以开声音

	private long AUTO_CLOSE_TIME_INTERVAL = 0;
	private AlarmManager alarmManager;// FIXME这个可以Softreference
	private PendingIntent autoCloseRequest;
	private long flashOpenTimeMills = 0;

	private List<OnFlashLevelChangedListener> observers;
	private int lisenseState = -1;
	private boolean lisenseEnable;
	private FlashHelper flashHelper;

	private Object CAMERA_LOCK = new Object();
	private boolean isCameraInited = false;

	// FIXME 执行完毕之后是否需要显式去停止
	private Runnable cameraInitTask = new Runnable() {

		@Override
		public void run() {
			initCamera(hasCameraReleased());
		}
	};

	private FlashController() {
		lisenseEnable = true;
		currentLevel = LEVEL_OFF;
		flashHelper = new FlashHelper();
		enableSound = MPreferenceManager.getInstance().isSwitchSoundOn();
	}

	public static FlashController getInstance() {
		if (mInstance == null)
			mInstance = new FlashController();
		return mInstance;
	}

	// FIXME 在哪释放
	public void releaseInstance() {
		if (mInstance != null) {
			relaseSwitchSound();
			releaseAutoCloseMgr();
			releaseCamera();
			mInstance = null;
			observers.removeAll(observers);
		}
	}

	// FIXME 线程执行完毕之后需不需要停止
	public void initCameraSync() {
		Thread cameraInitThread = new Thread(cameraInitTask);
		cameraInitThread.start();
	}

	/*
	 * 回到桌面的时候需要调用一次
	 */
	public void initCamera() {
		initCamera(hasCameraReleased());
	}
	
	private void initCamera(boolean needReconnect) {
		synchronized (CAMERA_LOCK) {
			if(isCameraInited == false) {
				if (needReconnect && camera != null) {
					camera = null;
				}
				if (camera == null)
					camera = Camera.open();
				parameters = camera.getParameters();
				if (needReconnect && currentLevel > LEVEL_OFF) {
					currentLevel = LEVEL_OFF;
					notifyFlashLevelChanged();//重新打开需要通知对应的观察者
					turnFlashOffIfCameraReleased();
				}
				isCameraInited = true;
			}
		}
	}
	
	/*
	 * 关灯灭屏时释放Camera，亮屏幕时不着急调用，首次调用时打开
	 */
	public void releaseCamera() {
		synchronized (CAMERA_LOCK) {
			if (camera != null) {
				try {
					camera.release();
				} catch (RuntimeException re) {
					Log.v(TAG, "camera already released");
				}
				camera = null;
				isCameraInited = false;
			}
		}
	}

	public void initSwitchSound() {
		if (enableSound && (isSoundInit == false || soundPool == null)) {
			soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
			soundPool.setOnLoadCompleteListener(soundInitListener);
			switchSound = soundPool.load(FlashApp.getContext(),
					R.raw.flashswitch, 1);
		}
	}

	OnLoadCompleteListener soundInitListener = new OnLoadCompleteListener() {
		@Override
		public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
			isSoundInit = true;
		}
	};

	/*
	 * 关灯灭屏时释放，亮屏幕时不着急调用，首次调用时打开
	 */
	public void relaseSwitchSound() {
		if (soundPool != null && isSoundInit) {
			soundPool.release();
			soundPool = null;
			isSoundInit = false;
		}
	}

	public void enableSound() {
		enableSound = true;
		initSwitchSound();// 打开这个时，用户应该很快想去试试，所以提前加载
	}

	public void disabelSound() {
		enableSound = false;
		relaseSwitchSound();
	}

	// FIXME 这样还是不能确保，可能那一次没有声音
	public void playSwitchSound() {
		initSwitchSound();
		if (enableSound && isSoundInit)
			soundPool.play(switchSound, 1.5f, 1.5f, 0, 0, 1);
	}

	private void initAutoCloseMgr() {
		if (alarmManager == null) {
			alarmManager = (AlarmManager) FlashApp.getContext()
					.getSystemService(Context.ALARM_SERVICE);
		}
	}

	public void releaseAutoCloseMgr() {
		cancelAutoCloseTask();
		alarmManager = null;
		autoCloseRequest = null;
	}

	/*
	 * 调用时机 1. 开灯的时候 2. 调整设置项的时候 FIXME 如果时间已经过了
	 */
	private void requestAutoCloseTask() {
		requestAutoCloseTask(0);
	}
	
	public void requestAutoCloseTask(long autoCloseTimInterval) {
		initAutoCloseMgr();
		
		if(autoCloseTimInterval > 0) {
			AUTO_CLOSE_TIME_INTERVAL = autoCloseTimInterval;
		} else if(AUTO_CLOSE_TIME_INTERVAL == 0) {
			AUTO_CLOSE_TIME_INTERVAL = MPreferenceManager.getInstance().getAutoCloseTime() * 60000;
		}
		
		if (currentLevel > LEVEL_OFF) {
			long triggleAtMills = flashOpenTimeMills + AUTO_CLOSE_TIME_INTERVAL;
			Intent requestIntent = ServiceHelper.getAutoCloseIntent();
			autoCloseRequest = PendingIntent.getService(FlashApp.getContext(),
					0, requestIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager
					.set(AlarmManager.RTC, triggleAtMills, autoCloseRequest);
		}
	}

	public void cancelAutoCloseTask() {
		if (autoCloseRequest != null) {
			alarmManager.cancel(autoCloseRequest);
			autoCloseRequest = null;
		}
	}

	public int toggleFlash() {
		initCamera();
		if (currentLevel == LEVEL_OFF) {
			turnFlashOn();
		} else {
			turnFlashOff();
		}
		return currentLevel;
	}

	private void turnFlashOn() {
		if (currentLevel == LEVEL_OFF) {
			parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			try {
				camera.setParameters(parameters);
			} catch (RuntimeException re) {
				initCamera(true);
				parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			}
			currentLevel = LEVEL_MAX;
			flashOpenTimeMills = System.currentTimeMillis();

			requestAutoCloseTask();
			playSwitchSound();
		}
	}

	public void turnFlashOff() {
		if (currentLevel > LEVEL_OFF) {
			parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			try {
				camera.setParameters(parameters);
			} catch (RuntimeException re) {
				initCamera(true);
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			}
			currentLevel = LEVEL_OFF;
			flashOpenTimeMills = 0;
			cancelAutoCloseTask();

			playSwitchSound();
		}
	}
	
	public void turnFlashOffIfCameraReleased() {
		if(currentLevel > LEVEL_OFF) {
			currentLevel = LEVEL_OFF;
			flashOpenTimeMills = 0;
	
			cancelAutoCloseTask();
		}
	}
	/*
	 * 调节亮度需要lisenseEnable才可以
	 */
	public int turnFlashUp() {
		if (lisenseEnable && currentLevel < LEVEL_MAX) {
			flashHelper.changeFlashLight(true);
			currentLevel++;
		}
		return currentLevel;
	}

	public int turnFlashDown() {
		if (lisenseEnable && currentLevel > LEVEL_OFF + 1) {
			flashHelper.changeFlashLight(false);
			currentLevel--;
		}
		return currentLevel;
	}

	public boolean hasCameraReleased() {
		boolean hasReleased = false;
		if (camera != null) {
			try {
				parameters = camera.getParameters();
			} catch (RuntimeException re) {
				hasReleased = true;
				isCameraInited = false;
			}
		}
		return hasReleased;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public boolean isFlashOn() {
		return currentLevel > LEVEL_OFF;
	}

	public void notifyFlashLevelChanged() {
		if (observers != null) {
			for (OnFlashLevelChangedListener observer : observers) {
				observer.onFlashLevelChanged(currentLevel);
			}
		}
	}

	public void setLisenseState(int lisenseState) {
		if (this.lisenseState != lisenseState) {
			this.lisenseState = lisenseState;

			lisenseEnable = lisenseState == MLisenseMangaer.STATE_PURCHASED
					|| lisenseState == MLisenseMangaer.STATE_TRYING;
			
			if(!lisenseEnable) {
				//关闭对应设置项，并通知更新
				MPreferenceManager prefsMgr = MPreferenceManager.getInstance();
				prefsMgr.toggleKeyguardPanel(false, true);
				prefsMgr.toggleLauncherPanel(false, true);
				//FIXME preference的写入考虑使用队列的形式
			}
		}
	}
	
	public int getLisenseState() {
		return lisenseState;
	}
	
	public boolean islisenseEnable() {
		return lisenseEnable;
	}
	
	public boolean isPurchased() {
		return lisenseState == MLisenseMangaer.STATE_PURCHASED;
	}
	
	public void addObserver(OnFlashLevelChangedListener listener) {
		if (observers == null)
			observers = new ArrayList<OnFlashLevelChangedListener>();
		if (!observers.contains(listener)) {
			observers.add(listener);
		}
	}

	public void removeObserver(OnFlashLevelChangedListener listener) {
		if (observers != null && observers.contains(listener)) {
			observers.remove(listener);
		}
	}

	public interface OnFlashLevelChangedListener {
		public void onFlashLevelChanged(int currentLevel);
	}
}
