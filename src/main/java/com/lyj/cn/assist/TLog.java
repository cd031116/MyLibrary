package com.lyj.cn.assist;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

/**
 * Created by lyj on 2017/3/22 0022.
 */

public class TLog {
    public static final String TAG = "Tlog";
    public static boolean DEBUG = true;

    public TLog() {
    }

    public static void v(Object o) {
        if(DEBUG) {
            String log = toJson(o);
            Log.v(TAG, log);
            TLog2File.log2File(TAG, log);
        }

    }

    public static void v(String tag, Object o) {
        if(DEBUG) {
            String log = toJson(o);
            Log.v(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void v(String tag, String msg, Throwable tr) {
        if(DEBUG) {
            String log = msg + '\n' + getStackTraceString(tr);
            Log.v(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void v(String tag, String format, Object... args) {
        if(DEBUG) {
            String log = String.format(format, args);
            Log.v(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void d(Object o) {
        if(DEBUG) {
            String log = toJson(o);
            Log.d(TAG, log);
            TLog2File.log2File(TAG, log);
        }

    }

    public static void d(String tag, Object o) {
        if(DEBUG) {
            String log = toJson(o);
            Log.d(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void d(String tag, String msg, Throwable tr) {
        if(DEBUG) {
            String log = msg + '\n' + getStackTraceString(tr);
            Log.d(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void d(String tag, String format, Object... args) {
        if(DEBUG) {
            String log = String.format(format, args);
            Log.d(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void i(Object o) {
        if(DEBUG) {
            String log = toJson(o);
            Log.i(TAG, log);
            TLog2File.log2File(TAG, log);
        }

    }

    public static void i(String tag, Object o) {
        if(DEBUG) {
            String log = toJson(o);
            Log.i(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void i(String tag, String msg, Throwable tr) {
        if(DEBUG) {
            String log = msg + '\n' + getStackTraceString(tr);
            Log.i(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void i(String tag, String format, Object... args) {
        if(DEBUG) {
            String log = String.format(format, args);
            Log.i(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void w(Object o) {
        if(DEBUG) {
            String log = toJson(o);
            Log.w(TAG, log);
            TLog2File.log2File(TAG, log);
        }

    }

    public static void w(String tag, Object o) {
        if(DEBUG) {
            String log = toJson(o);
            Log.w(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void w(String tag, String msg, Throwable tr) {
        if(DEBUG) {
            String log = msg + '\n' + getStackTraceString(tr);
            Log.w(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void w(String tag, String format, Object... args) {
        if(DEBUG) {
            String log = String.format(format, args);
            Log.w(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void e(Object o) {
        if(DEBUG) {
            String log = toJson(o);
            Log.e(TAG, log);
            TLog2File.log2File(TAG, log);
        }

    }

    public static void e(String tag, Object o) {
        if(DEBUG) {
            String log = toJson(o);
            Log.e(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void e(String tag, String msg, Throwable tr) {
        if(DEBUG) {
            String log = msg + '\n' + getStackTraceString(tr);
            Log.e(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void e(String tag, String format, Object... args) {
        if(DEBUG) {
            String log = String.format(format, args);
            Log.e(tag, log);
            TLog2File.log2File(tag, log);
        }

    }

    public static void sysout(String msg) {
        try {
            Log.v(TAG, msg);
            TLog2File.log2File(TAG, msg);
        } catch (Throwable var2) {
            ;
        }

    }

    public static void printExc(Class<?> clazz, Throwable e) {
        try {
            if(DEBUG) {
                e.printStackTrace();
                TLog2File.log2File(TAG, e);
            } else {
                String ee = clazz == null?"Unknow":clazz.getSimpleName();
                Log.v(TAG, String.format("class[%s], %s", new Object[]{ee, e + ""}));
            }
        } catch (Throwable var3) {
            var3.printStackTrace();
        }

    }

    public static String toJson(Object msg) {
        if(msg instanceof String) {
            return msg.toString();
        } else {
            String json = JSON.toJSONString(msg);
            if(json.length() > 500) {
                json = json.substring(0, 500);
            }

            return json;
        }
    }

    static String getStackTraceString(Throwable tr) {
        if(tr == null) {
            return "";
        } else {
            for(Throwable t = tr; t != null; t = t.getCause()) {
                if(t instanceof UnknownHostException) {
                    return "";
                }
            }

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            tr.printStackTrace(pw);
            pw.flush();
            return sw.toString();
        }
    }
}
