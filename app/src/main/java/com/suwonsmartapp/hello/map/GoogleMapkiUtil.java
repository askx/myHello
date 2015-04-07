package com.suwonsmartapp.hello.map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
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
        qparams.add(new BasicNameValuePair("q", searchingName));
        qparams.add(new BasicNameValuePair("output", "json"));
        qparams.add(new BasicNameValuePair("mrt", "yp"));
        qparams.add(new BasicNameValuePair("hl", "ko"));
        qparams.add(new BasicNameValuePair("radius", "18.641"));

        // miles = kilometers / 1.60934
        qparams.add(new BasicNameValuePair("num", "5"));
        qparams.add(new BasicNameValuePair("near", nearAddress));
        searchThread = new SearchThread(qparams.toArray(new BasicNameValuePair[qparams.size()]));
        searchThread.start();
    }

    private class SearchThread extends Thread {
        private String parameters;

        public SearchThread(NameValuePair[] _nameValues) {
            parameters = encodeParams(_nameValues);
        }

        public void run() {
            httpclient = new DefaultHttpClient();
            try {
                HttpGet get = new HttpGet();
                get.setURI(new URI("http://maps.google.co.kr?" + parameters));
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
            sb.append('=');
            sb.append(parameters[i].getValue().replace(" ", "+"));
            if (i + 1 < parameters.length)
                sb.append('&');
        }

        return sb.toString();
    }

    private ResponseHandler<String> responseSearchHandler = new ResponseHandler<String>() {

        private String jsonString;

        @Override
        public String handleResponse(HttpResponse response)
                throws ClientProtocolException, IOException {
            StringBuilder sb = new StringBuilder();
            try {
                InputStreamReader isr = new InputStreamReader(response
                        .getEntity().getContent(), "EUC-KR");
                BufferedReader br = new BufferedReader(isr);
                for (;;) {
                    String line = br.readLine();
                    if (line == null)
                        break;
                    sb.append(line + '\n');
                }
                br.close();

                String jsonString = sb.toString().substring(9);
                JSONObject jj = new JSONObject(jsonString);
                JSONObject overlays = jj.getJSONObject("overlays");
                JSONArray markers = overlays.getJSONArray("markers");
                if (markers != null) {
                    ArrayList<String> searchList = new ArrayList<String>();
                    String lat, lon;
                    String address;
                    for (int i = 0; i < markers.length(); i++) {
                        address = markers.getJSONObject(i).getString("laddr");
                        lat = markers.getJSONObject(i).getJSONObject("latlng")
                                .getString("lat");
                        lon = markers.getJSONObject(i).getJSONObject("latlng")
                                .getString("lng");
                        searchList.add(address);
                        searchList.add(lat);
                        searchList.add(lon);
                    }

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
