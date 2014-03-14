package com.meizu.smartbar.tab;

import com.meizu.smartbar.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DialerActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.text_content);
        TextView text = (TextView)findViewById(android.R.id.text1);
        text.setText("Dialer");
    }
    
    @Override
    protected void onResume() {
    	getParent().getActionBar().setTitle("Dialer title");
    	super.onResume();
    }
}