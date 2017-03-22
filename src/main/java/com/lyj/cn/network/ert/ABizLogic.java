package com.lyj.cn.network.ert;

import android.text.TextUtils;

import com.lyj.cn.assist.TLog;
import com.lyj.cn.network.task.TaskException;
import com.lyj.cn.network.task.WorkTask;
import com.lyj.cn.setting.Setting;
import com.lyj.cn.setting.SettingExtra;
import com.lyj.cn.setting.SettingUtility;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2017/3/22 0022.
 */

public abstract class ABizLogic  implements IHttpUtility {
    public static final String TAG = "BizLogic";
    public static final String BASE_URL = "base_url";
    public static final String CACHE_UTILITY = "cache_utility";
    public static final String HTTP_UTILITY = "http";
    private ABizLogic.CacheMode mCacheMode;
    static final int CORE_POOL_SIZE = 10;
    static final int MAXIMUM_POOL_SIZE = 128;
    static final int KEEP_ALIVE = 1;
    static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "BizlogicCacheTask #" + this.mCount.getAndIncrement());
        }
    };
    static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue(10);
    public static final Executor CACHE_THREAD_POOL_EXECUTOR;

    public static String getTag(Setting action, String append) {
        return "BizLogic-" + action.getType() + "-" + append;
    }

    public ABizLogic() {
        this.mCacheMode = ABizLogic.CacheMode.disable;
    }

    public ABizLogic(ABizLogic.CacheMode cacheMode) {
        this.mCacheMode = cacheMode;
    }

    public <T> T doGet(HttpConfig config, Setting action, Params params, Class<T> responseCls) throws TaskException {
        ICacheUtility cacheUtility = null;
        IResult cache = null;
        if(action.getExtras().containsKey("cache_utility")) {
            if(!TextUtils.isEmpty(((SettingExtra)action.getExtras().get("cache_utility")).getValue())) {
                try {
                    cacheUtility = (ICacheUtility)Class.forName(((SettingExtra)action.getExtras().get("cache_utility")).getValue()).newInstance();
                } catch (Exception var10) {
                    TLog.w(getTag(action, "Get"), "CacheUtility 配置错误");
                }
            }
        } else {
            TLog.v(getTag(action, "Get"), "CacheUtility 没有配置");
        }

        long e;
        if(this.mCacheMode != ABizLogic.CacheMode.disable && cacheUtility != null) {
            e = System.currentTimeMillis();
            cache = cacheUtility.findCacheData(action, params);
            if(cache != null) {
                TLog.d(getTag(action, "Cache"), "读取缓存耗时 %s ms", new Object[]{String.valueOf(System.currentTimeMillis() - e)});
            }
        }

        if(cache != null && this.mCacheMode != ABizLogic.CacheMode.servicePriority) {
            if(cache != null) {
                TLog.d(getTag(action, "Cache"), "返回缓存数据");
                return cache;
            } else {
                throw null;
            }
        } else {
            try {
                e = System.currentTimeMillis();
                Object result = this.getHttpUtility(action).doGet(this.resetHttpConfig(config, action), action, params, responseCls);
                TLog.d(getTag(action, "Get-Http"), "耗时 %s ms", new Object[]{String.valueOf(System.currentTimeMillis() - e)});
                if(result != null && result instanceof IResult) {
                    this.putToCache(action, params, (IResult)result, cacheUtility);
                }

                TLog.d(getTag(action, "Get-Http"), "返回服务器数据");
                return result;
            } catch (TaskException var11) {
                TLog.w(getTag(action, "Exception"), var11 + "");
                throw var11;
            } catch (Exception var12) {
                TLog.w(getTag(action, "Exception"), var12 + "");
                throw new TaskException(TextUtils.isEmpty(var12.getMessage())?"服务器错误":var12.getMessage());
            }
        }
    }

    public <T> T doGet(Setting actionSetting, Params params, Class<T> responseCls) throws TaskException {
        return this.doGet(this.configHttpConfig(), actionSetting, params, responseCls);
    }

    public <T> T doPost(HttpConfig config, Setting action, Params urlParams, Params bodyParams, Object requestObj, Class<T> responseCls) throws TaskException {
        long time = System.currentTimeMillis();
        Object result = this.getHttpUtility(action).doPost(this.resetHttpConfig(config, action), action, urlParams, bodyParams, requestObj, responseCls);
        TLog.d(getTag(action, "Post"), "耗时 %s ms", new Object[]{String.valueOf(System.currentTimeMillis() - time)});
        return result;
    }

    public <T> T doPostFiles(HttpConfig config, Setting action, Params urlParams, Params bodyParams, MultipartFile[] files, Class<T> responseCls) throws TaskException {
        long time = System.currentTimeMillis();
        Object result = this.getHttpUtility(action).doPostFiles(this.resetHttpConfig(config, action), action, urlParams, bodyParams, files, responseCls);
        TLog.d(getTag(action, "doPostFiles"), "耗时 %s ms", new Object[]{String.valueOf(System.currentTimeMillis() - time)});
        return result;
    }

    private IHttpUtility getHttpUtility(Setting action) {
        if(action.getExtras().get("http") != null && !TextUtils.isEmpty(((SettingExtra)action.getExtras().get("http")).getValue())) {
            try {
                IHttpUtility e = (IHttpUtility)Class.forName(((SettingExtra)action.getExtras().get("http")).getValue()).newInstance();
                return e;
            } catch (Exception var3) {
                var3.printStackTrace();
                TLog.w("BizLogic", "CacheUtility 没有配置或者配置错误");
            }
        }

        return this.configHttpUtility();
    }

    protected abstract HttpConfig configHttpConfig();

    protected IHttpUtility configHttpUtility() {
        try {
            if(!TextUtils.isEmpty(SettingUtility.getStringSetting("http"))) {
                return (IHttpUtility)Class.forName(SettingUtility.getStringSetting("http")).newInstance();
            }
        } catch (Exception var2) {
            TLog.printExc(ABizLogic.class, var2);
        }

        return new DefHttpUtility();
    }

    private HttpConfig resetHttpConfig(HttpConfig config, Setting actionSetting) {
        try {
            if(actionSetting != null && actionSetting.getExtras().containsKey("base_url")) {
                config.baseUrl = ((SettingExtra)actionSetting.getExtras().get("base_url")).getValue().toString();
            }
        } catch (Exception var4) {
            ;
        }

        return config;
    }

    protected HttpConfig getHttpConfig() {
        return this.configHttpConfig();
    }

    public void putToCache(Setting setting, Params params, IResult data, ICacheUtility cacheUtility) {
        if(data != null && cacheUtility != null && !data.fromCache()) {
            (new ABizLogic.PutCacheTask(setting, params, data, cacheUtility)).executeOnExecutor(CACHE_THREAD_POOL_EXECUTOR, new Void[0]);
        }

    }

    public static Setting getSetting(String type) {
        return SettingUtility.getSetting(type);
    }

    protected Setting newSetting(String type, String value, String desc) {
        Setting extra = new Setting();
        extra.setType(type);
        extra.setValue(value);
        extra.setDescription(desc);
        return extra;
    }

    protected SettingExtra newSettingExtra(String type, String value, String desc) {
        SettingExtra extra = new SettingExtra();
        extra.setType(type);
        extra.setValue(value);
        extra.setDescription(desc);
        return extra;
    }

    static {
        CACHE_THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(10, 128, 1L, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
    }

    class PutCacheTask extends WorkTask<Void, Void, Void> {
        private Setting setting;
        private Params params;
        private IResult o;
        private ICacheUtility cacheUtility;

        PutCacheTask(Setting setting, Params params, IResult o, ICacheUtility cacheUtility) {
            this.setting = setting;
            this.params = params;
            this.o = o;
            this.cacheUtility = cacheUtility;
        }

        public Void workInBackground(Void... p) throws TaskException {
            long time = System.currentTimeMillis();
            TLog.d(ABizLogic.getTag(this.setting, "Cache"), "开始保存缓存");
            this.cacheUtility.addCacheData(this.setting, this.params, this.o);
            TLog.d(ABizLogic.getTag(this.setting, "Cache"), "保存缓存耗时 %s ms", new Object[]{String.valueOf(System.currentTimeMillis() - time)});
            return null;
        }
    }

    public static enum CacheMode {
        auto,
        servicePriority,
        cachePriority,
        disable;

        private CacheMode() {
        }
    }
}
