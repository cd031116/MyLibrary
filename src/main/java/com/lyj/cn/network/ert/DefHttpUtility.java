package com.lyj.cn.network.ert;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.lyj.cn.assist.TLog;
import com.lyj.cn.base.Adhibition;
import com.lyj.cn.network.task.TaskException;
import com.lyj.cn.setting.Setting;
import com.lyj.cn.setting.SettingUtility;
import com.lyj.cn.util.SystemUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Set;

import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by Administrator on 2017/3/22 0022.
 */

public class DefHttpUtility implements IHttpUtility {
    public DefHttpUtility() {
    }

    static String getTag(Setting action, String append) {
        return ABizLogic.getTag(action, append);
    }

    public <T> T doGet(HttpConfig config, Setting action, Params urlParams, Class<T> responseCls) throws TaskException {
        Request.Builder builder = this.createRequestBuilder(config, action, urlParams, "Get");
        Request request = builder.build();
        return this.executeRequest(request, responseCls, action, "Get");
    }

    public <T> T doPost(HttpConfig config, Setting action, Params urlParams, Params bodyParams, Object requestObj, Class<T> responseCls) throws TaskException {
        Request.Builder builder = this.createRequestBuilder(config, action, urlParams, "Post");
        String requestBodyStr;
        if(bodyParams != null) {
            requestBodyStr = ParamsUtil.encodeToURLParams(bodyParams);
            TLog.d(getTag(action, "Post"), requestBodyStr);
            builder.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), requestBodyStr));
        } else if(requestObj != null) {
            if(requestObj instanceof String) {
                requestBodyStr = requestObj + "";
            } else {
                requestBodyStr = JSON.toJSONString(requestObj);
            }

            TLog.d(getTag(action, "Post"), requestBodyStr);
            builder.post(RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), requestBodyStr));
        }

        return this.executeRequest(builder.build(), responseCls, action, "Post");
    }

    public <T> T doPostFiles(HttpConfig config, Setting action, Params urlParams, Params bodyParams, MultipartFile[] files, Class<T> responseCls) throws TaskException {
        String method = "doPostFiles";
        Request.Builder builder = this.createRequestBuilder(config, action, urlParams, method);
        MultipartBuilder multipartBuilder = new MultipartBuilder();
        multipartBuilder.type(MultipartBuilder.FORM);
        if(bodyParams != null && bodyParams.getKeys().size() > 0) {
            Iterator requestBody = bodyParams.getKeys().iterator();

            while(requestBody.hasNext()) {
                String key = (String)requestBody.next();
                String value = bodyParams.getParameter(key);
                multipartBuilder.addFormDataPart(key, value);
                TLog.d(getTag(action, method), "BodyParam[%s, %s]", new Object[]{key, value});
            }
        }

        if(files != null && files.length > 0) {
            MultipartFile[] var14 = files;
            int var16 = files.length;

            for(int var17 = 0; var17 < var16; ++var17) {
                MultipartFile file = var14[var17];
                if(file.getBytes() != null) {
                    multipartBuilder.addFormDataPart(file.getKey(), file.getKey(), createRequestBody(file));
                    TLog.d(getTag(action, method), "Multipart bytes, length = " + file.getBytes().length);
                } else if(file.getFile() != null) {
                    multipartBuilder.addFormDataPart(file.getKey(), file.getFile().getName(), createRequestBody(file));
                    TLog.d(getTag(action, method), "Multipart file, name = %s, path = %s", new Object[]{file.getFile().getName(), file.getFile().getAbsolutePath()});
                }
            }
        }

        RequestBody var15 = multipartBuilder.build();
        builder.post(var15);
        return this.executeRequest(builder.build(), responseCls, action, method);
    }

    private Request.Builder createRequestBuilder(HttpConfig config, Setting action, Params urlParams, String method) throws TaskException {
        if(Adhibition.getInstance() != null && SystemUtils.getNetworkType(Adhibition.getInstance()) == SystemUtils.NetWorkType.none) {
            TLog.w(getTag(action, method), "没有网络连接");
            throw new TaskException(TaskException.TaskError.noneNetwork.toString());
        } else {
            String url = (config.baseUrl + action.getValue() + (urlParams == null?"":"?" + ParamsUtil.encodeToURLParams(urlParams))).replaceAll(" ", "");
            TLog.d(getTag(action, method), url);
            Request.Builder builder = new Request.Builder();
            builder.url(url);
            if(!TextUtils.isEmpty(config.cookie)) {
                builder.header("Cookie", config.cookie);
                TLog.d(getTag(action, method), "Cookie = " + config.cookie);
            }

            if(config.headerMap.size() > 0) {
                Set keySet = config.headerMap.keySet();
                Iterator var8 = keySet.iterator();

                while(var8.hasNext()) {
                    String key = (String)var8.next();
                    builder.addHeader(key, (String)config.headerMap.get(key));
                    TLog.d(getTag(action, method), "Header[%s, %s]", new Object[]{key, config.headerMap.get(key)});
                }
            }

            return builder;
        }
    }

    private <T> T executeRequest(Request request, Class<T> responseCls, Setting action, String method) throws TaskException {
        try {
            if(SettingUtility.getPermanentSettingAsInt("http_delay") > 0) {
                Thread.sleep((long) SettingUtility.getPermanentSettingAsInt("http_delay"));
            }
        } catch (Throwable var11) {
            ;
        }

        try {
            Response e = this.getOkHttpClient().newCall(request).execute();
            TLog.w(getTag(action, method), "Http-code = %d", new Object[]{Integer.valueOf(e.code())});
            String responseStr;
            if(e.code() != 200 && e.code() != 206) {
                responseStr = e.body().string();
                if(TLog.DEBUG) {
                    TLog.w(getTag(action, method), responseStr);
                }

                TaskException.checkResponse(responseStr);
                throw new TaskException(TaskException.TaskError.timeout.toString());
            } else {
                responseStr = e.body().string();
                TLog.v(getTag(action, method), "Response = %s", new Object[]{responseStr});
                return this.parseResponse(responseStr, responseCls);
            }
        } catch (SocketTimeoutException var7) {
            TLog.printExc(DefHttpUtility.class, var7);
            TLog.w(getTag(action, method), var7 + "");
            throw new TaskException(TaskException.TaskError.timeout.toString());
        } catch (IOException var8) {
            TLog.printExc(DefHttpUtility.class, var8);
            TLog.w(getTag(action, method), var8 + "");
            throw new TaskException(TaskException.TaskError.timeout.toString());
        } catch (TaskException var9) {
            TLog.printExc(DefHttpUtility.class, var9);
            TLog.w(getTag(action, method), var9 + "");
            throw var9;
        } catch (Exception var10) {
            TLog.printExc(DefHttpUtility.class, var10);
            TLog.w(getTag(action, method), var10 + "");
            throw new TaskException(TaskException.TaskError.resultIllegal.toString());
        }
    }

    protected <T> T parseResponse(String resultStr, Class<T> responseCls) throws TaskException {
        if(responseCls.getSimpleName().equals("String")) {
            return (T) resultStr;
        } else {
            Object result = JSON.parseObject(resultStr, responseCls);
            return (T) result;
        }
    }

    public synchronized OkHttpClient getOkHttpClient() {
        return Adhibition.getOkHttpClient();
    }

    static RequestBody createRequestBody(final MultipartFile file) {
        return new RequestBody() {
            public MediaType contentType() {
                return MediaType.parse(file.getContentType());
            }

            public long contentLength() throws IOException {
                return file.getBytes() != null?(long)file.getBytes().length:file.getFile().length();
            }

            public void writeTo(BufferedSink sink) throws IOException {
                Source source;
                if(file.getFile() != null) {
                    source = Okio.source(file.getFile());
                } else {
                    source = Okio.source(new ByteArrayInputStream(file.getBytes()));
                }

                OnFileProgress onFileProgress = file.getOnProgress();
                if(onFileProgress != null) {
                    try {
                        long e = this.contentLength();
                        long writeLen = 0L;
                        long readLen = -1L;
                        Buffer buffer = new Buffer();
                        long MIN_PROGRESS_STEP = 65536L;
                        long MIN_PROGRESS_TIME = 300L;
                        long mLastUpdateBytes = 0L;
                        long mLastUpdateTime = 0L;

                        while(true) {
                            long now;
                            do {
                                if((readLen = source.read(buffer, 8192L)) == -1L) {
                                    return;
                                }

                                sink.write(buffer, readLen);
                                writeLen += readLen;
                                now = System.currentTimeMillis();
                            } while((writeLen - mLastUpdateBytes <= MIN_PROGRESS_STEP || now - mLastUpdateTime <= MIN_PROGRESS_TIME) && writeLen != e);

                            onFileProgress.onProgress(writeLen, e);
                            mLastUpdateBytes = writeLen;
                            mLastUpdateTime = now;
                        }
                    } catch (IOException var33) {
                        TLog.printExc(DefHttpUtility.class, var33);
                        throw var33;
                    } finally {
                        Util.closeQuietly(source);
                    }
                } else {
                    try {
                        sink.writeAll(source);
                    } catch (IOException var31) {
                        TLog.printExc(DefHttpUtility.class, var31);
                        throw var31;
                    } finally {
                        Util.closeQuietly(source);
                    }
                }

            }
        };
    }
}
