package org.fight4edu.web;


import okhttp3.*;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WebClient {

    private OkHttpClient httpClient;

    public WebClient() {
        httpClient  = new OkHttpClient.Builder()
                .addInterceptor(
                        new Interceptor() {
                            @Override
                            public Response intercept(Interceptor.Chain chain) throws IOException {
                                Request request = chain.request().newBuilder()
                                        .addHeader("Accept", "Application/JSON").build();
                                return chain.proceed(request);
                            }}).build();
    }

    public Object doPostRequest(Object obj, String url) throws IOException {

        Map<String, String> map = new HashMap<String, String>();
        map.put("Content-Type","application/json");
        Headers headers = Headers.of(map);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        byte[] bodyBytes = SerializationUtils.serialize((Serializable) obj);
        String stringBody = new String(bodyBytes);

        RequestBody body = RequestBody.create(stringBody, JSON);

        Request request = new Request.Builder().post(body).headers(headers).url(url).build();
        Response response = httpClient.newCall(request).execute();

        ResponseBody responseBody = response.body();
        return responseBody;

    }
}
