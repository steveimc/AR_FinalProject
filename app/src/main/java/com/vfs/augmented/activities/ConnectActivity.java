package com.vfs.augmented.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.vfs.augmented.BluetoothApplication;
import com.vfs.augmented.R;
import com.vfs.augmented.bluetooth.BluetoothController;
import com.vfs.augmented.bluetooth.BluetoothControllerReceiver;
import com.vfs.augmented.bluetooth.DeviceListActivity;

public class ConnectActivity extends ActionBarActivity implements BluetoothControllerReceiver
{
    BluetoothController _btController;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_activity);

        ((BluetoothApplication)this.getApplicationContext())._bluetoothController = new BluetoothController(this, this);
        _btController = ((BluetoothApplication)this.getApplicationContext())._bluetoothController;
        _btController.onActivityStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        _btController.onActivityResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.secure_connect_scan:
            {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, BluetoothController.REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan:
            {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, BluetoothController.REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable:
            {
                // Ensure this device is discoverable by others
                _btController.ensureDiscoverable();
                return true;
            }
        }
        return false;
    }

    @Override
    public void receiveMsg(String msg)
    {
        // Do Stuff
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case BluetoothController.REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK)
                {
                    _btController.connectDevice(data, true);
                }
                break;
            case BluetoothController.REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK)
                {
                    _btController.connectDevice(data, false);
                }
                break;
            case BluetoothController.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK)
                {
                    // Bluetooth is now enabled, so set up a chat session
                    _btController.setupChat();
                }
                else
                {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(this, "not_enabled_leaving",
                            Toast.LENGTH_SHORT).show();
                }
        }
    }
}
