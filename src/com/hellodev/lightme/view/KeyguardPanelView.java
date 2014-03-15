package com.hellodev.lightme.view;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;

public class KeyguardPanelView extends LinearLayout {
	private View contentView;
	
	public KeyguardPanelView(Context context) {
		super(context);
		
		contentView = new View(context);
		addView(contentView);
	}
	
	@Override
	public void setBackgroundResource(int resid) {
		contentView.setBackgroundResource(resid);
	}
	
	@Override
	public void startAnimation(Animation animation) {
		contentView.startAnimation(animation);
	}
}
