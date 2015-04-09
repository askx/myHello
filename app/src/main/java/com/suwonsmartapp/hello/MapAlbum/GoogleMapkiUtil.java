package com.suwonsmartapp.hello.mapalbum;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GoogleMapkiUtil {

    private String address = "";
    private String latitudePos = "";
    private String longitudePos = "";

    final static public String RESULT = "result";
    final static public String FAIL_MAP_RESULT = "fail_map_result";
    final static public String ERROR_RESULT = "error_result";
    final static public String SUCCESS_RESULT = "success_result";
    final static public String TIMEOUT_RESULT = "timeout_result";
    final static public String TAG_CLIENT = "client";
    final static public String TAG_SERVER = "server";
    public String stringData;
    public String searchingAddress;

    private SearchThread searchThread;
    private Handler resultHandler;
    private HttpClient httpclient;

    public GoogleMapkiUtil() {
    }

    public String getAddress() {
        return address;                 // return address
    }

    // 광범위한 주소를 통해 그 동네를 표시할 경우
    public void requestMapSearch(Handler _resultHandler, String searchingName, String nearAddress) {
        resultHandler = _resultHandler;

        List<BasicNameValuePair> qparams = new ArrayList<BasicNameValuePair>();
        try {
            searchingAddress = URLEncoder.encode(searchingName, "UTF-8");   // convert address to UTF-8
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        qparams.add(new BasicNameValuePair("address", searchingAddress));  // address=수원시+장안구
        qparams.add(new BasicNameValuePair("sensor", "true"));          // sensor=true
        qparams.add(new BasicNameValuePair("language", "ko"));          // language=ko

        searchThread = new SearchThread(qparams.toArray(new BasicNameValuePair[qparams.size()]));
        searchThread.start();                                           // search address
    }

    // 위도, 경도를 통해 주소를 얻을 경우
    public void requestPointSearch(Handler _resultHandler, String _latitude, String _longitude) {
        resultHandler = _resultHandler;

        List<BasicNameValuePair> qparams = new ArrayList<BasicNameValuePair>();
        qparams.add(new BasicNameValuePair("latlng", _latitude + "," + _longitude));  // address
        qparams.add(new BasicNameValuePair("sensor", "true"));          // sensor=true
        qparams.add(new BasicNameValuePair("language", "ko"));          // language=ko

        searchThread = new SearchThread(qparams.toArray(new BasicNameValuePair[qparams.size()]));
        searchThread.start();                                           // search address
    }

    private class SearchThread extends Thread {
        private String parameters;  // http 검색할 때 ? 다음에 들어갈 검색 구문 옵션 편집 변수

        public SearchThread(NameValuePair[] _nameValues) {
            parameters = encodeParams(_nameValues);
        }

        public void run() {
            httpclient = new DefaultHttpClient();   // maps.googleapis.com 사이트에 주소 검색 의뢰

            try {
                HttpGet get = new HttpGet();

                // google map v2.0 이후에는 googleapis를 호출해야 함. 현재는 v3.0 사용중.
                // parameters에 들어가는 값은 두 가지 경우가 있음.
                // case 1) 탐색 버튼을 누르고 주소를 입력하면 주소값이 parameters에 들어감.
                // case 2) 화면에서 롱클릭을 하면 위도와 경도값이 parameters에 들어감.
                get.setURI(new URI("http://maps.googleapis.com/maps/api/geocode/json?" + parameters));
                HttpParams params = httpclient.getParams();
                params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpConnectionParams.setConnectionTimeout(params, 10000);
                HttpConnectionParams.setSoTimeout(params, 10000);
                httpclient.execute(get, responseSearchHandler);

            } catch (ConnectTimeoutException e) {
                Message message = resultHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(RESULT, TIMEOUT_RESULT);
                message.setData(bundle);
                resultHandler.sendMessage(message);
                stringData = e.toString();

            } catch (Exception e) {
                Message message = resultHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(RESULT, ERROR_RESULT);
                message.setData(bundle);
                resultHandler.sendMessage(message);
                stringData = e.toString();
            } finally {
                httpclient.getConnectionManager().shutdown();
            }
        }
    }

    // A,B 로 호출하면 A=B& 를 parameters에 넣어줌.
    private String encodeParams(NameValuePair[] parameters) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < parameters.length; i++) {
            sb.append(parameters[i].getName());
            sb.append('=');                                         // 파라메터 사이는 "="
            sb.append(parameters[i].getValue().replace(" ", "+"));  // 스페이스는 플러스로 치환
            if (i + 1 < parameters.length)
                sb.append('&');                                     // 필드 구분은 "&"
        }

        return sb.toString();           // 만들어진 서치 구문을 건네줌.
    }

    private ResponseHandler<String> responseSearchHandler = new ResponseHandler<String>() {

        private String jsonString;

        @Override
        public String handleResponse(HttpResponse response)
                throws ClientProtocolException, IOException {
            StringBuilder sb = new StringBuilder();
            try {
                String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");

                // 주소를 넣어 검색하던지 위도/경도를 넣어 검색해도 동일한 포맷의 JSON 값이 리턴됨.
                // 어느 경우에나 주소와 위도/경도를 가져와 저장함.
                JSONObject rootObject = new JSONObject(responseString);
                JSONArray eventArray = rootObject.getJSONArray("results");

                // 주소
                address = eventArray.getJSONObject(0).getString("formatted_address");
                // 주소값에 대한민국 경기도 등도 포함되나 외국 검색을 위해 나라명도 필요함.

                // 위도, 경도
                latitudePos = eventArray.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitudePos = eventArray.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lng");

                if (address != null) {
                    ArrayList<String> searchList = new ArrayList<String>();
                    searchList.add(address);
                    searchList.add(latitudePos);
                    searchList.add(longitudePos);

                    Message message = resultHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString(RESULT, SUCCESS_RESULT);
                    bundle.putStringArrayList("searchList", searchList);
                    message.setData(bundle);
                    resultHandler.sendMessage(message);
                } else {
                    Message message = resultHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString(RESULT, FAIL_MAP_RESULT);
                    message.setData(bundle);
                    resultHandler.sendMessage(message);

                    stringData = "JSon >> \n" + sb.toString();
                    return stringData;
                }

            } catch (Exception e) {
                Message message = resultHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(RESULT, ERROR_RESULT);
                message.setData(bundle);
                resultHandler.sendMessage(message);

                stringData = "JSon >> \n" + e.toString();
                return stringData;
            }

            stringData = jsonString;
            return stringData;
        }
    };
}
