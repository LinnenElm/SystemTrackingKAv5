package pubsub.mqtt.android.systemtrackingkav5;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.support.v7.app.ActionBar;
import android.widget.EditText;
import android.widget.Toast;


public class HomeScreen extends AppCompatActivity implements View.OnClickListener{

    private Button subscribeButt, publishButt;
    private boolean isOnPublishing = false;
    private ActionBar actionBar;
    private String m_Text = "default server";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_home_screen);

        initiateButtonAndView();

        SharedPreferences settings = getSharedPreferences(MainActivity.APP_Boolean, MODE_PRIVATE);
        this.isOnPublishing = settings.getBoolean("isOnPublishing", false);

        if(this.isOnPublishing == true){
            publishButt.setText("Stop Publish");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                actionSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initiateButtonAndView(){
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(
                Color.parseColor("#373836"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        subscribeButt = (Button) findViewById(R.id.subscribeButt);
        publishButt = (Button) findViewById(R.id.publishButt);

        subscribeButt.setOnClickListener(this);
        publishButt.setOnClickListener(this);
    }

    public void editSubscribePreference(boolean value){
        SharedPreferences settings = getSharedPreferences(MainActivity.APP_Boolean, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("isOnSubscribe", value);
        editor.commit();
    }

    public void editPublishdPreference(boolean value){
        SharedPreferences settings = getSharedPreferences(MainActivity.APP_Boolean, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.putBoolean("isOnPublishing", value);
        editor.commit();
    }

    public void createSharedPreference(String name, String broker){
        SharedPreferences settings = getSharedPreferences(name, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("broker", broker);
        editor.commit();
    }


    public void stopServicePublish(){
        Intent svc = new Intent(this, MQTTServicePublish.class);
        stopService(svc);
    }

    public void actionSettings(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server Host");


        SharedPreferences settings = getSharedPreferences(MainActivity.APP_ID, MODE_PRIVATE);
        m_Text = settings.getString("broker", "astaga");

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint(m_Text);

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                createSharedPreference(MainActivity.APP_ID, m_Text);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.subscribeButt: {
                Intent subscribe = new Intent (HomeScreen.this, MainActivity.class);
                editSubscribePreference(true);
                startActivity(subscribe);

                break;
            }
            case R.id.publishButt: {
                if(this.isOnPublishing==true){
                    stopServicePublish();
                    editPublishdPreference(false);
                    publishButt.setText("Publish Position");

                    SharedPreferences settings = getSharedPreferences(MainActivity.APP_Boolean, MODE_PRIVATE);
                    this.isOnPublishing = settings.getBoolean("isOnPublishing", false);

//                    Toast.makeText(getApplicationContext(), "WEW "+this.isOnPublishing,
//                            Toast.LENGTH_LONG).show();
                } else {
                    Intent publish = new Intent (HomeScreen.this, MainActivity.class);
                    editSubscribePreference(false);
                    startActivity(publish);
                }

                break;
            }
        }
    }
}
