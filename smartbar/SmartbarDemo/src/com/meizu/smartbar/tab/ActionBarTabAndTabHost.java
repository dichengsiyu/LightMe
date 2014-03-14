package com.meizu.smartbar.tab;

import com.meizu.smartbar.R;
import com.meizu.smartbar.SmartBarUtils;

import java.sql.BatchUpdateException;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TabHost;

/**
 * 使用 ActionBar Tab 与 TabHost 实现 SmartBar tab 界面。
 */
public class ActionBarTabAndTabHost extends TabActivity implements ActionBar.TabListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean findMethod = findActionBarTabsShowAtBottom();
		if (!findMethod) {
		    // 取消ActionBar拆分，换用TabHost
		    getWindow().setUiOptions(0);
		}
		
		setContentView(R.layout.tab_content);
		
		final TabHost tabHost = getTabHost();
		tabHost.addTab(tabHost.newTabSpec("recent")
		        .setIndicator(null, getResources().getDrawable(R.drawable.ic_tab_recent))
				.setContent(new Intent(this, RecentActivity.class)));

		tabHost.addTab(tabHost.newTabSpec("contacts")
		        .setIndicator(null, getResources().getDrawable(R.drawable.ic_tab_contacts))
				.setContent(new Intent(this, ContactsActivity.class)));

		tabHost.addTab(tabHost.newTabSpec("dialer")
		        .setIndicator(null, getResources().getDrawable(R.drawable.ic_tab_dialer))
				.setContent(new Intent(this, DialerActivity.class)));
		
		
		if (findMethod) {
		    getTabWidget().setVisibility(View.GONE);
		    
	        final ActionBar bar = getActionBar();
	        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	        
	        bar.addTab(bar.newTab().setIcon(R.drawable.ic_tab_recent).setTabListener(this));
	        bar.addTab(bar.newTab().setIcon(R.drawable.ic_tab_contacts).setTabListener(this));
	        bar.addTab(bar.newTab().setIcon(R.drawable.ic_tab_dialer).setTabListener(this));
	        
//	         如果是用户自定义的View，可以像下面这样操作
//	        bar.addTab(bar.newTab().setTabListener(this).setCustomView(R.layout.tab_widget_indicator).setTabListener(this));
	        
	        // 设置ActionBar Tab显示在底栏
	        SmartBarUtils.setActionBarTabsShowAtBottom(bar, true);
		}
	}
	
	// 查找设置ActionBar Tab显示在底栏的方法，找不到method则返回false。
	private boolean findActionBarTabsShowAtBottom() {
        try {
            Class.forName("android.app.ActionBar")
                    .getMethod("setTabsShowAtBottom", new Class[] { boolean.class });
        } catch (Exception e) {
            return false;
        }
        
        return true;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_settings, menu);
		return true;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (tab != null) {
			getTabHost().setCurrentTab(tab.getPosition());
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}
}
