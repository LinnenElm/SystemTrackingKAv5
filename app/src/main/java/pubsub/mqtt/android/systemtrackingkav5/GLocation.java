package pubsub.mqtt.android.systemtrackingkav5;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import javax.microedition.khronos.opengles.GL;

public class GLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker now;
//    public double longitude, latitude;
    private LatLng currentLocation;
    public static final String TAG = "DATABASE_LOG";
    public static final String JSON = "JSON_UNPACK_LOG";
    public static final String LONGLAT_ID = "pubsub.mqtt.android.longlat";
    private StatusUpdateReceiver statusUpdateIntentReceiver;
    private MQTTMessageReceiver messageIntentReceiver;

    private MyDatabaseHelper dbHelper;

    private String UNIT_TABLE="unit"; // name of table

    private double UNIT_LONGITUDE = 0.0;
    private double UNIT_LATITUDE = 0.0;
    private long UNIT_TIMESENT = 12345678910L;
    private long UNIT_TIMERECV = 12345678910L;

    Thread jalan;
    JSONObject jObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        UNIT_LONGITUDE = getIntent().getExtras().getDouble("longitude");
        UNIT_LATITUDE = getIntent().getExtras().getDouble("latitude");
        currentLocation = new LatLng(UNIT_LATITUDE, UNIT_LONGITUDE);

        //updateMarker();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public GLocation(){
        dbHelper = new MyDatabaseHelper(this);
        Log.e(TAG, "Creation phase is completed.");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        now = mMap.addMarker(new MarkerOptions().position(currentLocation).title("Waiting for unit info ..."));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(7));

    }

    public void updateMarker(){
       jalan =  new Thread(new Runnable() {
            public void run(){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        now.setPosition(new LatLng(UNIT_LATITUDE, UNIT_LONGITUDE));
                    }
                });

            }
        });
        jalan.start();
    }

    public void updateLonglat(double longitude, double latitude){
        this.UNIT_LONGITUDE = longitude;
        this.UNIT_LATITUDE = latitude;
    }

    public class StatusUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            Bundle notificationData = intent.getExtras();
//            String newStatus;
//
//            if(getIntent().getExtras().getBoolean("onSubscribe") == true){
//                newStatus = notificationData.getString(MQTTServiceSubscribe.MQTT_STATUS_MSG);
//            }else{
//                newStatus = notificationData.getString(MQTTServicePublish.MQTT_STATUS_MSG);
//            }
//
//            Toast.makeText(getApplicationContext(), newStatus,
//                    Toast.LENGTH_LONG).show();
        }
    }

    public class MQTTMessageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle notificationData = intent.getExtras();
            String newData;
            if(getIntent().getExtras().getBoolean("onSubscribe") == true){
                newData  = notificationData.getString(MQTTServiceSubscribe.MQTT_MSG_RECEIVED_MSG);
            }else{
                newData  = notificationData.getString(MQTTServicePublish.MQTT_MSG_RECEIVED_MSG);
            }

            try {
                jObject = new JSONObject(newData);
                UNIT_LONGITUDE = jObject.getDouble("longitude");
                UNIT_LATITUDE = jObject.getDouble("latitude");
                UNIT_TIMERECV = (new Date()).getTime();
                UNIT_TIMESENT =  jObject.getLong("timeStamp");
                UNIT_TABLE = jObject.getString("topiName");

                //value check
                Log.e(JSON, "LONGITUDE: "+UNIT_LONGITUDE+" LATITUDE: "+UNIT_LATITUDE+" TIMERECV: "+UNIT_TIMERECV+" TIMESENT: "+UNIT_TIMESENT);

                //write to sqlite database
                dbHelper.createRecords(UNIT_TABLE, UNIT_LONGITUDE, UNIT_LATITUDE, UNIT_TIMESENT, UNIT_TIMERECV);

                //update and zoom in marker
                currentLocation = new LatLng(UNIT_LATITUDE, UNIT_LONGITUDE);
                now.setTitle(UNIT_TABLE);
                now.setPosition(currentLocation);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 12.0f));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        statusUpdateIntentReceiver = new StatusUpdateReceiver();
        IntentFilter intentSFilter = new IntentFilter(MQTTServicePublish.MQTT_STATUS_INTENT);
        registerReceiver(statusUpdateIntentReceiver, intentSFilter);

        messageIntentReceiver = new MQTTMessageReceiver();
        IntentFilter intentCFilter = new IntentFilter(MQTTServicePublish.MQTT_MSG_RECEIVED_INTENT);
        registerReceiver(messageIntentReceiver, intentCFilter);
    }

    @Override
    public void onBackPressed() {

        Intent data = new Intent();
        data.putExtra("unsubscribe", true);
        setResult(RESULT_OK, data);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(statusUpdateIntentReceiver);
        unregisterReceiver(messageIntentReceiver);
    }


}
