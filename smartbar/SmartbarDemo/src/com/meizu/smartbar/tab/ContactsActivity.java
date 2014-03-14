package com.meizu.smartbar.tab;

import com.meizu.smartbar.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class ContactsActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.text_content_contacts);
        EditText text = (EditText)findViewById(android.R.id.text1);
        text.setText("Contacts1");
    }
    
    @Override
    protected void onResume() {
    	getParent().getActionBar().setTitle("Contacts title");
    	super.onResume();
    }
}