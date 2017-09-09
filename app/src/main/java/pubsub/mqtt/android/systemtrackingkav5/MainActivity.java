package pubsub.mqtt.android.systemtrackingkav5;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String APP_ID = "pubsub.mqtt.android";
    public static final String APP_Boolean = "pubsub.mqtt.android.";
    private static final String REPORT = "REPORT: ";
    public boolean isAlreadyRun = false;
    public boolean isOnSubscribe = false;
    public boolean isNotResponding = false;
    public String preferenceBrokerTopic;
    public String preferenceBrokerHost;
    public double longitude = 0.0;
    public double latitude = 0.0;
    private StatusUpdateReceiver statusUpdateIntentReceiver;
    private MQTTMessageReceiver  messageIntentReceiver;

    private Button buttonSubscribe, buttonPublish, buttonSetting;
    private boolean isOnPublishing = false;

    private String unitKereta[] = {"unit00", "unit01", "unit02", "unit03",
            "unit04", "unit05", "unit06", "unit07", "unit08", "unit09", "unit10"};
    public JSONObject jObject;
    private String m_Text;

    private DraweeController controller;
    private ControllerListener controllerListener;
    private SimpleDraweeView gif;
    private Uri uri_active = Uri.parse("res://pubsub.mqtt.android.systemtrackingkav5/" + R.drawable.circle_link);
    private Uri uri_standby = Uri.parse("res://pubsub.mqtt.android.systemtrackingkav5/" + R.drawable.circle_link_standby);
    private ImageView alert_status_lamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences(APP_Boolean, MODE_PRIVATE);
        isOnSubscribe = settings.getBoolean("isOnSubscribe", false);
        SharedPreferences settings2 = getSharedPreferences(APP_ID, MODE_PRIVATE);
        preferenceBrokerHost = settings2.getString("broker", "ehh it doesn't work?");

        initiateButtonAndView();
        deployReceivers();
    }

    public void initiateButtonAndView(){
        alert_status_lamp = (ImageView) findViewById(R.id.view_alert_status_lamp);
        gif = (SimpleDraweeView) findViewById(R.id.view_button_publish);

        buttonSubscribe = (Button) findViewById(R.id._id_button_subscribe);
        buttonPublish = (Button) findViewById(R.id._id_button_publish);
        buttonSetting = (Button) findViewById(R.id._id_button_setting);

        buttonSubscribe.setOnClickListener(this);
        buttonPublish.setOnClickListener(this);
        buttonSetting.setOnClickListener(this);

        // Listen to Download events
        controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable anim) {
                // Image Loaded
//                Toast.makeText(getApplicationContext(), "Image Loaded: " + id, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                // Failure happened
//                Toast.makeText(getApplicationContext(), "Error loading: " + id, Toast.LENGTH_SHORT).show();
            }
        };

        SharedPreferences settings = getSharedPreferences(MainActivity.APP_Boolean, MODE_PRIVATE);
        this.isOnPublishing = settings.getBoolean("isOnPublishing", false);

        if(this.isOnPublishing==true){
            // Initialize a controller and attach the listener to it.
            controller = Fresco.newDraweeControllerBuilder()
                    .setUri(uri_active)
                    .setControllerListener(controllerListener)
                    .setAutoPlayAnimations(true)
                    .build();

            gif.setController(controller);

            buttonPublish.setBackgroundResource(R.drawable.xml_button_publish_active);
            alert_status_lamp.setImageResource(R.drawable.alert_status_on);
        } else {
            // Initialize a controller and attach the listener to it.
            controller = Fresco.newDraweeControllerBuilder()
                    .setUri(uri_standby)
                    .setControllerListener(controllerListener)
                    .setAutoPlayAnimations(true)
                    .build();

            gif.setController(controller);

            alert_status_lamp.setImageResource(R.drawable.alert_status_off);
        }
    }

    public void updateLonglat(double longitude, double latitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public class StatusUpdateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle notificationData = intent.getExtras();
            String newStatus;

            if(isOnSubscribe == true){
                newStatus = notificationData.getString(MQTTServiceSubscribe.MQTT_STATUS_MSG);
            }else{
                newStatus = notificationData.getString(MQTTServicePublish.MQTT_STATUS_MSG);
            }

//            Toast.makeText(getApplicationContext(), newStatus,
//                    Toast.LENGTH_LONG).show();
        }
    }

    public class MQTTMessageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            if(isOnSubscribe==true){
                Bundle notificationData = intent.getExtras();
                String newTopic = notificationData.getString(MQTTServicePublish.MQTT_MSG_RECEIVED_TOPIC);
                String newData  = notificationData.getString(MQTTServicePublish.MQTT_MSG_RECEIVED_MSG);

                if(isAlreadyRun == false){
                    try {
                        jObject = new JSONObject(newData);
                        double lng = jObject.getDouble("longitude");
                        double ltd = jObject.getDouble("latitude");
                        updateLonglat(lng, ltd);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    Intent onGmap = new Intent (MainActivity.this, GLocation.class);
                    onGmap.putExtra("onSubscribe", isOnSubscribe);
                    onGmap.putExtra("longitude", longitude);
                    onGmap.putExtra("latitude", latitude);
                    startActivityForResult(onGmap,1);
                }
                commitIsAlreadyRun();
                resetIsNotRespondingState();

//                Toast.makeText(getApplicationContext(), "Waiting data to be received ...",
//                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void commitIsAlreadyRun(){
        this.isAlreadyRun = true;
    }
    public void resetIsAlreadyRunState(){
        this.isAlreadyRun = false;
    }
    public void commitIsNotResponding(){
        this.isNotResponding = true;
    }
    public void resetIsNotRespondingState(){
        this.isNotResponding = false;
    }

    public void deployReceivers(){
        if(isOnSubscribe == true){
            onDeployReceivers(MQTTServiceSubscribe.MQTT_STATUS_INTENT, MQTTServiceSubscribe.MQTT_MSG_RECEIVED_INTENT);
        }else{
            onDeployReceivers(MQTTServicePublish.MQTT_STATUS_INTENT, MQTTServicePublish.MQTT_MSG_RECEIVED_INTENT);
        }
    }

    public void onDeployReceivers(String statusIntent, String receivedIntent){
        statusUpdateIntentReceiver = new StatusUpdateReceiver();
        IntentFilter intentSFilter = new IntentFilter(statusIntent);
        registerReceiver(statusUpdateIntentReceiver, intentSFilter);

        messageIntentReceiver = new MQTTMessageReceiver();
        IntentFilter intentCFilter = new IntentFilter(receivedIntent);
        registerReceiver(messageIntentReceiver, intentCFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void startService(String topic){
        if(isOnSubscribe == true){
            if(isNotResponding == true){
                stopServiceSubscribe();
            }
            onSubsribe(topic);
            commitIsNotResponding();
        }else{
            onPublish(topic);
            editPublishPreference(true);
        }
    }

    public void startServicePublish(){
        Intent svc = new Intent(this, MQTTServicePublish.class);
        startService(svc);
    }

    public void startServiceSubscribe(){
        Intent svc = new Intent(this, MQTTServiceSubscribe.class);
        startService(svc);
    }

    public void stopServicePublish(){
        Intent svc = new Intent(this, MQTTServicePublish.class);
        stopService(svc);
    }

    public void stopServiceSubscribe(){
        Intent svc = new Intent(this, MQTTServiceSubscribe.class);
        stopService(svc);
    }

    public void onPublish(String topic){
        preferenceBrokerTopic = topic;
        createSharedPreference(APP_ID, preferenceBrokerHost, preferenceBrokerTopic);

        startServicePublish();
    }

    public void onSubsribe(String topic){
        preferenceBrokerTopic = topic;
        createSharedPreference(APP_ID, preferenceBrokerHost, preferenceBrokerTopic);

        startServiceSubscribe();
    }

    public void createSharedPreference(String name, String broker, String topic){
        SharedPreferences settings = getSharedPreferences(name, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("broker", broker);
        editor.putString("topic", topic);
        editor.commit();
    }

    public void createSharedPreference(String name, String broker){
        SharedPreferences settings = getSharedPreferences(name, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("broker", broker);
        editor.apply();
        preferenceBrokerHost = broker;
        //Log.e(REPORT, "Shared Preference Commited -> "+broker);
    }

    private void SingleChoiceWithRadioButton(String announcer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(announcer);
        builder.setSingleChoiceItems(unitKereta, -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences settings = getSharedPreferences(MainActivity.APP_Boolean, MODE_PRIVATE);
                        isOnSubscribe = settings.getBoolean("isOnSubscribe", false);

                        if(isOnSubscribe ==  true){
                            startService(unitKereta[which].toString());
                        }else{
                            startService(unitKereta[which].toString());

                            buttonPublish.setBackgroundResource(R.drawable.xml_button_publish_active);
                            switchGif(uri_active);
                            alert_status_lamp.setImageResource(R.drawable.alert_status_on);

                            editPublishPreference(true);
                        }

                        dialog.dismiss();
                    }
                });
        builder.setNegativeButton("cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void editPublishPreference(boolean value){
        SharedPreferences settings = getSharedPreferences(MainActivity.APP_Boolean, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.putBoolean("isOnPublishing", value);
        editor.commit();
    }

    public void editSubscribePreference(boolean value){
        SharedPreferences settings = getSharedPreferences(MainActivity.APP_Boolean, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isOnSubscribe", value);
        editor.commit(); // Stop everything and do an immediate save!
        // editor.apply();//Keep going and save when you are not busy - Available only in APIs 9 and above.  This is the preferred way of saving.
    }

    public void switchGif(Uri targetGif){
        controller = Fresco.newDraweeControllerBuilder()
                .setUri(targetGif)
                .setControllerListener(controllerListener)
                .setAutoPlayAnimations(true)
                .build();
        gif.setController(controller);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id._id_button_subscribe: {
                editSubscribePreference(true);
                SingleChoiceWithRadioButton("[Subscribe] Pick a Unit to Show its Location");
                break;
            }
            case R.id._id_button_publish: {
                SharedPreferences settings = getSharedPreferences(MainActivity.APP_Boolean, MODE_PRIVATE);
                this.isOnPublishing = settings.getBoolean("isOnPublishing", false);

                if(this.isOnPublishing==true){
                    stopServicePublish();
                    editPublishPreference(false);

                    buttonPublish.setBackgroundResource(R.drawable.xml_button_publish);
                    switchGif(uri_standby);
                    alert_status_lamp.setImageResource(R.drawable.alert_status_off);
                } else {
                    editSubscribePreference(false);
                    SingleChoiceWithRadioButton("[Publish] Pick a Unit to Broadcast its Location");
                }

                break;
            }
            case R.id._id_button_setting: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Server Host");

                SharedPreferences settings = getSharedPreferences(APP_ID, MODE_PRIVATE);
                this.m_Text = settings.getString("broker", "");

                // Set up the input
                final EditText input = new EditText(this);
                input.setText(m_Text);

                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        createSharedPreference(APP_ID, m_Text);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                if (data.getBooleanExtra("unsubscribe", false)){
                    resetIsAlreadyRunState();
                    resetIsNotRespondingState();
                    stopServiceSubscribe();
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(statusUpdateIntentReceiver);
        unregisterReceiver(messageIntentReceiver);
        stopServiceSubscribe();
    }
}
