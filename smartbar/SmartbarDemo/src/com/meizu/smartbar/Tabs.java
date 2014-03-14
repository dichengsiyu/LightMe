package com.meizu.smartbar;

import com.meizu.smartbar.tab.ActionBarTab;
import com.meizu.smartbar.tab.ActionBarTabAndTabHost;
import com.meizu.smartbar.tab.ActionItemTab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Tabs extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabs_layout);
        
        getActionBar().setTitle("SmartBar Tab");
    }
    
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
        case R.id.btn_action_item_tab:
            intent.setClass(this, ActionItemTab.class);
            break;
            
        case R.id.btn_action_bar_tab:
            intent.setClass(this, ActionBarTab.class);
            break;
            
        case R.id.btn_action_bar_tab_and_tabhost:
            intent.setClass(this, ActionBarTabAndTabHost.class);
            break;

        default:
            break;
        }
        
        startActivity(intent);
    }

}
