package com.lyj.cn.surport;

/**
 * Created by lyj on 2017/3/22 0022.
 */

public interface IPermissionsSubject {
    void attach(IPermissionsObserver var1);

    void detach(IPermissionsObserver var1);

    void notifyActivityResult(int var1, String[] var2, int[] var3);
}
