package com.meizu.smartbar;

import android.app.Activity;
import android.os.Bundle;

public class SettingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getActionBar().setTitle("SettingActivity");
    }

}
