package com.vfs.augmented.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.vfs.augmented.R;


public class SplashActivity extends ActionBarActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

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
        timerThread.start();
    }
}
