package com.meizu.smartbar;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 调用 ActionBar.setActionBarViewCollapsable(boolean) 隐藏 ActionBar 顶栏的示例。
 */
public class ActionBarTop extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        
        CheckBox cb = new CheckBox(this);
        cb.setText("Set ActionBar Top Collapsable");
        cb.setGravity(Gravity.CENTER);
        setContentView(cb);
        
        // 设置若顶栏没有显示内空，则隐藏
        SmartBarUtils.setActionBarViewCollapsable(getActionBar(), true);
        
        cb.setOnCheckedChangeListener(
                new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    getActionBar().setDisplayOptions(0);
                } else {
                    getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
                }
            }
        });
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_top_menu, menu);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    public void onSort(MenuItem item) {
    }
    
}
