package com.vfs.augmented.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.vfs.augmented.BluetoothApplication;
import com.vfs.augmented.R;


public class LoginActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
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
        Intent intent = new Intent(LoginActivity.this,ConnectActivity.class);
        startActivity(intent);
        this.finish();
    }
}
