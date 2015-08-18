package com.vfs.augmented.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.vfs.augmented.BluetoothApplication;
import com.vfs.augmented.R;
import com.vfs.augmented.bluetooth.interfaces.BTCConnectionCallback;
import com.vfs.augmented.bluetooth.interfaces.BTCReceiver;
import com.vfs.augmented.bluetooth.BluetoothController;
import com.vfs.augmented.bluetooth.DeviceListActivity;
import com.vfs.augmented.bluetooth.packet.Packet;
import com.vfs.augmented.bluetooth.packet.PacketCodes;

public class ConnectActivity extends ActionBarActivity implements BTCReceiver, BTCConnectionCallback
{
    BluetoothController _btController;
    Dialog              _acceptFightDialog;
    boolean             _thisPlayerInvited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_activity);

        ((BluetoothApplication)this.getApplicationContext())._bluetoothController = new BluetoothController(this, this);
        _btController = ((BluetoothApplication)this.getApplicationContext())._bluetoothController;
        _btController.onActivityStart();
        _btController.addConnectionCallback(this);

        //askIfUserAcceptsBattle();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        _btController.onActivityResume();
    }

    private void goToSelectActivity()
    {
        final Intent mainIntent = new Intent(ConnectActivity.this, SelectActivity.class);
        ConnectActivity.this.startActivity(mainIntent);
        ConnectActivity.this.finish();
    }

    @Override
    public void receivePacket(Packet packet)
    {
        switch (packet.code)
        {
            case PacketCodes.FIGHT_PROMPT:
                if(packet.value.equals(PacketCodes.YES))
                {
                    goToSelectActivity();
                }
                else if (packet.value.equals(PacketCodes.NO))
                {
                    _thisPlayerInvited = false;
                    _btController.stopConnection();
                }

                break;
            case PacketCodes.PLAYER_NAME:
                askIfUserAcceptsBattle(packet.value);
                break;
        }
    }

    @Override
    public void onConnectedSuccessfully(String connectedDeviceName)
    {
        /*if(!_thisPlayerInvited)
        {
            askIfUserAcceptsBattle();
        }*/
        if(_thisPlayerInvited)
        {
            _btController.sendMessage(new Packet(PacketCodes.PLAYER_NAME, "REAL_STAR_LORD"));
        }
    }

    private void askIfUserAcceptsBattle(String name)
    {
        final Dialog acceptFightDialog = new Dialog(this);
        acceptFightDialog.setContentView(R.layout.connect_dialog);
        acceptFightDialog.setTitle("FIGHT!");

        // set the custom dialog components - text, image and button
        TextView text = (TextView)      acceptFightDialog.findViewById(R.id.text);
        text.setText(name + " wants to fight!");

        ImageView image = (ImageView)   acceptFightDialog.findViewById(R.id.image);
        image.setImageResource(R.drawable.ic_flash_on_black_24dp);

        Button dialogOK = (Button) acceptFightDialog.findViewById(R.id.dialog_button_accept);
        Button dialogNO = (Button) acceptFightDialog.findViewById(R.id.dialog_button_deny);
        // if button is clicked, close the custom dialog
        dialogOK.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                acceptFightDialog.dismiss();
                _btController.sendMessage(new Packet(PacketCodes.FIGHT_PROMPT, PacketCodes.YES));
                goToSelectActivity();
            }
        });

        dialogNO.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                acceptFightDialog.dismiss();
                _btController.sendMessage(new Packet(PacketCodes.FIGHT_PROMPT, PacketCodes.NO));
                _btController.stopConnection();
            }
        });

        acceptFightDialog.show();
    }

    public void onEnableDiscoveryButton(View view)
    {
        // Ensure this device is discoverable by others
        _btController.ensureDiscoverable();
    }

    public void onSecureConnectButton(View view)
    {
        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, BluetoothController.REQUEST_CONNECT_DEVICE_SECURE);
    }

    public void onInsecureConnectButton(View view)
    {
        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, BluetoothController.REQUEST_CONNECT_DEVICE_INSECURE);
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
                    _thisPlayerInvited = true;
                }
                break;
            case BluetoothController.REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK)
                {
                    _btController.connectDevice(data, false);
                    _thisPlayerInvited = true;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
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

}
