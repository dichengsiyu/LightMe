package com.meizu.smartbar;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomTabOnTopActivity extends FragmentActivity implements OnPageChangeListener {

    private int mActionBarOptions;
    private ViewPager mViewPager;
    
    private View mCustomView;
    private ImageView mScroll1;
    private ImageView mScroll2;
    private ImageView mScroll3;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_tab_on_top_content);
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(this);
        // 设置页面分隔线
        mViewPager.setPageMargin(8);
        mViewPager.setPageMarginDrawable(android.R.drawable.divider_horizontal_bright);
        
        // 自定义 Tab View，并添加到ActionBar上(未设置DisplayOptions，不会显示）
        mCustomView = LayoutInflater.from(this).inflate(R.layout.custom_tab_view, null);
        getParent().getActionBar().setCustomView(mCustomView);
        
        mScroll1 = (ImageView) mCustomView.findViewById(R.id.scroll_1);
        mScroll2 = (ImageView) mCustomView.findViewById(R.id.scroll_2);
        mScroll3 = (ImageView) mCustomView.findViewById(R.id.scroll_3);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        ActionBar bar = getParent().getActionBar();
        mActionBarOptions = bar.getDisplayOptions();
        // 设置DisplayOptions，显示ActionBar自定义的View
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | mActionBarOptions);
        
        setCurrentScroll(mViewPager.getCurrentItem());
    }

    @Override
    protected void onPause() {
        super.onPause();
        getParent().getActionBar().setDisplayOptions(mActionBarOptions,
                ActionBar.DISPLAY_SHOW_CUSTOM | mActionBarOptions);
    }
    
    public void onTabClick(View view) {
        switch (view.getId()) {
        case R.id.tab_text_1:
            mViewPager.setCurrentItem(0, false);
            break;
            
        case R.id.tab_text_2:
            mViewPager.setCurrentItem(1, false);
            break;
            
        case R.id.tab_text_3:
            mViewPager.setCurrentItem(2, false);
            break;
        }
    }
    
    private void setCurrentScroll(int selection) {
        if (mScroll1 != null && mScroll2 != null && mScroll3 != null) {
            mScroll1.setVisibility(selection == 0 ? View.VISIBLE : View.INVISIBLE);
            mScroll2.setVisibility(selection == 1 ? View.VISIBLE : View.INVISIBLE);
            mScroll3.setVisibility(selection == 2 ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPageSelected(int arg0) {
        setCurrentScroll(arg0);
    }


    private class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return CountingFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
    
    
    public static class CountingFragment extends Fragment {
        int mNum;

        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static CountingFragment newInstance(int num) {
            CountingFragment f = new CountingFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 1;
        }

        /**
         * The Fragment's UI is just a simple text view showing its
         * instance number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.text_content, container, false);
            TextView text = (TextView) v.findViewById(android.R.id.text1);
            text.setText("Fragment #" + mNum);
            return v;
        }
    }

}
