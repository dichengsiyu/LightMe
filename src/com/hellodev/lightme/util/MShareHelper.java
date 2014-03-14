package com.hellodev.lightme.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.widget.Toast;

import com.hellodev.lightme.R;

import java.util.ArrayList;
import java.util.List;

public class MShareHelper {
	private Context context;
	private Resources resource;
	
	public MShareHelper(Context context) {
		this.context = context;
		resource = context.getResources();
	}
	
	public void shareFeedbackWithMail() {
		Intent intent = getBaseShareIntent();
		
		List<ResolveInfo> resInfos = context.getPackageManager().queryIntentActivities(intent, 0);
		List<Intent> targetIntents = null;
		Intent targetIntent = null;
		if(resInfos != null && !resInfos.isEmpty()) {
			targetIntents = new ArrayList<Intent>();
			for(ResolveInfo resInfo : resInfos) {
				ActivityInfo activityInfo = resInfo.activityInfo;
				if(activityInfo.packageName.contains("mail")) {
					targetIntent = getMailIntent();
					targetIntent.setPackage(activityInfo.packageName);
					targetIntents.add(targetIntent);
				}
			}
			
			Intent chooserIntent = Intent.createChooser(targetIntents.remove(0), "选择分享应用");
			if(chooserIntent == null)
				return;
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, 
					targetIntents.toArray(new Intent[]{}));
			
			try {
				context.startActivity(chooserIntent);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(context, "Can't find share component to share",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public Intent getMailIntent() {
		Intent emailIntent = getBaseShareIntent();
		String[] emailReciver = new String[]{"hellodever@gmail.com"};
		String subject = resource.getString(R.string.feedback_subject);
		String defaultContent = resource.getString(R.string.feedback_content_default);
		
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
}
