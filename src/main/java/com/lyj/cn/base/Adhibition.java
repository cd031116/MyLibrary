package com.lyj.cn.base;

import android.app.Application;
import android.os.Handler;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;


/**
 * Created by lyj on 2017/3/22 0022.
 */

public class Adhibition extends Application {
    private static Adhibition _context;
    public static final int CONN_TIMEOUT = 30000;
    public static final int READ_TIMEOUT = 30000;
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();
    Handler mHandler = new Handler() {
    };

    public Adhibition(){

    }

    public void onCreate() {
        super.onCreate();
        _context = this;
    }

    public static Adhibition getInstance() {
        return _context;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public static OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public static void configOkHttpClient(int connTimeout, int socketTimeout) {
        if(mOkHttpClient != null) {
            mOkHttpClient.setConnectTimeout((long)connTimeout, TimeUnit.MILLISECONDS);
            mOkHttpClient.setReadTimeout((long)socketTimeout, TimeUnit.MILLISECONDS);
        }

    }

    static {
        configOkHttpClient(30000, 30000);
    }
}
