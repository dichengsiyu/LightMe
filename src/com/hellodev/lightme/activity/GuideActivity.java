package com.hellodev.lightme.activity;

import com.hellodev.lightme.FlashController;
import com.hellodev.lightme.R;
import com.hellodev.lightme.util.CommonDataHelper;
import com.hellodev.lightme.util.MDisplayHelper;
import com.hellodev.lightme.util.MPreferenceManager;
import com.hellodev.lightme.util.SmartBarUtils;
import com.hellodev.lightme.view.GuidePagerAdapter;
import com.hellodev.lightme.view.GuidePagerAdapter.OnGuideViewClickListener;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;

public class GuideActivity extends Activity implements OnPageChangeListener,
		OnGuideViewClickListener {
	private final static String TAG = "GuideActivity";

	private LinearLayout indexContainer;
	private ImageView[] indexImages;
	
	private ViewPager vpGuide;
	private GuidePagerAdapter pagerAdapter;
	private int[] IMG_RES = new int[] { R.drawable.guide_page_1,
			R.drawable.guide_page_2, R.drawable.guide_page_3 };//FIXME 异步加载为Drawable会快一些么
	private final static int INDEX_IMG_SELECTED = R.drawable.guide_index_selected;
	private final static int INDEX_IMG_NORMAL = R.drawable.guide_index_normal;

	private MPreferenceManager prefsMgr;
	private MDisplayHelper displayer;
	
	public final static String KEY_FLAG_FROM = "from_activity";
	private boolean fromSetting = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fromSetting = SettingActivity.class.getName().equals(
				getIntent().getStringExtra(KEY_FLAG_FROM));
		prefsMgr = MPreferenceManager.getInstance();
		int apkVersion = CommonDataHelper.getCurrentAppVersionCode(this);
		int localVersion = prefsMgr.getVersionCode();
		if (fromSetting) {
			setContentView(R.layout.activity_guide);
			
			initView();
			initSmartBar();
		} else if(apkVersion > localVersion) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.activity_guide);
			
			initView();
			initSmartBar();
			initControlData();
		} else {
			initControlData();
			startMainActivity();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		prefsMgr = null;
		if(pagerAdapter != null)
			pagerAdapter.release();
		if(vpGuide != null)
			vpGuide.removeAllViews();
	}

	//FIXME 宽高都应该写到xml中，然后可以直接getResource获取得到像素
	private void initView() {
		displayer = new MDisplayHelper();
		RelativeLayout root = (RelativeLayout) findViewById(R.id.root);

		int indexMarginBottom = fromSetting ? displayer.dpiToPx(60) : displayer
				.dpiToPx(16);
		indexContainer = new LinearLayout(this);
		indexContainer.setGravity(Gravity.CENTER);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.setMargins(0, 0, 0, indexMarginBottom);
		indexContainer.setLayoutParams(params);
		root.addView(indexContainer);

		indexImages = new ImageView[IMG_RES.length + 1];
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		param.leftMargin = displayer.dpiToPx(2);
		param.rightMargin = displayer.dpiToPx(2);
		param.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
		for (int i = 0; i <= IMG_RES.length; ++i) {
			indexImages[i] = new ImageView(this);
			indexContainer.addView(indexImages[i], param);
		}
		setSelectedIndexImage(0);

		vpGuide = (ViewPager) findViewById(R.id.vp_guide);
		vpGuide.setOnPageChangeListener(this);
		pagerAdapter = new GuidePagerAdapter(IMG_RES, fromSetting, this);
		vpGuide.setAdapter(pagerAdapter);
	}

	private void initSmartBar() {
		if (fromSetting) {
			SmartBarUtils.toogleActionBar(vpGuide, true);
			
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayOptions(0);
			SmartBarUtils.setActionBarViewCollapsable(actionBar, true);
			// 替换back键图标
			SmartBarUtils.setBackIcon(actionBar,
					getResources().getDrawable(R.drawable.ic_back));
			// 修改actionBar的背景
		} else {
			SmartBarUtils.toogleActionBar(vpGuide, false);
		}
	}

	private void setSelectedIndexImage(int position) {
		for (int i = 0; i < indexImages.length; ++i) {
			indexImages[i]
					.setBackgroundResource(i == position ? INDEX_IMG_SELECTED
							: INDEX_IMG_NORMAL);
		}
	}

	private void initControlData() {
		FlashController.getInstance().initCameraSync();
		if (prefsMgr.isSwitchSoundOn())
			FlashController.getInstance().initSwitchSound();
	}

	private void startMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	// arg0 ==1的时候表示正在滑动，arg0==2的时候表示滑动完毕了，arg0==0的时候表示什么都没做
	// 三种状态的变化顺序为（1，2，0）
	@Override
	public void onPageScrollStateChanged(int state) {
	}

	// arg0 :当前页面，当前点击滑动的页面
	// arg1:当前页面偏移的百分比
	// arg2:当前页面偏移的像素位置
	@Override
	public void onPageScrolled(int pos, float offsetRate, int offset) {

	}

	@Override
	public void onPageSelected(int currentPage) {
		setSelectedIndexImage(currentPage);
	}

	@Override
	public void onGuideViewClick(View view) {
		if (view.getId() == R.id.btn_lightme) {
			startMainActivity();
			prefsMgr.setVersionCode(CommonDataHelper
					.getCurrentAppVersionCode(this));
		}
	}
}
