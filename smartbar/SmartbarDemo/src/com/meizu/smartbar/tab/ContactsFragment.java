package com.meizu.smartbar.tab;

import com.meizu.smartbar.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class ContactsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.text_content_contacts, container, false);
        EditText text = (EditText) fragmentView.findViewById(android.R.id.text1);
        text.setText("Contacts1");
        
        return fragmentView;
    }

}
