package com.suwonsmartapp.hello.mapalbum;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.suwonsmartapp.hello.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MapAlbumActivity extends FragmentActivity {

    private boolean flagFinish = false;          // APP을 종료할지 결정하는 플래그(true=다른 APP이 부른 경우)
    private boolean semaphoreLongTouch = false; // 롱터치가 발생했는지를 알리는 플래그
    private boolean semaphoreMapReady = false;  // Map이 준비되었는지 알리는 플래그

    public static LatLng DEFAULT_GP = new LatLng(37.566500, 126.978000);

    // 위도와 경도의 최대/최소값
    // 위도의 최소/최대는 -90/90, 경도의 최소/최대는 -180/180
    private double minLatitude =  +90;
    private double maxLatitude =  -90;
    private double minLongitude = +180;
    private double maxLongitude = -180;

    protected GoogleMap mMap;
    private StringBuffer strAddr;
    private ProgressDialog progressDialog;
    private String errorString = "";
    private ImageButton searchBt;       // 돋보기 탐색 버튼
    private GoogleMapUtility httpUtil;   // mapapis를 통해 query를 하는 모듈
    private AlertDialog errorDialog;
    private Handler handler;            // 메시지 핸들러

    private String coordinates[] = { "37.517180", "127.041268" };   // 위도와 경도를 초기화.
    private double latitude = 0;        // 위도
    private double longitude = 0;       // 경도
    private Point screenPt;
    private LatLng latitudeLongitude;

    private static final int REQUEST_CODE_A = 0x5a5a;
    private static final int REQUEST_CODE_B = 0xa5a5;
    private String getAddress = "address";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        setTitle("Map Album");

        if (mMap == null) {     // create a map if there is no map opened.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) { // if map was created successfully, setup it's type and my location.
                mMap.setMapType(MAP_TYPE_NORMAL);
                mMap.setMyLocationEnabled(true);
            }
        }

        searchBt = (ImageButton) findViewById(R.id.mapview_searchBt);
        searchBt.setVisibility(View.VISIBLE);
        searchBt.setOnClickListener(onNameSearch);

        // httpUtil 생성자
        httpUtil = new GoogleMapUtility();

        errorDialog = new AlertDialog.Builder(this).setTitle("Searching...")
                .setMessage(errorString).setPositiveButton("Close", null)
                .create();

        handler = new Handler(getMainLooper());
        handler.post(findSeoul);                // prepare map ready to search...

        semaphoreLongTouch = false;           // 롱터치가 발생했는지를 알리는 플래그
        semaphoreMapReady = true;             // Map이 준비되었는지 알리는 플래그

        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    // 현재 위도와 경도에서 화면 포인트를 알려줌.
                    getMapPosition(latLng);     // get current position

                    // httpUtil
                    httpUtil = new GoogleMapUtility();
                    httpUtil.requestPointSearch(new ResultHandler(MapAlbumActivity.this),
                            coordinates[0], coordinates[1]);

                    semaphoreLongTouch = true;           // 롱터치가 발생했는지를 알리는 플래그
                }
            });
        }
    }

