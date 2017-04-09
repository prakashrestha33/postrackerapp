package com.android.postracker.jsonparse;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class JsonParser {

    private static final String base_url= "192.168.0.112:8000/api";

    public JSONObject performPostCI(String requestURL,
                                    HashMap<String, String> postDataParams) {
        String json = null;
        JSONObject jObj = null;
        HttpURLConnection connection = null;
        InputStream in;
        String line;
        try {
            Log.e("url", requestURL);

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .encodedAuthority(base_url)
                    .appendEncodedPath(requestURL)
                    .build();
            URL u;
            // Create connection
            u = new URL(URLDecoder.decode(builder.toString(),"UTF-8"));
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setRequestProperty("charset", "EN-US");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.connect();

            // Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(getPostDataString(postDataParams));
            try {
                wr.flush();
                wr.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }


        } catch (UnknownHostException e) {
            Log.e("connection", "failed here");
            e.printStackTrace();
            return null;
        } catch (IOException e) {

            e.printStackTrace();
        }
        try {
            int status = connection != null ? connection.getResponseCode() : 0;
            if (status >= HttpURLConnection.HTTP_OK)
                in = connection.getInputStream();
            else
                in = connection != null ? connection.getErrorStream() : null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in != null ? in : null));
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            json = sb.toString();
            Log.e("response data", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (json != null)
                jObj = new JSONObject(json);
            else {
                return null;
            }
        } catch (JSONException ignored) {
        } finally {
            assert connection != null;
            connection.disconnect();
        }
        return jObj;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public JSONObject getForJSONObject(String url,String token) {
        String json = null;
        JSONObject jObj = null;
        HttpURLConnection connection = null;
        BufferedReader reader;
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .encodedAuthority(base_url)
                    .appendEncodedPath(url)
                    .appendQueryParameter("api_token",token)
                    .build();

//            Log.e("url", url);
            URL u;
            // Create connection
            u = new URL(URLDecoder.decode(builder.toString(),"UTF-8"));
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setConnectTimeout(15000);
            connection.setChunkedStreamingMode(0);

            connection.connect();

        } catch (UnknownHostException e) {
            Log.e("connection", "failed here");
            e.printStackTrace();
            return null;
        } catch (IOException e) {

            e.printStackTrace();
        }

        try {
            reader = new BufferedReader(new InputStreamReader(
                    connection != null ? connection.getInputStream() : null), 8);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            json = sb.toString();

//            Log.i("JSON", json);
        } catch (Exception ignored) {
        }
        try {
            if (json != null)
                jObj = new JSONObject(json);
            else {
                return null;
            }
        } catch (JSONException ignored) {
        } finally {
            if (connection !=null) {
                connection.disconnect();
            }
        }

        return jObj;
    }
}