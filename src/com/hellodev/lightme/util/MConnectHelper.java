package com.hellodev.lightme.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.widget.Toast;

import com.hellodev.lightme.R;

import java.util.ArrayList;
import java.util.List;

public class MConnectHelper {
	public static final String MSTORE_URI_HEADER = "mstore:";
    public static final String SOFTWARE_URL_BASE = "http://app.meizu.com/phone/apps/";
    public static final String MSTORE_IDENTIFY = "45e228a5a533488c8d46f7b5284d4ec7";
    
	private Context context;
	private Resources resource;

	public MConnectHelper(Context context) {
		resource = context.getResources();
		this.context = context;
	}
	
	public void sendLockMsgWithMail(int lisenseState) {
		String subject = resource.getString(R.string.lock_subject);
		String defaultContent = String.format(resource
				.getString(R.string.lock_content_default), MLisenseMangaer.getLisenseInfo(lisenseState));
		sendInfoWithMail(subject, defaultContent);
	}
	
	public void sendFeedbackWithMail() {
		String subject = resource.getString(R.string.feedback_subject);
		String defaultContent = resource
				.getString(R.string.feedback_content_default);
		sendInfoWithMail(subject, defaultContent);
	}

	private void sendInfoWithMail(String subject, String defaultContent) {
		Intent intent = getBaseShareIntent();

		List<ResolveInfo> resInfos = context.getPackageManager()
				.queryIntentActivities(intent, 0);
		List<Intent> targetIntents = null;
		Intent targetIntent = null;
		if (resInfos != null && !resInfos.isEmpty()) {
			targetIntents = new ArrayList<Intent>();
			for (ResolveInfo resInfo : resInfos) {
				ActivityInfo activityInfo = resInfo.activityInfo;
				if (activityInfo.packageName.contains("mail")) {
					targetIntent = getMailIntent(subject, defaultContent);
					targetIntent.setPackage(activityInfo.packageName);
					targetIntents.add(targetIntent);
				}
			}

			Intent chooserIntent = Intent.createChooser(
					targetIntents.remove(0), "选择邮件应用");
			if (chooserIntent == null)
				return;
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
					targetIntents.toArray(new Intent[] {}));

			try {
				context.startActivity(chooserIntent);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(context, "Can't find share component to share",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public Intent getMailIntent(String subject, String defaultContent) {
		Intent emailIntent = getBaseShareIntent();
		String[] emailReciver = new String[] { "dichengsiyu@gmail.com" };

		emailIntent.putExtra(Intent.EXTRA_EMAIL, emailReciver);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, defaultContent);
		return emailIntent;
	}

	private Intent getBaseShareIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		return intent;
	}

	public void jumpToMarket(String appIdentify) {
		try {
			Intent intent = new Intent();
			intent.setData(Uri.parse(MSTORE_URI_HEADER
					+ SOFTWARE_URL_BASE + appIdentify));
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.setClassName("com.meizu.mstore",
					"com.meizu.mstore.MStoreMainPlusActivity");
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