//        Intent intent = new Intent(getApplicationContext(), GoogleMapUtility.class);
//        intent.putExtra("key", getAddress);
//        intent.putExtra("code", REQUEST_CODE_A);
//        startActivityForResult(intent, REQUEST_CODE_A);
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_A && resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), data.getStringExtra("data"), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void getMapPosition(LatLng latLng) {
        // 현재 위도와 경도에서 화면 포인트를 알려줌.
        screenPt = mMap.getProjection().toScreenLocation(latLng);

        // 현재 화면에 찍힌 포인트로 부터 위도와 경도를 알려줌.
        latitudeLongitude = mMap.getProjection().fromScreenLocation(screenPt);

        coordinates[0] = String.valueOf(latLng.latitude);       // String 위도
        coordinates[1] = String.valueOf(latLng.longitude);      // String 경도
        latitude = Double.parseDouble(coordinates[0]);          // Double 위도
        longitude = Double.parseDouble(coordinates[1]);         // Double 경도
        latitudeLongitude = new LatLng(latitude, longitude);    // LatLng format 위도/경도
        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latitudeLongitude, 15));
    }

    private Runnable findSeoul = new Runnable() {

        @Override
        public void run() {
            if(mMap != null) {
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(DEFAULT_GP, 5f);
                mMap.moveCamera(cu);
            } else {
                handler.postDelayed(findSeoul, 300);
            }
        }
    };

    protected void onStop() {
        handler.removeCallbacks(findSeoul);
        super.onStop();
    };

    // when the search button pressed, display a dialog and get the address input:
    private View.OnClickListener onNameSearch = new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {

            if (semaphoreMapReady == true) {    // Map에서 검색을 받을 준비가 되었는지?
                final LinearLayout linear = (LinearLayout) View.inflate(MapAlbumActivity.this, R.layout.dialog_map_namesearch, null);
                TextView addrTv = (TextView) linear.findViewById(R.id.dialog_map_search_addr);
                Location lo = mMap.getMyLocation();                             // 내 위치(주소)를 화면에 표시해 줌
                addrTv.setText(addresReformation(lo.getLatitude(), lo.getLongitude())); // 대한민국 제외한 축약된 주소로 표시

                new AlertDialog.Builder(MapAlbumActivity.this).setTitle("Please type address to go.")
                        .setView(linear).setPositiveButton("Continue", onClickNameSearch)
                                .setNegativeButton("Abort", null).show();
            }
        }
    };

    private DialogInterface.OnClickListener onClickNameSearch = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (semaphoreMapReady == true) {    // Map에서 검색을 받을 준비가 되었는지?
                AlertDialog ad = (AlertDialog) dialog;
                EditText nameEt = (EditText) ad.findViewById(R.id.dialog_map_search_et);
                TextView addrTv = (TextView) ad.findViewById(R.id.dialog_map_search_addr);

                if (nameEt.getText().length() > 0) {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        return;     // if the dialog box showing, the just return.
                    }

                    progressDialog = ProgressDialog.show(MapAlbumActivity.this, "Wait", "Please wait for a moments...");

                    httpUtil.requestMapSearch(new ResultHandler(MapAlbumActivity.this),
                            nameEt.getText().toString(), addrTv.getText().toString());

                    final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(nameEt.getWindowToken(), 0);
                }
            }
        }
    };

    static class ResultHandler extends Handler {
        private final WeakReference<MapAlbumActivity> mActivity;

        ResultHandler(MapAlbumActivity activity) {
            mActivity = new WeakReference<MapAlbumActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MapAlbumActivity activity = mActivity.get();
            if(activity != null) {
                activity.handleMessage(msg);
            }
        }
    }

    private void handleMessage(Message msg) {
        progressDialog.dismiss();

        String result = msg.getData().getString(GoogleMapUtility.RESULT);
        ArrayList<String> searchList = new ArrayList<String>();

        if (result.equals(GoogleMapUtility.SUCCESS_RESULT)) {
            searchList = msg.getData().getStringArrayList("searchList");

        } else if (result.equals(GoogleMapUtility.TIMEOUT_RESULT)) {
            errorString = "Timeout Error.";
            errorDialog.setMessage(errorString);
            errorDialog.show();
            return;
        } else if (result.equals(GoogleMapUtility.FAIL_MAP_RESULT)) {
            errorString = "No Map Found.";
            errorDialog.setMessage(errorString);
            errorDialog.show();
            return;
        } else {
            errorString = httpUtil.stringData;
            errorDialog.setMessage(errorString);
            errorDialog.show();
            return;
        }

        String[] searches = searchList.toArray(new String[searchList.size()]);
        adjustToPoints(searches);       // 검색한 곳에 마크를 표시함.

        if (semaphoreLongTouch == true) {
            String resultAddress = httpUtil.getAddress();
            Toast.makeText(this, resultAddress, Toast.LENGTH_LONG).show();
            semaphoreLongTouch = false;         // 플래그를 끄고, 롱터치가 발생하면 다시 켬

            if (flagFinish == true) {       // finish 플래그가 세팅되어 있으면 APP을 종료함.
                Intent intent = new Intent();               // intent 생성
                intent.putExtra("data", resultAddress);     // value에 결과값(주소) 리턴
                setResult(RESULT_OK, intent);               // status = OK
                finish();                   // 다른 앱에 의해 불려졌을 경우에만 종료해야 함.
            }
        }
    }


    protected void adjustToPoints(String[] results) {

        mMap.clear();

        int length = Integer.valueOf(results.length / 3);
        LatLng[] mPoints = new LatLng[length];

        for (int i = 0; i < length; i++) {
            LatLng latlng = new LatLng(
                    Float.valueOf(results[i * 3 + 1]),      // 위도
                    Float.valueOf(results[i * 3 + 2]));     // 경도
            mMap.addMarker(new MarkerOptions()
                    .position(latlng)                       // 마킹할 위치
                    .title(results[i * 3])
                    .icon(BitmapDescriptorFactory.defaultMarker(i * 360 / length)));

            mPoints[i] = latlng;                            // 검색한 장소를 저장
        }

        for (LatLng ll : mPoints) {

            // (위도,경도)=(0,0)는 검색하지 않는다고 가정함.
            // (0,0)을 배제함으로써 범위를 검색할 값이 있는지 여부도 알 수 있음.

            if (ll.latitude != 0 && ll.longitude != 0) {
                minLatitude = (minLatitude > ll.latitude) ? ll.latitude : minLatitude;
                maxLatitude = (maxLatitude < ll.latitude) ? ll.latitude : maxLatitude;
                minLongitude = (minLongitude > ll.longitude) ? ll.longitude	: minLongitude;
                maxLongitude = (maxLongitude < ll.longitude) ? ll.longitude	: maxLongitude;
            }
        }

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(new LatLngBounds(new LatLng(minLatitude, minLongitude),
                                                                               new LatLng(maxLatitude, maxLongitude)), 4);
        mMap.animateCamera(cu);     // 카메라를 움직여서 검색한 위치를 포커스함.
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mMap == null) {     // create a map if there is no map opened.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) { // if map was created successfully, setup it's type and my location.
                mMap.setMapType(MAP_TYPE_NORMAL);
                mMap.setMyLocationEnabled(true);        // now we are prepared to search address
            }
        }
    }

    private String addresReformation(double lat, double lng) {
        Geocoder gcK = new Geocoder(getApplicationContext(), Locale.KOREA);
        String res = "";        // prepare result string (address)
        try {
            List<Address> addresses = gcK.getFromLocation(lat, lng, 1);
            StringBuilder sb = new StringBuilder();

            if (null != addresses && addresses.size() > 0) {
                Address address = addresses.get(0);
                // sb.append(address.getPostalCode()).append(" ");
                // sb.append(address.getCountryName()).append(" ");

                sb.append(address.getLocality()).append(" ");
                // sb.append(address.getPremises()).append(" ");
                // sb.append(address.getSubAdminArea()).append(" ");
                sb.append(address.getSubLocality()).append(" ");
                sb.append(address.getThoroughfare()).append(" ");
                sb.append(address.getFeatureName());
                res = sb.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
