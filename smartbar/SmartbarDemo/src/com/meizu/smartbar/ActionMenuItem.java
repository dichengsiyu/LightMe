package com.meizu.smartbar;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * 调用 Build.hasSmartBar() 判断本机有无动态SmartBar。
 */
public class ActionMenuItem extends Activity {
    
    private static final int SETTINGS_ID = Menu.FIRST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (hasSmartBar()) {
            // 如有SmartBar，则使用拆分ActionBar，使MenuItem显示在底栏
            getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
        } else {
            // 取消ActionBar拆分，使MenuItem显示在顶栏
            getWindow().setUiOptions(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, SETTINGS_ID, 0, "settings");
        item.setIcon(R.drawable.ic_setting);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }
    
    private boolean hasSmartBar() {
        try {
            // 新型号可用反射调用Build.hasSmartBar()
            Method method = Class.forName("android.os.Build").getMethod("hasSmartBar");
            return ((Boolean) method.invoke(null)).booleanValue();
        } catch (Exception e) {
        }
        
        // 反射不到Build.hasSmartBar()，则用Build.DEVICE判断
        if (Build.DEVICE.equals("mx2")) {
            return true;
        } else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
            return false;
        }
        
        return false;
    }
    
}
