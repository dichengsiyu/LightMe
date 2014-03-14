
package com.meizu.smartbar;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;

public class CustomBackButton extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ActionBar bar = getActionBar();
        
        //替换back键图标
        SmartBarUtils.setBackIcon(bar, getResources().getDrawable(R.drawable.ic_back));
        
    }

}
