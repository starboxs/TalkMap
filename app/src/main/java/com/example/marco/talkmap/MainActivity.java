package com.example.marco.talkmap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.games.internal.request.RequestUpdateOutcomes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
    public static final int LOCATION_UPDATE_MIN_TIME = 5000;
    private SQLite db;
    private ListView lv_msg;
    private ArrayList<Obj_Marker> data = new ArrayList<Obj_Marker>();
    private ArrayList<Obj_Msg> data_msg = new ArrayList<Obj_Msg>();
    private Button button, btn_msg;
    private EditText editText, et_msg;
    private DatabaseReference mDatabase;
    private LinearLayout linear_msg;
    private Bitmap pic = null;
    private String androidId = "";
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private Adapter_msg am;
    private ImageView msg_close;
    private MapView mMapView;
    private String bestProvider = LocationManager.GPS_PROVIDER;
    private GoogleMap mGoogleMap;
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            double now_lat = location.getLatitude();
            double now_lng = location.getLongitude();

            Firebase_Write(null, null, now_lat + "", now_lng + "", null, null, null);

            //  drawMarker(location, pic);
            // System.out.println("追蹤現在座標：" + now_lng + "  " + now_lat);

        }
    };
    Firebase ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SimpleDateFormat sdf = new SimpleDateFormat("MM 月 dd 日 HH 時 mm 分 ss 秒");
        System.out.println("===========================(校正)" + sdf.format(new Date()) + "(校正)===========================");
        //不關閉螢幕
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        db = new SQLite(this);
        //  db.insert_msg("marco","go","11:11");

         db.delete_msg();

        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_main);
        callbackManager = CallbackManager.Factory.create();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        MapsInitializer.initialize(getApplicationContext());
        Firebase.setAndroidContext(this);
        ref = new Firebase("https://talkmap-6c910.firebaseio.com/");
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Firebase_read();


        //取得系統定位服務
        LocationManager status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
        if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
            System.out.println("已取得系統服務");
            getService = true;

            //    locationServiceInitial();
        } else {
            Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
            // startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//開啟設定頁面
        }

        System.out.println("權限:" + accessToken);
        AppEventsLogger.activateApp(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mMapView = (MapView) findViewById(R.id.mapview);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mMapView.onCreate(savedInstanceState);
        mGoogleMap = mMapView.getMap();
        key();
        initMap();
        facebook();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Firebase_Write(null, "true", null, null, null, null, null);
        lv_msg = (ListView) findViewById(R.id.lv_msg);
        msg_close =(ImageView)findViewById(R.id.msg_close);
        am = new Adapter_msg(this);
        linear_msg = (LinearLayout)findViewById(R.id.linear_msg);
        lv_msg.setAdapter(am);
        editText = (EditText) findViewById(R.id.editText);
        et_msg = (EditText) findViewById(R.id.et_msg);
        button = (Button) findViewById(R.id.button);
        btn_msg = (Button) findViewById(R.id.btn_msg);
        btn_msg.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                SimpleDateFormat sdf = new SimpleDateFormat("MM:dd HH:mm:ss");


                Firebase_Write(null, null, null, null, null, et_msg.getText().toString(), sdf.format(new Date()));
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Firebase_Write(editText.getText().toString(), null, null, null, null, null, null);
            }
        });
        msg_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(msg_close_boolean)
                {
                    linear_msg.setVisibility(View.INVISIBLE);
                    msg_close_boolean = false;
                }
                else
                {
                    linear_msg.setVisibility(View.VISIBLE);
                    msg_close_boolean = true;
                }


            }
        });
    }
