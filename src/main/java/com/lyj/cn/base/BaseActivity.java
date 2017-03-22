package com.lyj.cn.base;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ViewUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.lyj.cn.assist.TLog;
import com.lyj.cn.network.task.ITaskManager;
import com.lyj.cn.network.task.TaskManager;
import com.lyj.cn.network.task.WorkTask;
import com.lyj.cn.setting.SettingUtility;
import com.lyj.cn.surport.BitmapOwner;
import com.lyj.cn.surport.ViewInject;
import com.lyj.cn.util.InjectUtility;
import com.lyj.cn.widget.AsToolbar;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static android.R.attr.id;

/**
 * Created by lyj on 2017/3/22 0022.
 */

public class BaseActivity extends AppCompatActivity implements BitmapOwner, ITaskManager,AsToolbar.OnToolbarDoubleClick {
    static final String TAG = "Activity-Base";
    private static Class<? extends BaseActivityHelper> mHelperClass;
    private BaseActivityHelper mHelper;
    private int theme = 0;
    private Locale language = null;
    private TaskManager taskManager;
    private boolean isDestory;
    private Map<String, WeakReference<BaseFragment>> fragmentRefs;
    private static BaseActivity runningActivity;
    private View rootView;
    @ViewInject(
            idStr = "toolbar"
    )
    Toolbar mToolbar;

    public BaseActivity() {
    }

    public static BaseActivity getRunningActivity() {
        return runningActivity;
    }

    public static void setRunningActivity(BaseActivity activity) {
        runningActivity = activity;
    }

    public static void setHelper(Class<? extends BaseActivityHelper> clazz) {
        mHelperClass = clazz;
    }

    protected int configTheme() {
        if(this.mHelper != null) {
            int theme = this.mHelper.configTheme();
            if(theme > 0) {
                return theme;
            }
        }

        return -1;
    }

    protected void onCreate(Bundle savedInstanceState) {
        if(this.mHelper == null) {
            try {
                if(mHelperClass != null) {
                    this.mHelper = (BaseActivityHelper)mHelperClass.newInstance();
                    this.mHelper.bindActivity(this);
                }
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }

        if(this.mHelper != null) {
            this.mHelper.onCreate(savedInstanceState);
        }

        this.fragmentRefs = new HashMap();
        if(savedInstanceState == null) {
            this.theme = this.configTheme();
            this.language = new Locale(SettingUtility.getPermanentSettingAsStr("language", Locale.getDefault().getLanguage()), SettingUtility.getPermanentSettingAsStr("language-country", Locale.getDefault().getCountry()));
        } else {
            this.theme = savedInstanceState.getInt("theme");
            this.language = new Locale(savedInstanceState.getString("language"), savedInstanceState.getString("language-country"));
        }

        if(this.theme > 0) {
            this.setTheme(this.theme);
        }

        this.setLanguage(this.language);
        this.taskManager = new TaskManager();

        try {
            ViewConfiguration viewConfiguration = ViewConfiguration.get(this);
            if(viewConfiguration.hasPermanentMenuKey()) {
                Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(viewConfiguration, false);
            }
        } catch (Throwable var4) {
            ;
        }

        super.onCreate(savedInstanceState);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(this.mHelper != null) {
            this.mHelper.onPostCreate(savedInstanceState);
        }

    }

    protected void onStart() {
        super.onStart();
        if(this.mHelper != null) {
            this.mHelper.onStart();
        }

    }

    protected void onRestart() {
        super.onRestart();
        if(this.mHelper != null) {
            this.mHelper.onRestart();
        }

    }

    public Toolbar getToolbar() {
        return this.mToolbar;
    }

    public View getRootView() {
        return this.rootView;
    }

    public void setContentView(int layoutResID) {
        this.setContentView(View.inflate(this, layoutResID, (ViewGroup)null));
    }

    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        this.rootView = view;
        InjectUtility.initInjectedView(this, this, this.rootView);
        this.mToolbar = (Toolbar)this.findViewById(id.toolbar);
        if(this.mToolbar != null) {
            this.setSupportActionBar(this.mToolbar);
        }

    }

    public void setContentView(View view) {
        super.setContentView(view, new ViewGroup.LayoutParams(-1, -1));
        this.rootView = view;
        InjectUtility.initInjectedView(this, this, this.rootView);
        this.mToolbar = (Toolbar)this.findViewById(id.toolbar);
        if(this.mToolbar != null) {
            this.setSupportActionBar(this.mToolbar);
        }

    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(this.mHelper != null) {
            this.mHelper.onSaveInstanceState(outState);
        }

        outState.putInt("theme", this.theme);
        outState.putString("language", this.language.getLanguage());
        outState.putString("language-country", this.language.getCountry());
    }

    public void addFragment(String tag, BaseFragment fragment) {
        this.fragmentRefs.put(tag, new WeakReference(fragment));
    }

    public void removeFragment(String tag) {
        this.fragmentRefs.remove(tag);
    }

