package com.meizu.smartbar.tab;

import com.meizu.smartbar.R;
import com.meizu.smartbar.SmartBarUtils;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

/**
 * 使用 ActionBar Tab 与 Fragment 实现 tab 界面。
 */
public class ActionBarTab extends Activity {
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final ActionBar bar = getActionBar();
		bar.addTab(bar.newTab().setIcon(R.drawable.ic_tab_recent)
				.setTabListener(new MyTabListener<RecentFragment>(this, "recent", RecentFragment.class)));
		bar.addTab(bar.newTab().setIcon(R.drawable.ic_tab_contacts)
				.setTabListener(new MyTabListener<ContactsFragment>(this, "contacts", ContactsFragment.class)));
		bar.addTab(bar.newTab().setIcon(R.drawable.ic_tab_dialer)
				.setTabListener(new MyTabListener<DialerFragment>(this, "dialer", DialerFragment.class)));

		// 如果是用户自定义的View，可以像下面这样操作
//		bar.addTab(bar.newTab().setCustomView(R.layout.tab_widget_indicator)
//		        .setTabListener(new MyTabListener<ContactsFragment>(this, "contacts", ContactsFragment.class)));
		 
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// 设置ActionBar Tab显示在底栏
		SmartBarUtils.setActionBarTabsShowAtBottom(bar, true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_settings, menu);
		return true;
	}

	public static class MyTabListener<T extends Fragment> implements ActionBar.TabListener {
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private Fragment mFragment;

        public MyTabListener(Activity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        public MyTabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
            
            mActivity.getActionBar().setTitle(mTag);
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            Toast.makeText(mActivity, "Reselected!", Toast.LENGTH_SHORT).show();
        }
    }
	
}
