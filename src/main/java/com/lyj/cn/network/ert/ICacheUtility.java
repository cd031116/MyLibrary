package com.lyj.cn.network.ert;

import com.lyj.cn.setting.Setting;

/**
 * Created by Administrator on 2017/3/22 0022.
 */

public interface ICacheUtility {
    IResult findCacheData(Setting var1, Params var2);

    void addCacheData(Setting var1, Params var2, IResult var3);
}
