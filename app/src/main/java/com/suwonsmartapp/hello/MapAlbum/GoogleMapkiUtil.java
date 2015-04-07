package com.suwonsmartapp.hello.mapalbum;

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

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class GoogleMapkiUtil {

    final static public String RESULT = "result";
    final static public String FAIL_MAP_RESULT = "fail_map_result";
    final static public String ERROR_RESULT = "error_result";
    final static public String SUCCESS_RESULT = "success_result";
    final static public String TIMEOUT_RESULT = "timeout_result";
    final static public String TAG_CLIENT = "client";
    final static public String TAG_SERVER = "server";
    public String stringData;

    private SearchThread searchThread;
    private Handler resultHandler;
    private HttpClient httpclient;

    public GoogleMapkiUtil() {
    }

    public void requestMapSearch(Handler _resultHandler, String searchingName, String nearAddress) {
        resultHandler = _resultHandler;

        List<BasicNameValuePair> qparams = new ArrayList<BasicNameValuePair>();
        qparams.add(new BasicNameValuePair("address", searchingName)); // address=수원시+장안구
        qparams.add(new BasicNameValuePair("sensor", "true_or_false"));// sensor=true_or_false
//        qparams.add(new BasicNameValuePair("output", "json"));      // output=json
//        qparams.add(new BasicNameValuePair("mrt", "yp"));           // mrt=yp
//        qparams.add(new BasicNameValuePair("hl", "ko"));            // hl=ko
//        qparams.add(new BasicNameValuePair("radius", "18.641"));    //radius=18.641
//
//        // miles = kilometers / 1.60934
//        qparams.add(new BasicNameValuePair("num", "5"));            // num=5
//        qparams.add(new BasicNameValuePair("near", nearAddress));   // near=현재나의위치
        searchThread = new SearchThread(qparams.toArray(new BasicNameValuePair[qparams.size()]));
        searchThread.start();           // 주소 서치
    }

    private class SearchThread extends Thread {
        private String parameters;

        public SearchThread(NameValuePair[] _nameValues) {
            parameters = encodeParams(_nameValues);
        }

        public void run() {
            httpclient = new DefaultHttpClient();   // maps.google.co.kr 사이트에 주소 검색 의뢰
            try {
                HttpGet get = new HttpGet();

                // http://maps.google.com/maps/api/geocode/json?latlng=37.572826,126.976853&sensor=false&language='ko'
                //http://maps.googleapis.com/maps/api/geocode/json?address=주소&sensor=true_or_false
                // get.setURI(new URI("http://maps.google.co.kr?" + parameters));
                parameters = URLEncoder.encode("수원시+장안구", "UTF-8");
                // get.setURI(new URI("http://maps.googleapis.com/maps/api/geocode/json?sensor=true&address=" + parameters));
                get.setURI(new URI("http://maps.googleapis.com/maps/api/geocode/json?sensor=true&language=ko&address=" + parameters));
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
            try {
                String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");

                String formatted_address = "";
                String lat = "";
                String lng = "";
                JSONObject rootObject = new JSONObject(responseString);
                JSONArray eventArray = rootObject.getJSONArray("results");
                for (int i = 0; i < eventArray.length(); i++) {
                    JSONObject jsonObject = eventArray.getJSONObject(i);

                    // 주소
                    formatted_address = jsonObject.getString("formatted_address");

                    if (!TextUtils.isEmpty(formatted_address)) {
                        break;
                    }
                }

                // 위도, 경고
                lat = eventArray.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lat");
                lng = eventArray.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lng");

                // 로그 확인
                Log.d("Address", formatted_address);
                Log.d("Lat", lat);
                Log.d("Lng", lng);


//                int lastAddrOf = jsonResult.lastIndexOf("formatted_address");
//                int startAddressIndex = lastAddrOf + 20;
//                int endAddressIndex = lastAddrOf;
//                String address = "";
//                String compareChar = "\"";
//                for (int i = startAddressIndex; i < jsonResult.length(); i++) {
//                    if (compareChar.equals(jsonResult.charAt(i))) {
//                        break;
//                    } else {
//                        address = address + jsonResult.charAt(i);
//                    }
//                }
//
//                int lastPosOf = jsonResult.lastIndexOf("location\"");
//                int startPosIndex = lastPosOf + 17;
//                int lastPosIndex = 0;
//                String latitudePos = "";
//                String longitudePos = "";
//                compareChar = ",";
//                for (int i = startPosIndex; i < jsonResult.length(); i++) {
//                    if (compareChar.equals(jsonResult.charAt(i))) {
//                        break;
//                    } else {
//                        latitudePos = latitudePos + jsonResult.charAt(i);
//                        lastPosIndex = i;
//                    }
//                }
//
//                lastPosIndex = lastPosIndex + 10;
//                compareChar = "\n";
//                for (int i = lastPosIndex; i < jsonResult.length(); i++) {
//                    if (compareChar.equals(jsonResult.charAt(i))) {
//                        break;
//                    } else {
//                        longitudePos = longitudePos + jsonResult.charAt(i);
//                    }
//                }



//                JSONObject geometry = eventArray.getJSONObject("results");
//                JSONObject addr = geometry.getJSONObject("address_components");
//                JSONArray markers = addr.getJSONArray("formatted_address");
//
//                // JSONObject overlays = jj.getJSONObject("overlays");
//                // JSONArray markers = overlays.getJSONArray("markers");
//                if (markers != null) {
//                    ArrayList<String> searchList = new ArrayList<String>();
//                    String lat, lon;
//                    String addresses;
//                    for (int i = 0; i < markers.length(); i++) {
//                        addresses = markers.getJSONObject(i).getString("laddr");
//                        lat = markers.getJSONObject(i).getJSONObject("latlng")
//                                .getString("lat");
//                        lon = markers.getJSONObject(i).getJSONObject("latlng")
//                                .getString("lng");
//                        searchList.add(addresses);
//                        searchList.add(lat);
//                        searchList.add(lon);
//                    }
//
//                    Message message = resultHandler.obtainMessage();
//                    Bundle bundle = new Bundle();
//                    bundle.putString(RESULT, SUCCESS_RESULT);
//                    bundle.putStringArrayList("searchList", searchList);
//                    message.setData(bundle);
//                    resultHandler.sendMessage(message);
//                } else {
//                    Message message = resultHandler.obtainMessage();
//                    Bundle bundle = new Bundle();
//                    bundle.putString(RESULT, FAIL_MAP_RESULT);
//                    message.setData(bundle);
//                    resultHandler.sendMessage(message);
//
//                    stringData = "JSon >> \n" + sb.toString();
//                    return stringData;
//                }

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
