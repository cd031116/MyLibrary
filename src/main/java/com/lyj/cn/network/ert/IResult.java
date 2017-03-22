package com.lyj.cn.network.ert;

/**
 * Created by Administrator on 2017/3/22 0022.
 */

public interface IResult {
    boolean outofdate();

    boolean fromCache();

    boolean endPaging();

    String[] pagingIndex();
}

