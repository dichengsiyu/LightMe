package com.meizu.smartbar;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SettingFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        
        View fragmentView = inflater.inflate(R.layout.text_content, container, false);
        TextView text = (TextView) fragmentView.findViewById(android.R.id.text1);
        text.setText("SettingFragment");
        
        return fragmentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        
        super.onCreateOptionsMenu(menu, inflater);
    }

}
