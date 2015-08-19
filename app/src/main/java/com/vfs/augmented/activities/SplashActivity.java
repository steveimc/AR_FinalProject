package com.vfs.augmented.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.vfs.augmented.BluetoothApplication;
import com.vfs.augmented.R;


public class SplashActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);



/*
        Thread timerThread = new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(3000);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    Intent intent = new Intent(SplashActivity.this,ConnectActivity.class);
                    startActivity(intent);
                }
            }
        };
        timerThread.start();*/
    }

    public void onStartButton(View view)
    {
        EditText editText = (EditText) findViewById(R.id.splash_edit_text);
        String username = editText.getText().toString();
        if(username.length() > 1)
            ((BluetoothApplication)this.getApplicationContext())._username = username;

        goToConnectActivity();
    }

    void goToConnectActivity()
    {
        Intent intent = new Intent(SplashActivity.this,ConnectActivity.class);
        startActivity(intent);
        this.finish();
    }
}
