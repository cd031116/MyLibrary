package com.lyj.cn.network.task;

/**
 * Created by lyj on 2017/3/22 0022.
 */

public interface IExceptionDeclare  {
    void checkResponse(String var1) throws TaskException;

    String checkCode(String var1);
}