boolean msg_close_boolean = true;
    public void Firebase_Write(String name, String online, String lat, String lon, String image, String msg, String time) {
      //  System.out.println("uuid:" + UUID.randomUUID().toString());
        //team           //uuid                         //屬性            //變數

        if (name != null) {
            ref.child("TalkMap").child("Marco").child(androidId).child("name").setValue(name);
        }
        if (online != null) {
            ref.child("TalkMap").child("Marco").child(androidId).child("online").setValue(online);
        }
        if (lat != null) {
            ref.child("TalkMap").child("Marco").child(androidId).child("lat").setValue(lat);
        }
        if (lon != null) {
            ref.child("TalkMap").child("Marco").child(androidId).child("lon").setValue(lon);
        }
        if (image != null) {
            ref.child("TalkMap").child("Marco").child(androidId).child("image").setValue(image);
        }
        if (msg != null) {
            ref.child("TalkMap").child("Marco").child(androidId).child("Msg").setValue(msg);
        }
        if (time != null) {
            ref.child("TalkMap").child("Marco").child(androidId).child("time").setValue(time);
        }

    }

    public void Firebase_read() {
        System.out.println("資料庫");

        ref.child("TalkMap").child("Marco").addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("收到Firebase訊息："+dataSnapshot.toString());
                System.out.println("");
                if (data.size() == 0) {
                    for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {


                        Obj_Marker om = new Obj_Marker();

                        System.out.println("收到Firebase訊息開始初始化data無資料");
//                        System.out.println("收到Firebase訊息0:" + chatSnapshot.getKey());
                        om.setPhoneid(chatSnapshot.getKey());
//                        System.out.println("收到Firebase訊息1:" + (String) chatSnapshot.child("name").getValue());
                        om.setName((String) chatSnapshot.child("name").getValue());
//                        System.out.println("收到Firebase訊息2:" + (String) chatSnapshot.child("image").getValue());
                        om.setImage((String) chatSnapshot.child("image").getValue());
//                        System.out.println("收到Firebase訊息3:" + (String) chatSnapshot.child("lat").getValue());
                        om.setLat((String) chatSnapshot.child("lat").getValue());
//                        System.out.println("收到Firebase訊息4:" + (String) chatSnapshot.child("lon").getValue());
                        om.setLon((String) chatSnapshot.child("lon").getValue());
//                        System.out.println("收到Firebase訊息5:" + (String) chatSnapshot.child("Msg").getValue());
                        om.setMsg((String) chatSnapshot.child("Msg").getValue());
//                        System.out.println("收到Firebase訊息6:" + (String) chatSnapshot.child("online").getValue());
//                        om.setOnline((String) chatSnapshot.child("online").getValue());
                        db.insert_msg((String)chatSnapshot.child("name").getValue() ,(String)chatSnapshot.child("Msg").getValue() ,(String)chatSnapshot.child("time").getValue());

                        System.out.println("收到Firebase訊息結束初始化data無資料");
                        data.add(om);
                        System.out.println("收到Firebase訊息結束初始化data無資料 數量：" + data.size());
                    }
                    drawMarker(null, pic);

                } else {
                    for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {

                        if (((String) chatSnapshot.child("online").getValue()).equals("false")) {
                            for (int i = 0; i < data.size(); i++) {
                                if (data.get(i).getPhoneid() == chatSnapshot.getKey()) {
                                    System.out.println("收到Firebase訊息開始有人離線");
                                    System.out.println("收到Firebase訊息開始有人離線移除" + data.get(i).getPhoneid());
                                    data.remove(i);
                                }

                            }

                        } else {
                            System.out.println("收到Firebase訊息1111111111111:" + chatSnapshot.getKey());
                            for (int i = 0; i < data.size(); i++) {
                                //  System.out.println("收到Firebase訊息2222222222222:"+data.get(i).getPhoneid());
                                if ((data.get(i).getPhoneid()).equals(chatSnapshot.getKey())) {

                                    System.out.println("收到Firebase訊息333333333333:" + chatSnapshot.getKey());
                                    data.get(i).setPhoneid(chatSnapshot.getKey());
//                                  System.out.println("收到Firebase訊息1:" + (String) chatSnapshot.child("name").getValue());
                                    data.get(i).setName((String) chatSnapshot.child("name").getValue());
//                                  System.out.println("收到Firebase訊息2:" + (String) chatSnapshot.child("image").getValue());
                                    data.get(i).setImage((String) chatSnapshot.child("image").getValue());
//                                  System.out.println("收到Firebase訊息3:" + (String) chatSnapshot.child("lat").getValue());
                                    data.get(i).setLat((String) chatSnapshot.child("lat").getValue());
//                                  System.out.println("收到Firebase訊息4:" + (String) chatSnapshot.child("lon").getValue());
                                    data.get(i).setLon((String) chatSnapshot.child("lon").getValue());
                                    //System.out.println("收到Firebase訊息5:" + (String) chatSnapshot.child("Msg").getValue());
                                    data.get(i).setMsg((String) chatSnapshot.child("Msg").getValue());
                                    //System.out.println("收到Firebase訊息6:" + (String) chatSnapshot.child("online").getValue());
                                    data.get(i).setOnline((String) chatSnapshot.child("online").getValue());


                                    db.insert_msg((String)chatSnapshot.child("name").getValue() ,(String)chatSnapshot.child("Msg").getValue() ,(String)chatSnapshot.child("time").getValue());
                                    data_msg  = db.select_msg();
                                    am.change(data_msg);

                                    System.out.println("收到Firebase訊息結束");
                                }


                            }


                        }


                    }
                    drawMarker(null, pic);

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }

        });
    }


    private LocationManager lms;

    private void locationServiceInitial() {
        lms = (LocationManager) getSystemService(LOCATION_SERVICE);    //取得系統定位服務
        Criteria criteria = new Criteria();  //資訊提供者選取標準
        bestProvider = lms.getBestProvider(criteria, true);    //選擇精準度最高的提供者
        Location location = lms.getLastKnownLocation(bestProvider);
        getLocation(location);
    }

    private void getLocation(Location location) {    //將定位資訊顯示在畫面中
        if (location != null) {

            Double longitude = location.getLongitude();    //取得經度
            Double latitude = location.getLatitude();    //取得緯度
            //   Toast.makeText(this, "longitude:" + longitude + " latitude" + latitude, Toast.LENGTH_SHORT).show();
            System.out.println("longitude:" + longitude + " latitude" + latitude);


            drawMarker(location, pic);
        } else {
            Toast.makeText(this, "無法定位座標", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Firebase_Write(null, "false", null, null, null, null, null);
        mMapView.onDestroy();
    }

    private boolean getService = false;

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        System.out.println("進入onResume");
        if (getService) {
            System.out.println("進入定位onResume");
//            lms.requestLocationUpdates(bestProvider, 1000, 1, this);
            // lms.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);


            //  getCurrentLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (getService) {
            lms.removeUpdates(this);    //離開頁面時停止更新
        }
    }

    public void facebook() {
        loginButton = (LoginButton) findViewById(R.id.login_button);
        // loginButton.setReadPermissions("email");
        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                AccessToken accessToken = AccessToken.getCurrentAccessToken();

                System.out.println("成功:" + accessToken);

                GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {


                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        System.out.println("id=" + object.optString("id"));
                        System.out.println("email=" + object.optString("email"));
                        System.out.println("last_name=" + object.optString("last_name"));
                        System.out.println("first name=" + object.optString("first_name"));
                        System.out.println("address=" + object.optString("address"));//NO
                        System.out.println("gender=" + object.optString("gender"));
                        System.out.println("birthday=" + object.optString("birthday"));
                        JSONObject data = response.getJSONObject();
                        if (data.has("picture")) {
                            try {
                                String profilePicUrl = data.getJSONObject("picture").getJSONObject("data").getString("url");
                                Firebase_Write(null, null, null, null, profilePicUrl, null, null);
                                final Bitmap[] image = new Bitmap[1];
                                try {
                                    final URL url = new URL(profilePicUrl);

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                image[0] = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                                pic = zoomImage(image[0], 100, 100);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                System.out.println("picture錯誤:" + e);
                                            }
                                        }
                                    }).start();


                                } catch (IOException e) {
                                    System.out.println(e);
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // set profile image to imageview using Picasso or Native methods
                        }

                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday,picture.type(large)");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                System.out.println("清除");
            }

            @Override
            public void onError(FacebookException exception) {
                System.out.println("錯誤");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("onActivityResult");
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void initMap() {
        int googlePlayStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (googlePlayStatus != ConnectionResult.SUCCESS) {
            System.out.println("成功");
            GooglePlayServicesUtil.getErrorDialog(googlePlayStatus, this, -1).show();
            finish();
        } else {
            if (mGoogleMap != null) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("步步成功");
                    return;
                }
                System.out.println("測試");

                mGoogleMap.setOnMyLocationChangeListener(myLocationChangeListener);
                mGoogleMap.getUiSettings().setAllGesturesEnabled(true);

                mGoogleMap.getUiSettings().setMapToolbarEnabled(false);

                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                mGoogleMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
                    @Override
                    public void onInfoWindowClose(Marker marker) {
                        System.out.println("onInfoWindowClose:" + marker.getTitle());


                    }
                });
                mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) { //show 在map 上 沒有框框


                        System.out.println("Marker:" + marker.getTitle());
                        System.out.println("Marker:" + marker.getId());
                        System.out.println("Marker:" + marker.getPosition());

                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {  //有框框
                        View v = getLayoutInflater().inflate(R.layout.infowindows, null);
                        TextView infowindows = (TextView) v.findViewById(R.id.infowindows);


                        for (int i = 0; i < data.size(); i++) {
                            if (data.get(i).getName().equals(marker.getTitle())) {
                                infowindows.setText(data.get(i).getMsg());
                            }
                        }


                        return v;
                    }
                });
                mGoogleMap.setMyLocationEnabled(true);

            }
        }
    }

    public static Bitmap GetBitmapClippedCircle(Bitmap bitmap) {

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle(
                (float) (width / 2)
                , (float) (height / 2)
                , (float) Math.min(width, (height / 2))
                , Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }

    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,
                                   double newHeight) {


        if (bgimage == null) {
            System.out.println("沒照片");

            return null;
        }

        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    Marker marker;
    boolean move = true;

    private void drawMarker(Location location, Bitmap pic) {


        if (mGoogleMap != null) {
            mGoogleMap.clear();
        }

        for (int i = 0; i < data.size(); i++) {
            System.out.println("連線個數：" + data.size());
            BitmapDescriptor icon = null;
            if (data.get(i).getPic() == null) {
                icon = BitmapDescriptorFactory.fromBitmap(zoomImage(BitmapFactory.decodeResource(this.getResources(), R.drawable.title), 100, 100));
            } else {
                icon = BitmapDescriptorFactory.fromBitmap(data.get(i).getPic());

            }


            if (mGoogleMap != null) {
                //
                LatLng gps = new LatLng(Double.parseDouble(data.get(i).getLat()), Double.parseDouble(data.get(i).getLon()));
                marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(gps)
                        .icon(icon)
                        .title(data.get(i).getName()));


                if (move == true) {
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 12));
                    move = false;
                }


            }


        }


