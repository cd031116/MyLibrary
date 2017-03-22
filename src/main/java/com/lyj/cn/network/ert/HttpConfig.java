package com.lyj.cn.network.ert;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/22 0022.
 */

public class HttpConfig {
    public String cookie;
    public String baseUrl;
    public Map<String, String> headerMap = new HashMap();

    public HttpConfig() {
    }

    public HttpConfig clone() throws CloneNotSupportedException {
        super.clone();
        HttpConfig httpConfig = new HttpConfig();
        httpConfig.cookie = this.cookie;
        httpConfig.baseUrl = this.baseUrl;
        httpConfig.headerMap = this.headerMap;
        return httpConfig;
    }

    public void addHeader(String key, String value) {
        this.headerMap.put(key, value);
    }
}
