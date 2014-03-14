package com.hellodev.lightme.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.ActionBar;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.view.View;

public class SmartBarUtils {

    /**
     * 调用 ActionBar.setTabsShowAtBottom(boolean) 方法。
     * 
     * <p>如果 android:uiOptions="splitActionBarWhenNarrow"，则可设置ActionBar Tabs显示在底栏。
     * 
     * <p>示例：</p>
     * <pre class="prettyprint">
     * public class MyActivity extends Activity implements ActionBar.TabListener {
     * 
     *     protected void onCreate(Bundle savedInstanceState) {
     *         super.onCreate(savedInstanceState);
     *         ...
     *         
     *         final ActionBar bar = getActionBar();
     *         bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
     *         SmartBarUtils.setActionBarTabsShowAtBottom(bar, true);
     *         
     *         bar.addTab(bar.newTab().setText("tab1").setTabListener(this));
     *         ...
     *     }
     * }
     * </pre>
     */
    public static void setActionBarTabsShowAtBottom(ActionBar actionbar, boolean showAtBottom) {
        try {
            Method method = Class.forName("android.app.ActionBar").getMethod(
                    "setTabsShowAtBottom", new Class[] { boolean.class });
            try {
                method.invoke(actionbar, showAtBottom);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 调用 ActionBar.setActionBarViewCollapsable(boolean) 方法。
     * 
     * <p>设置ActionBar顶栏无显示内容时是否隐藏。
     * 
     * <p>示例：</p>
     * <pre class="prettyprint">
     * public class MyActivity extends Activity {
     * 
     *     protected void onCreate(Bundle savedInstanceState) {
     *         super.onCreate(savedInstanceState);
     *         ...
     *         
     *         final ActionBar bar = getActionBar();
     *         
     *         // 调用setActionBarViewCollapsable，并设置ActionBar没有显示内容，则ActionBar顶栏不显示
     *         SmartBarUtils.setActionBarViewCollapsable(bar, true);
     *         bar.setDisplayOptions(0);
     *     }
     * }
     * </pre>
     */
    public static void setActionBarViewCollapsable(ActionBar actionbar, boolean collapsable) {
        try {
            Method method = Class.forName("android.app.ActionBar").getMethod(
                    "setActionBarViewCollapsable", new Class[] { boolean.class });
            try {
                method.invoke(actionbar, collapsable);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 调用 ActionBar.setActionModeHeaderHidden(boolean) 方法。
     * 
     * <p>设置ActionMode顶栏是否隐藏。
     * 
     * <p>示例：</p>
     * <pre class="prettyprint">
     * public class MyActivity extends Activity {
     * 
     *     protected void onCreate(Bundle savedInstanceState) {
     *         super.onCreate(savedInstanceState);
     *         ...
     *         
     *         final ActionBar bar = getActionBar();
     *         
     *         // ActionBar转为ActionMode时，不显示ActionMode顶栏
     *         SmartBarUtils.setActionModeHeaderHidden(bar, true);
     *     }
     * }
     * </pre>
     */
    public static void setActionModeHeaderHidden(ActionBar actionbar, boolean hidden) {
        try {
            Method method = Class.forName("android.app.ActionBar").getMethod(
                    "setActionModeHeaderHidden", new Class[] { boolean.class });
            try {
                method.invoke(actionbar, hidden);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    
    
    /**
     * 调用ActionBar.setBackButtonDrawable(Drawable)方法
     * 
     * <p>设置返回键图标
     * 
     * <p>示例：</p>
     * <pre class="prettyprint">
     * public class MyActivity extends Activity {
     * 
     *     protected void onCreate(Bundle savedInstanceState) {
     *         super.onCreate(savedInstanceState);
     *         ...
     *         
     *         final ActionBar bar = getActionBar();
     *         // 自定义ActionBar的返回键图标
     *         SmartBarUtils.setBackIcon(bar, getResources().getDrawable(R.drawable.ic_back));
     *         ...
     *     }
     * }
     * </pre>
     */
    public static void setBackIcon(ActionBar actionbar, Drawable backIcon) {
        try {
            Method method = Class.forName("android.app.ActionBar").getMethod(
                    "setBackButtonDrawable", new Class[] { Drawable.class });
            try {
                method.invoke(actionbar, backIcon); 
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public static void setHomeupIcon(View decorView, Drawable homeUpDrawable) {
    	
    }
    
    public static void toogleActionBar(View view, boolean show) {
        int flags = !show ? View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        			:View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        view.setSystemUiVisibility(flags);
	}
    
}