//        BitmapDescriptor icon = null;
//        if (pic == null) {
//            icon = BitmapDescriptorFactory.fromBitmap(zoomImage(BitmapFactory.decodeResource(this.getResources(), R.drawable.title), 100, 100));
//        } else {
//            icon = BitmapDescriptorFactory.fromBitmap(GetBitmapClippedCircle(pic));
//        }

//        if (mGoogleMap != null) {
//            mGoogleMap.clear();
//            LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
//            LatLng gps1 = new LatLng(location.getLatitude() - 0.03, location.getLongitude() - 0.003);
//            marker = mGoogleMap.addMarker(new MarkerOptions()
//                    .position(gps)
//                    .icon(icon)
//                    .title("Marco"));
//            //  marker.showInfoWindow();
//            marker = mGoogleMap.addMarker(new MarkerOptions()
//                    .position(gps1)
//                    .icon(BitmapDescriptorFactory.fromBitmap(zoomImage(BitmapFactory.decodeResource(this.getResources(), R.drawable.title), 100, 100)))
//                    .title("Miyasaki"));
//            //  marker.showInfoWindow();
//            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(gps, 12));
//
//
//        }

    }

    public void key() {   //生成Facebook手機key
        PackageInfo info;
        try {
            info = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String KeyResult = new String(Base64.encode(md.digest(), 0));//String something = new String(Base64.encodeBytes(md.digest()));
                //  Log.e("hash key", KeyResult);
                //  Toast.makeText(this, "My FB Key is \n" + KeyResult, Toast.LENGTH_LONG).show();
                System.out.println("MainActivity_key=" + KeyResult);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            System.out.println("MainActivity_name not found=" + e1.toString());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("MainActivity_no such an algorithm=" + e.toString());
        } catch (Exception e) {
            System.out.println("MainActivity_exception=" + e.toString());
        }

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //GPS 定位
    @Override
    public void onLocationChanged(Location location) {
        System.out.println("定位變更");
        getLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
