package com.hellodev.lightme.activity;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hellodev.lightme.FlashController;
import com.hellodev.lightme.R;
import com.hellodev.lightme.util.CommonDataHelper;
import com.hellodev.lightme.util.MConnectHelper;
import com.hellodev.lightme.util.MDisplayHelper;
import com.hellodev.lightme.util.SmartBarUtils;
import com.umeng.analytics.MobclickAgent;

public class AboutActivity extends Activity {
	private TextView tvVerison;
	private LinearLayout membersContainer;
	private View[] members = new View[3];
	
	private String names[] = {"@dichengsiyu","@川沢悪童","@沦陷Lesaw"};
	private String descriptions[] = {"一枚程序员","主题 Details 的设计师","主题 写意 的设计师"};
	private String nameUrls[] = {"http://weibo.com/dichengsiyu",
			"http://weibo.com/p/1005051730920503",
			"http://weibo.com/otakuf"};
	
	private final static String URL_XY = "ad34c12290ae4e0ba30a6e0a4f4055ad";
	private final static String URL_DETAILS = "dc5b775e846e41ac8f449b212e44bdfa";
	
	private MConnectHelper connecter;
	private MDisplayHelper displayer;
	private ColorStateList desColors;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		displayer = new MDisplayHelper();
		connecter = new MConnectHelper(this);
		
		initActionBar();
		initView();
		initMembers();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		desColors = null;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void initView() {
		tvVerison = (TextView) findViewById(R.id.tv_version);
		String version = CommonDataHelper.getCurrentAppVersionInfo(this, getResources().getString(R.string.format_version_v));
		tvVerison.setText(version);
		
		membersContainer = (LinearLayout) findViewById(R.id.members);
	}
	
	private void initActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME 
				| ActionBar.DISPLAY_SHOW_TITLE
				| ActionBar.DISPLAY_HOME_AS_UP);
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		int icon = FlashController.getInstance().islisenseEnable()? R.drawable.ic_logo: R.drawable.ic_logo_locked;
		actionBar.setIcon(icon);
		SmartBarUtils.setBackIcon(actionBar, getResources().getDrawable(R.drawable.ic_back));
	}
	
	private void initMembers() {
		XmlResourceParser nameColorParser = getResources().getXml(R.color.name_url_color_list);  
        try {  
        	desColors= ColorStateList.createFromXml(getResources(),nameColorParser);  
        }catch(XmlPullParserException e){  
            e.printStackTrace();          
        }catch(IOException e){  
            e.printStackTrace();          
        } 
        
		LayoutInflater mInflater = LayoutInflater.from(this);
		
		members[0] = mInflater.inflate(R.layout.member, null);
		generateMember(0, members[0], R.drawable.ico_sy, null, null);
		membersContainer.addView(members[0]);
		
		members[1] = mInflater.inflate(R.layout.member, null);
		generateMember(1, members[1], R.drawable.ico_cc, "Details", URL_DETAILS);
		membersContainer.addView(members[1]);
		
		members[2] = mInflater.inflate(R.layout.member, null);
		generateMember(2, members[2], R.drawable.ico_lx, "写意", URL_XY);
		membersContainer.addView(members[2]);
	}
	
	//FIXME 考虑用viewHolder的形式，最后能回收图片资源
	private View generateMember(int pos, View root, int avatar, String desKeyword, String desUrl) {
		ImageView imgAvatar = (ImageView) root.findViewById(R.id.avatar);
		imgAvatar.setImageResource(avatar);
		
		TextView tvName = (TextView) root.findViewById(R.id.name);
		String name = names[pos];
		String nameUrl = nameUrls[pos];

		tvName.setText(name);
		tvName.setTag(nameUrl);
		tvName.setOnClickListener(memberNameOnClickListener);
		
		TextView tvDescription = (TextView) root.findViewById(R.id.description);
		String des = descriptions[pos];
		if(desKeyword != null && desUrl != null) {
			int start = des.indexOf(desKeyword);
			TextAppearanceSpan desSpan = new TextAppearanceSpan("monospace",android.graphics.Typeface.BOLD, displayer.dpiToPx(16), null, desColors);
			DesURLSpan desKeywordSpan = new DesURLSpan(desUrl);
			SpannableStringBuilder desBuilder = new SpannableStringBuilder(des);
			desBuilder.setSpan(desSpan, start, start + desKeyword.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			desBuilder.setSpan(desKeywordSpan, start, start + desKeyword.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			
			tvDescription.setText(desBuilder);
			tvDescription.setTag(desKeyword);
			tvDescription.setMovementMethod(LinkMovementMethod.getInstance());
		} else {
			tvDescription.setText(des);
		}
		return root;
	}
	
	private OnClickListener memberNameOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String url = (String)v.getTag(); 
			if(url != null) {
		        Uri uri = Uri.parse(url);   
		        Intent intent = new Intent(Intent.ACTION_VIEW,uri);    
		        startActivity(intent); 
			}
		}
	};
	
	private class DesURLSpan extends ClickableSpan {
		private String url;
		public DesURLSpan(String url) {
			this.url = url;
		}
		
		@Override
		public void onClick(View widget) {
			Spannable spannable = ((Spannable)((TextView)widget).getText()); 
			Selection.removeSelection(spannable);
//			Selection.setSelection(spannable, 0);//FIXME 点击有黄色
			connecter.jumpToMarket(url);
		}
		
		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setColor(ds.linkColor);
			ds.setUnderlineText(false);
		}
	};
}