package com.suwonsmartapp.hello.map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.suwonsmartapp.hello.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;


public class MapActivity extends FragmentActivity {

    private GoogleMap mGoogleMap;
    private StringBuffer strAddr;
    private EditText m_text_map;        // 주소 입력창
    private TextView m_btn_address;     // 주소 검색 버튼
    private TextView m_btn_map;         // 지도 모양 바꾸기 버튼
    private TextView m_btn_savemap;     // 지도 저장
    private boolean mapType = true;     // true = 지도 -> 위성, false = 위성 -> 지도
    private String coordinates[] = { "37.517180", "127.041268" };   // 위도와 경도를 초기화.
    private double initialLatitude = 37.0d + 51.0d/60 + 71.0d/(60*60) + 80.0d/(60*60*60);
    private double initialLongitude = 127.0d + 04.0d/60 + 12.0d/(60*60) + 68.0d/(60*60*60);
    public static LatLng DEFAULT_GP = new LatLng(37.517180, 127.041268);
    private double minLatitude =  +81;      // 위도 최대값
    private double maxLatitude =  -81;      // 위도 최소값
    private double minLongitude = +181;     // 경도 최대값
    private double maxLongitude = -181;     // 경도 최소값
    private double latitude = 0;        // 위도
    private double longitude = 0;       // 경도
    private Point screenPt;
    private LatLng latitudeLongitude;
    private Handler handler;


    private ProgressDialog progressDialog;
    private String errorString = "";
    private ImageButton searchBt;
    private GoogleMapkiUtil httpUtil;
    private AlertDialog errorDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setTitle("맵앨범");    // 프로그램 타이틀

        initPosition();     // 최초 표시 위치 초기화

        setUpMapIfNeeded();  // 맵 준비

        // httpUtil
        httpUtil = new GoogleMapkiUtil();

        errorDialog = new AlertDialog.Builder(this).setTitle("맵앨범")
                .setMessage(errorString).setPositiveButton("닫기", null)
                .create();

        handler = new Handler(getMainLooper());
        goToSeoul();

        Location lo = mGoogleMap.getMyLocation();

        if (mGoogleMap != null) {
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                public void onMapClick(LatLng point) {
                    getMapPosition(point);

                    Geocoder gcoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    strAddr = new StringBuffer();
                    try {
                        List<Address> lstAddrs = gcoder.getFromLocation(latitude, longitude, 1);
                        for (Address addr : lstAddrs) {
                            int idx = addr.getMaxAddressLineIndex();
                            for (int i = 1; i <= idx; i++) {
                                strAddr.append(addr.getAddressLine(i));
                                Log.v("addr", addr.getAddressLine(i));
                            }
                        }
                        Toast.makeText(getApplicationContext(), strAddr.toString(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });

            mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                public void onMapLongClick(LatLng point) {
                    // 현재 위도와 경도에서 화면 포인트를 알려줌.
                    getMapPosition(point);

                    Geocoder gcoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    strAddr = new StringBuffer();
                    try {
                        List<Address> lstAddrs = gcoder.getFromLocation(latitude, longitude, 1);
                        for (Address addr : lstAddrs) {
                            int idx = addr.getMaxAddressLineIndex();
                            for (int i = 1; i <= idx; i++) {
                                strAddr.append(addr.getAddressLine(i));
                                Log.v("addr", addr.getAddressLine(i));
                                }
                            }
                        Toast.makeText(getApplicationContext(), strAddr.toString(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                }
            });

            final TextView m_btn_map = (TextView) findViewById(R.id.btn_map);
            m_btn_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mapType == true) {
                        m_btn_map.setText("지도");
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    } else {
                        m_btn_map.setText("위성");
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    }
                    mapType = !mapType;     // toggle map type

                }
            });

