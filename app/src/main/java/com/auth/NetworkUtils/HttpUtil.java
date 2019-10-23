package com.auth.NetworkUtils;

import android.annotation.TargetApi;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class HttpUtil {
    public static CookieManager cookieManager = new CookieManager();

    @TargetApi(24)
    public static JSONObject sendPostRequest (String requestRoute, JSONObject requestJson){
        CookieStore cookieStore = cookieManager.getCookieStore();

        String requestUrl = NetworkConstant.serverUrl + requestRoute;
        String requestData = requestJson.toString();
        Log.i("HttpMessage", String.format("requestPath: %s", requestUrl));
        Log.i("HttpMessage", String.format("requestData: %s", requestData));
        try
        {
            HttpURLConnection connection = (HttpURLConnection) new URL(requestUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(NetworkConstant.readTimeout);
            connection.setConnectTimeout(NetworkConstant.connectTimeout);

//            connection.connect();
            List<HttpCookie> cookies = cookieStore.getCookies();
            if (cookies.size() > 0){
                connection.setRequestProperty("Cookie", StringUtils.join(cookies, ";"));
            }
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestData.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            if(connection.getResponseCode() != 200)
            {
                Log.e("Request Error", String.format("Http request failed: %s", connection.getResponseCode()));
                return null;
            }

            List<String> setCookies = connection.getHeaderFields().get("Set-Cookie");
            if(setCookies != null){
                for(String setCookie: setCookies){
                    cookieManager.getCookieStore().add(null, HttpCookie.parse(setCookie).get(0));
                }
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line);
                stringBuilder.append("\r\n");
            }
            Log.i("HttpMessage", String.format("responseData: %s", stringBuilder.toString()));
            return new JSONObject(stringBuilder.toString());
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
