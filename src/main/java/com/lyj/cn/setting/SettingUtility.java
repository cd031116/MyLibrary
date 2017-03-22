package com.lyj.cn.setting;

import android.content.Context;

import com.lyj.cn.base.Adhibition;
import com.lyj.cn.util.ActivityHelper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2017/3/22 0022.
 */

public class SettingUtility {
    private static Map<String, Setting> settingMap = new HashMap();

    private SettingUtility() {
    }

    public static void addSettings(Context context, String settingsXmlName) {
        Map newSettingMap = SettingsXmlParser.parseSettings(context, settingsXmlName);
        Set keySet = newSettingMap.keySet();
        Iterator var4 = keySet.iterator();

        while(var4.hasNext()) {
            String key = (String)var4.next();
            settingMap.put(key, newSettingMap.get(key));
        }

    }

    public static boolean getBooleanSetting(String type) {
        return settingMap.containsKey(type)?Boolean.parseBoolean(((Setting)settingMap.get(type)).getValue()):false;
    }

    public static int getIntSetting(String type) {
        return settingMap.containsKey(type)?Integer.parseInt(((Setting)settingMap.get(type)).getValue()):-1;
    }

    public static String getStringSetting(String type) {
        return settingMap.containsKey(type)?((Setting)settingMap.get(type)).getValue():null;
    }

    public static Setting getSetting(String type) {
        return settingMap.containsKey(type)?(Setting)settingMap.get(type):null;
    }

    public static void setPermanentSetting(String type, boolean value) {
        ActivityHelper.putBooleanShareData(Adhibition.getInstance(), type, value);
    }

    public static boolean getPermanentSettingAsBool(String type, boolean def) {
        return ActivityHelper.getBooleanShareData(Adhibition.getInstance(), type, settingMap.containsKey(type)?Boolean.parseBoolean(((Setting)settingMap.get(type)).getValue()):def);
    }

    public static void setPermanentSetting(String type, int value) {
        ActivityHelper.putIntShareData(Adhibition.getInstance(), type, value);
    }

    public static int getPermanentSettingAsInt(String type) {
        return ActivityHelper.getIntShareData(Adhibition.getInstance(), type, settingMap.containsKey(type)?Integer.parseInt(((Setting)settingMap.get(type)).getValue()):-1);
    }

    public static void setPermanentSetting(String type, String value) {
        ActivityHelper.putShareData(Adhibition.getInstance(), type, value);
    }

    public static String getPermanentSettingAsStr(String type, String def) {
        return ActivityHelper.getShareData(Adhibition.getInstance(), type, settingMap.containsKey(type)?((Setting)settingMap.get(type)).getValue():def);
    }
}