            // 주소 검색 버튼이 눌려졌을 경우: 지도에서 주소를 얻어 리턴하고 필드를 지움.
            final TextView m_btn_address = (TextView) findViewById(R.id.btn_address);
            m_btn_address.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {



                    Geocoder gcoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List<Address> lstAddr;
                    EditText m_text_map = (EditText)findViewById(R.id.text_map);

                    Location lo = mGoogleMap.getMyLocation();
                    m_text_map.setText(getAddres(lo.getLatitude(), lo.getLongitude()));


//                    try {
//                        lstAddr = gcoder.getFromLocationName(m_text_map.getText().toString(), 1);
//                        if (lstAddr != null && lstAddr.size() > 0) {
//                            Address addr = lstAddr.get(0);
//                            latitude = addr.getLatitude() * 1E6;
//                            longitude = addr.getLongitude() * 1E6;
////                            GeoPoint gpo = new GeoPoint(latitude, longitude);
////                            mGoogleMap.getController().animateTo(gpo);
//                            }
//                        } catch (IOException e) {
//                        e.printStackTrace();
//                        }

                }
            });

            // 저장 버튼이 눌려졌을 경우: 지도를 지우고 종료
            final TextView m_btn_savemap = (TextView) findViewById(R.id.btn_savemap);
            m_btn_address.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

        }
    }

    private void initPosition() {
        coordinates[0] = String.valueOf(initialLatitude);
        coordinates[1] = String.valueOf(initialLongitude);
        latitude = Double.parseDouble(coordinates[0]);        // 위도
        longitude = Double.parseDouble(coordinates[1]);       // 경도
        latitudeLongitude = new LatLng(latitude, longitude);
    };


    private void getMapPosition(LatLng point) {
        // 현재 위도와 경도에서 화면 포인트를 알려줌.
        screenPt = mGoogleMap.getProjection().toScreenLocation(point);

        // 현재 화면에 찍힌 포인트로 부터 위도와 경도를 알려줌.
        latitudeLongitude = mGoogleMap.getProjection().fromScreenLocation(screenPt);

        coordinates[0] = String.valueOf(point.latitude);
        coordinates[1] = String.valueOf(point.longitude);
        latitude = Double.parseDouble(coordinates[0]);        // 위도
        longitude = Double.parseDouble(coordinates[1]);       // 경도
        latitudeLongitude = new LatLng(latitude, longitude);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latitudeLongitude, 15));
    }

    private void setUpMapIfNeeded() {
        if (mGoogleMap == null) {
            FragmentManager manager = getSupportFragmentManager();
            SupportMapFragment fragment = (SupportMapFragment)manager.findFragmentById(R.id.map);
            mGoogleMap = fragment.getMap();

            if (mGoogleMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        Log.d("dd", "setUpMap");
        mGoogleMap.setMapType(MAP_TYPE_NORMAL);
        mGoogleMap.setMyLocationEnabled(true);
    }

    private void goToSeoul() {
        handler.post(findSeoul);
    }

    private Runnable findSeoul = new Runnable() {

        @Override
        public void run() {
            if(mGoogleMap != null) {
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(DEFAULT_GP, 5f);
                mGoogleMap.moveCamera(cu);
            } else {
                handler.postDelayed(findSeoul, 100);
            }
        }
    };

    protected void onStop() {
        handler.removeCallbacks(findSeoul);
        super.onStop();
    };

    private String getAddres(double lat, double lng) {
        Geocoder gcK = new Geocoder(getApplicationContext(), Locale.KOREA);
        String res = "대한민국";
        try {
            List<Address> addresses = gcK.getFromLocation(lat, lng, 1);
            StringBuilder sb = new StringBuilder();

            if (null != addresses && addresses.size() > 0) {
                Address address = addresses.get(0);
                // sb.append(address.getCountryName()).append("/");
                // sb.append(address.getPostalCode()).append("/");
                sb.append(address.getLocality()).append("/");
                sb.append(address.getThoroughfare()).append("/");
                sb.append(address.getFeatureName());
                res = sb.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }










}




