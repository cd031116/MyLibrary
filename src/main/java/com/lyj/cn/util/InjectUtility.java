package com.lyj.cn.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;

import com.lyj.cn.assist.TLog;
import com.lyj.cn.surport.OnClick;
import com.lyj.cn.surport.ViewInject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/3/22 0022.
 */

public class InjectUtility {
    static final String TAG = "InjectUtility";

    public InjectUtility() {
    }

    public static void initInjectedView(Activity sourceActivity) {
        initInjectedView(sourceActivity, sourceActivity, sourceActivity.getWindow().getDecorView());
    }

    public static void initInjectedView(Context context, final Object injectedSource, View sourceView) {
        long start = System.currentTimeMillis();

        for(Class clazz = injectedSource.getClass(); clazz != Object.class && !clazz.getName().startsWith("android"); clazz = clazz.getSuperclass()) {
            Method[] methods = clazz.getDeclaredMethods();
            Method[] fields = methods;
            int var8 = methods.length;

            int var9;
            for(var9 = 0; var9 < var8; ++var9) {
                final Method method = fields[var9];
                Class[] field = method.getParameterTypes();
                if(field != null && field.length == 1 && field[0].getName().equals(View.class.getName())) {
                    OnClick viewInject = (OnClick)method.getAnnotation(OnClick.class);
                    if(viewInject != null) {
                        int[] viewId = viewInject.value();
                        int[] e = viewId;
                        int packageName = viewId.length;

                        for(int resources = 0; resources < packageName; ++resources) {
                            int id = e[resources];
                            if(id != -1) {
                                View view = sourceView.findViewById(id);
                                if(view != null) {
                                    view.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {
                                            try {
                                                method.setAccessible(true);
                                                method.invoke(injectedSource, new Object[]{v});
                                            } catch (Throwable var3) {
                                                var3.printStackTrace();
                                            }

                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }

            Field[] var21 = clazz.getDeclaredFields();
            if(var21 != null && var21.length > 0) {
                Field[] var22 = var21;
                var9 = var21.length;

                for(int var23 = 0; var23 < var9; ++var23) {
                    Field var24 = var22[var23];
                    ViewInject var25 = (ViewInject)var24.getAnnotation(ViewInject.class);
                    if(var25 != null) {
                        int var26 = var25.id();
                        if(var26 == 0) {
                            String var27 = var25.idStr();
                            if(!TextUtils.isEmpty(var27)) {
                                try {
                                    String var28 = context.getPackageName();
                                    Resources var29 = context.getPackageManager().getResourcesForApplication(var28);
                                    var26 = var29.getIdentifier(var27, "id", var28);
                                    if(var26 == 0) {
                                        throw new RuntimeException(String.format("%s 的属性%s关联了id=%s，但是这个id是无效的", new Object[]{clazz.getSimpleName(), var24.getName(), var27}));
                                    }
                                } catch (Exception var20) {
                                    ;
                                }
                            }
                        }

                        if(var26 != 0) {
                            try {
                                var24.setAccessible(true);
                                if(var24.get(injectedSource) == null) {
                                    var24.set(injectedSource, sourceView.findViewById(var26));
                                }
                            } catch (Exception var19) {
                                TLog.printExc(InjectUtility.class, var19);
                            }
                        }
                    }
                }
            }
        }

        if(TLog.DEBUG) {
            TLog.v("InjectUtility", "耗时 %s ms : " + injectedSource, new Object[]{String.valueOf(System.currentTimeMillis() - start)});
        }

    }
}
