package com.hellodev.lightme.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.hellodev.lightme.FlashApp;
import com.hellodev.lightme.R;

public class GuidePagerAdapter extends PagerAdapter implements OnClickListener {
	private int[] IMG_RES;
	private int pageCount;
	private Context appContext;
	private boolean fromSettingFlag;
	private OnGuideViewClickListener listener;

	public GuidePagerAdapter(int[] imgRes, boolean fromSetting,
			OnGuideViewClickListener listener) {
		appContext = FlashApp.getContext();
		IMG_RES = imgRes;
		pageCount = IMG_RES.length + 1;
		fromSettingFlag = fromSetting;
		this.listener = listener;
	}

	public void release() {
		this.listener = null;
	}

	@Override
	public int getCount() {
		return pageCount;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View pager = generatePagerView(position);
		container.addView(pager);
		return pager;
	}

	private View generatePagerView(int position) {
		View view = null;
		if (position < pageCount - 1) {
			ImageView imgView = new ImageView(appContext);
			imgView.setBackgroundResource(IMG_RES[position]);
			view = imgView;
		} else {
			View lastView = LayoutInflater.from(appContext).inflate(
					R.layout.pager_view, null);
			Button btnStart = (Button) lastView
					.findViewById(R.id.btn_start);

			if (fromSettingFlag) {
				btnStart.setVisibility(View.GONE);
			} else {
				btnStart.setOnClickListener(this);
			}
			view = lastView;
		}

		return view;
	}

	@Override
	public void onClick(View v) {
		if (listener != null)
			listener.onGuideViewClick(v);
	}

	public interface OnGuideViewClickListener {
		public void onGuideViewClick(View view);
	}
};
