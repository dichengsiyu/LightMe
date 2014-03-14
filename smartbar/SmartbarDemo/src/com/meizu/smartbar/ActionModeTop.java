package com.meizu.smartbar;

import android.app.Activity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

/**
 * 调用 ActionBar.setActionModeHeaderHidden(boolean) 隐藏 Action Mode 顶栏的示例。
 */
public class ActionModeTop extends Activity {
    
    private static final int SORT_BY_SIZE_ID = Menu.FIRST;
    private static final int SORT_BY_ALPHA_ID = Menu.FIRST + 1;
    
    private ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        CheckBox cb = new CheckBox(this);
        cb.setText("Action Mode");
        setContentView(cb);
        
        // 设置ActionMode顶栏不显示
        SmartBarUtils.setActionModeHeaderHidden(getActionBar(), true);
        
        cb.setOnCheckedChangeListener(
                new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startActionMode();
                } else {
                    stopActionMode();
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
    
    public void startActionMode() {
        if (mActionMode == null) {
            ActionMode.Callback cb = new ModeCallback();
            mActionMode = startActionMode(cb);
        }
    }

    public void stopActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }
    
    private class ModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("My Action Mode!");
            mode.setSubtitle(null);
            
            menu.add(0, SORT_BY_SIZE_ID, 0, "Sort By Size")
                .setIcon(android.R.drawable.ic_menu_sort_by_size);
            
            menu.add(0, SORT_BY_ALPHA_ID, 0, "Sort By Alpha")
                .setIcon(android.R.drawable.ic_menu_sort_alphabetically);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Toast.makeText(ActionModeTop.this, "Clicked " + item.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub
            return false;
        }
    }

}
