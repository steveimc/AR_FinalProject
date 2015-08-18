package com.vfs.augmented.activities;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.vfs.augmented.BluetoothApplication;
import com.vfs.augmented.R;
import com.vfs.augmented.bluetooth.BTCReceiver;
import com.vfs.augmented.bluetooth.BluetoothController;

public class SelectActivity extends ActionBarActivity implements BTCReceiver
{
    BluetoothController _btController;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_activity);

        _btController = ((BluetoothApplication)this.getApplicationContext())._bluetoothController;
        _btController.changeActivity(this, this);
    }

    @Override
    public void receiveMsg(String msg)
    {

    }

    private void sendData(String msg)
    {
        
    }

    public void onMonster1(View view)
    {

    }

    public void onMonster2(View view)
    {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select, menu);
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


}
