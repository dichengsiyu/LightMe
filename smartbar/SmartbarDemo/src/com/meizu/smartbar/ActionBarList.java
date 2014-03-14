package com.meizu.smartbar;

import com.meizu.smartbar.tab.ContactsFragment;
import com.meizu.smartbar.tab.DialerFragment;
import com.meizu.smartbar.tab.RecentFragment;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * ActionBar list navigation mode 以及 Action Item 普通用法的示例。
 */
public class ActionBarList extends Activity implements OnNavigationListener {
    
    private final static int LIST_INDEX_RECENT = 0;
    private final static int LIST_INDEX_CONTACTS = 1;
    private final static int LIST_INDEX_DIALER = 2;
    private final static int LIST_INDEX_SETTINGS = 3;
    
    private final Fragment mRecentFragment = new RecentFragment();
    private final Fragment mContactsFragment = new ContactsFragment();
    private final Fragment mDialerFragment = new DialerFragment();
    private final Fragment mSettingFragment = new SettingFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        final ActionBar bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME,
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        
        // 设置 List Navigation Mode
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        MyAdapter adapter = new MyAdapter(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                new String[] {"Recent", "Contacts", "Dialer", "Settings"});
        // 设置下拉列表项的布局
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // 设置list adapter以及navigation callback
        bar.setListNavigationCallbacks(adapter, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_list_menu, menu);
        
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_delete:
            new AlertDialog.Builder(this)
                .setTitle("Action Menu Item")
                .setPositiveButton("Yes", null)
                .setNegativeButton("Cancel", null)
                .show();
            break;
            
        case R.id.menu_settings:
            startActivity(new Intent(this, SettingActivity.class));
            break;
        
        default:
            break;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        // 切换界面
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (itemPosition) {
        case LIST_INDEX_RECENT:
            getActionBar().setIcon(R.drawable.ic_tab_recent);
            ft.replace(android.R.id.content, mRecentFragment);
            break;
            
        case LIST_INDEX_CONTACTS:
            getActionBar().setIcon(R.drawable.ic_tab_contacts);
            ft.replace(android.R.id.content, mContactsFragment);
            break;
            
        case LIST_INDEX_DIALER:
            getActionBar().setIcon(R.drawable.ic_tab_dialer);
            ft.replace(android.R.id.content, mDialerFragment);
            break;
            
        case LIST_INDEX_SETTINGS:
            getActionBar().setIcon(R.drawable.ic_tab_settings);
            ft.replace(android.R.id.content, mSettingFragment);
            break;
        }      
        ft.commit();
        return true;
    }
    
    
    private class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            
            ImageView icon = (ImageView) view.findViewById(android.R.id.icon1);
            switch (position) {
            case LIST_INDEX_RECENT:
                icon.setImageResource(R.drawable.ic_tab_recent);
                break;
                
            case LIST_INDEX_CONTACTS:
                icon.setImageResource(R.drawable.ic_tab_contacts);
                break;
                
            case LIST_INDEX_DIALER:
                icon.setImageResource(R.drawable.ic_tab_dialer);
                break;
                
            case LIST_INDEX_SETTINGS:
                icon.setImageResource(R.drawable.ic_tab_settings);
                break;
            }
            
            return view;
        }
    }

}
