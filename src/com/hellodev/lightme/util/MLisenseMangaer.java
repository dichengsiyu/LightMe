package com.hellodev.lightme.util;

import java.util.Calendar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.hellodev.lightme.FlashApp;
import com.hellodev.lightme.R;
import com.meizu.mstore.license.ILicensingService;
import com.meizu.mstore.license.LicenseCheckHelper;
import com.meizu.mstore.license.LicenseResult;

public class MLisenseMangaer {
	private final static String TAG = "MLisenseMangaer";

	public final static int STATE_UNKNOWN = 0;
	public final static int STATE_PURCHASED = 1;
	public final static int STATE_TRYING = 2;
	public final static int STATE_EXPIRED = 3;

	public final static int EXPIRE_DAYS = 3;
	public final static String APK_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCB4xYVRA7w13J6dBWA+oERVDRnefb8J5N7zeD3e6JfExk76F59YRDbmDVPL+ztwwboy+R3RSQKIHM/DYymqAHnbq4m/tKXCS6BcL9MeH6Cc2xas2dNNfzOjDrb/5oqvzHI+0TPX3gU2ZML3Udsw4Q+QT+jYOgLbMYfbps3OZne5QIDAQAB/wJQnyPGfR72CNv5zR+qA4qjxSMBUSQh55awBgR4Jrwd3G+6/yH540pB/oP+GsTp0Sof/dEFaR85968aEhBGRcnpEl9OITISZRwMp654/LD6kzdsjMBjfPXiYRSLjygcbG//gOVxZbnmU2Nz5pnuFvav8wIDAQAB";

	private Context mContext;
	// 服务的实例对象
	private ILicensingService mLicensingService = null;
	private OnLisenseStateChangeListener mListener;

	public MLisenseMangaer() {
		// for local check
		this(null);
	}

	public MLisenseMangaer(OnLisenseStateChangeListener listener) {
		mListener = listener;
		mContext = FlashApp.getContext();
	}

	// 服务绑定的回调，可用于判断服务是否绑定成功
	private ServiceConnection mLicenseConnection = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
			if (mListener != null)
				mListener.onRemoteServiceDisconnected();
			mLicensingService = null;
		}

		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.v(TAG, "服务绑定成功，获取服务实例");
			mLicensingService = ILicensingService.Stub.asInterface(service);
			if (mListener != null)
				mListener.onRemoteServiceConnected();
		}
	};

	public void bindRemoteService() {
		if (mLicensingService == null) {
			// 绑定服务
			Intent serviceIntent = new Intent();
			serviceIntent.setAction(ILicensingService.class.getName());
			mContext.bindService(serviceIntent, mLicenseConnection,
					Context.BIND_AUTO_CREATE);
		} else {
			if (mListener != null)
				mListener.onRemoteServiceConnected();
		}
	}

	public void unbindRemoteService() {
		// 如果服务已经绑定，在退出时需要反绑服务
		if (mLicensingService != null) {
			mContext.unbindService(mLicenseConnection);
		}
	}

	// doCheck()方法演示了如何使用license服务来验证授权是否合法
	public int doRemoteCheck() {

		// 该第三方应用的包名,可使用getApplication().getPackageName()获取本应用包名
		final String packageName = mContext.getPackageName();

		// 保存服务验证的结果值
		LicenseResult result = null;
		int lisenseState = STATE_UNKNOWN;
		if (mLicensingService != null) {
			try {
				// 调用服务接口进行验证,获取返回的结果值
				// FIXME 为什么有的时候为空
				result = mLicensingService.checkLicense(packageName);
			} catch (RemoteException e) {
				Log.e(TAG, "获取证书失败");
				// 作为UNKNOWN来处理，这种异常不需要显示错误
			}

			if (result != null
					&& result.getResponseCode() == LicenseResult.RESPONSE_CODE_SUCCESS) {
				// license验证服务验证通过,需要接着对服务返回的结果再次进行校验(使用自己的公钥进行验证)
				boolean bSuccess = LicenseCheckHelper.checkResult(
						APK_PUBLIC_KEY, result);

				if (bSuccess
						&& result.getPurchaseType() == LicenseResult.PURCHASE_TYPE_NORMAL) {
					// 验证成功，并且为正式版本
					lisenseState = STATE_PURCHASED;

					// FIXME 获取子产品
				} else if (result.getPurchaseType() == LicenseResult.PURCHASE_TYPE_TRIAL) {
					// 验证成功，是试用版本
					// 该方法返回license文件的生成日期（注:该日期的值只精确到天，即时分秒为随机值）
					Calendar beginCal = result.getStartDate();
					// Log.v("", "试用开始的日期为: " + beginCal.get(Calendar.YEAR) +
					// "年"
					// + (beginCal.get(Calendar.MONTH) + 1) + "月"
					// + beginCal.get(Calendar.DAY_OF_MONTH) + "日");
					/**
					 * 以下是可供参考的过期判断
					 */
					// 你自己定义的过期天数
					final int expireDays = 0;
					// 获取当前日期
					Calendar nowCal = Calendar.getInstance();
					// 求剩余的天数
					long dif = nowCal.getTimeInMillis()
							- beginCal.getTimeInMillis();
					int passDay = (int) (dif / (24 * 60 * 60 * 1000));
					int left = expireDays - passDay;
					if (left > 0) {
						// 未过期
						Log.v(TAG, "未过期");
						lisenseState = STATE_TRYING;
					} else {
						// 过期
						Log.e(TAG, "过期");
						lisenseState = STATE_EXPIRED;
					}

					// FIXME 获取子产品
				} else {
					// 验证不成功或者版本类型不对，可按试用版处理
					// FIXME 状态一定要对，作为UNKNOWN来处理
					lisenseState = STATE_TRYING;
					Log.e(TAG, "license验证不通过");
				}
			} else {
				// license验证服务验证不通过
				lisenseState = STATE_UNKNOWN;// 用友盟来统计？
				if (result.getResponseCode() == LicenseResult.RESPONSE_CODE_NO_LICENSE_FILE) {
					// 不存在与应用对应的license文件
					Log.e(TAG, "无对应的license文件");
				} else {
					// license文件无效
					Log.e(TAG, "license文件无效");
				}
			}
		}
		MPreferenceManager.getInstance().setLocalLisenseState(lisenseState);
		return lisenseState;
	}

	public static String getLisenseInfo(int lisenseState) {
		Resources resource = FlashApp.getContext().getResources();
		String lisenseInfo;
		switch (lisenseState) {
		case STATE_TRYING:
			lisenseInfo = resource.getString(R.string.lisense_state_trying);
			break;
		case STATE_EXPIRED:
			lisenseInfo = resource.getString(R.string.lisense_state_expired);
			break;
		case STATE_UNKNOWN:
			lisenseInfo = resource.getString(R.string.lisense_state_unknown);
			break;
		default:
			lisenseInfo = null;
			break;
		}
		return lisenseInfo;
	}

	public interface OnLisenseStateChangeListener {
		public void onRemoteServiceConnected();

		public void onRemoteServiceDisconnected();
	}
}
