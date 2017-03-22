package com.lyj.cn.network.task;

/**
 * Created by lyj on 2017/3/22 0022.
 */

public interface ITaskManager {
    void addTask(WorkTask var1);

    void removeTask(String var1, boolean var2);

    void removeAllTask(boolean var1);

    int getTaskCount(String var1);
}