    protected void onResume() {
        super.onResume();
        if(this.mHelper != null) {
            this.mHelper.onResume();
        }

        setRunningActivity(this);
        if(this.theme == this.configTheme()) {
            String languageStr = SettingUtility.getPermanentSettingAsStr("language", Locale.getDefault().getLanguage());
            String country = SettingUtility.getPermanentSettingAsStr("language-country", Locale.getDefault().getCountry());
            if(this.language == null || !this.language.getLanguage().equals(languageStr) || !country.equals(this.language.getCountry())) {
                TLog.i("language changed, reload()");
                this.reload();
            }
        } else {
            TLog.i("theme changed, reload()");
            this.reload();
        }
    }

    protected void onPause() {
        super.onPause();
        if(this.mHelper != null) {
            this.mHelper.onPause();
        }

    }

    public void setLanguage(Locale locale) {
        Resources resources = this.getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(config, dm);
    }

    protected void onStop() {
        super.onStop();
        if(this.mHelper != null) {
            this.mHelper.onStop();
        }

    }

    public void reload() {
        Intent intent = this.getIntent();
        this.overridePendingTransition(0, 0);
        intent.addFlags(65536);
        this.finish();
        this.overridePendingTransition(0, 0);
        this.startActivity(intent);
    }

    public void onDestroy() {
        this.isDestory = true;
        this.removeAllTask(true);

        super.onDestroy();
        if(this.mHelper != null) {
            this.mHelper.onDestroy();
        }

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(this.mHelper != null) {
            boolean handle = this.mHelper.onOptionsItemSelected(item);
            if(handle) {
                return true;
            }
        }

        switch(item.getItemId()) {
            case 16908332:
                if(this.onHomeClick()) {
                    return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected boolean onHomeClick() {
        if(this.mHelper != null) {
            boolean keys = this.mHelper.onHomeClick();
            if(keys) {
                return true;
            }
        }

        Set keys1 = this.fragmentRefs.keySet();
        Iterator var2 = keys1.iterator();

        BaseFragment fragment;
        do {
            if(!var2.hasNext()) {
                return this.onBackClick();
            }

            String key = (String)var2.next();
            WeakReference fragmentRef = (WeakReference)this.fragmentRefs.get(key);
            fragment = (BaseFragment)fragmentRef.get();
        } while(fragment == null || !fragment.onHomeClick());

        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(this.mHelper != null) {
            boolean handle = this.mHelper.onKeyDown(keyCode, event);
            if(handle) {
                return true;
            }
        }

        return keyCode == 4 && event.getRepeatCount() == 0 && this.onBackClick()?true:super.onKeyDown(keyCode, event);
    }

    public boolean onBackClick() {
        if(this.mHelper != null) {
            boolean keys = this.mHelper.onBackClick();
            if(keys) {
                return true;
            }
        }

        Set keys1 = this.fragmentRefs.keySet();
        Iterator var2 = keys1.iterator();

        BaseFragment fragment;
        do {
            if(!var2.hasNext()) {
                this.finish();
                return true;
            }

            String key = (String)var2.next();
            WeakReference fragmentRef = (WeakReference)this.fragmentRefs.get(key);
            fragment = (BaseFragment)fragmentRef.get();
        } while(fragment == null || !fragment.onBackClick());

        return true;
    }

    public final void addTask(WorkTask task) {
        this.taskManager.addTask(task);
    }

    public final void removeTask(String taskId, boolean cancelIfRunning) {
        this.taskManager.removeTask(taskId, cancelIfRunning);
    }

    public final void removeAllTask(boolean cancelIfRunning) {
        this.taskManager.removeAllTask(cancelIfRunning);
    }

    public final int getTaskCount(String taskId) {
        return this.taskManager.getTaskCount(taskId);
    }



    public void finish() {
        this.setDestory(true);
        super.finish();
        if(this.mHelper != null) {
            this.mHelper.finish();
        }

    }

    public boolean isDestory() {
        return this.isDestory;
    }

    public void setDestory(boolean destory) {
        this.isDestory = destory;
    }

    public boolean canDisplay() {
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(this.mHelper != null) {
            this.mHelper.onActivityResult(requestCode, resultCode, data);
        }

    }

    public boolean onToolbarDoubleClick() {
        Set keys = this.fragmentRefs.keySet();
        Iterator var2 = keys.iterator();
        BaseFragment fragment;
        do {
            if(!var2.hasNext()) {
                return false;
            }

            String key = (String)var2.next();
            WeakReference fragmentRef = (WeakReference)this.fragmentRefs.get(key);
            fragment = (BaseFragment)fragmentRef.get();
        } while(fragment == null || !(fragment instanceof AsToolbar.OnToolbarDoubleClick) || !((AsToolbar.OnToolbarDoubleClick)fragment).onToolbarDoubleClick());

        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(this.mHelper != null) {
            this.mHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    public BaseActivityHelper getActivityHelper() {
        return this.mHelper;
    }

}
