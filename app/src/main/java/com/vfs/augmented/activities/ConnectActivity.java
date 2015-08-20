package com.vfs.augmented.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vfs.augmented.BluetoothApplication;
import com.vfs.augmented.R;
import com.vfs.augmented.UserInterfaceUtil;
import com.vfs.augmented.bluetooth.interfaces.BTCConnectionCallback;
import com.vfs.augmented.bluetooth.interfaces.BTCReceiver;
import com.vfs.augmented.bluetooth.BluetoothController;
import com.vfs.augmented.bluetooth.DeviceListActivity;
import com.vfs.augmented.bluetooth.packet.Packet;
import com.vfs.augmented.bluetooth.packet.PacketCodes;

/**
 * Created by andreia on 17/08/15.
 * Allows user to connect to other another device or choose Single Player Mode
 * Multiplayer is the default
 */
public class ConnectActivity extends Activity implements BTCReceiver, BTCConnectionCallback
{
    BluetoothController _btController;
    boolean             _thisPlayerInvited = false;

    // Used to tell next activity if user chose singleplayer
    public static final String SINGLE_PLAYER = "SinglePlayer";
    private boolean     _isSinglePlayer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_activity);

        // Get reference from application and initialize BluetoothController object
        ((BluetoothApplication)this.getApplicationContext())._bluetoothController = new BluetoothController(this, this);
        _btController = ((BluetoothApplication)this.getApplicationContext())._bluetoothController;
        _btController.onActivityStart();
        _btController.addConnectionCallback(this);

        // Ensure that bluetooth is discoverable before user tries to scan devices
        _btController.ensureDiscoverable();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        _btController.onActivityResume();
    }


    @Override
    public void receivePacket(Packet packet)
    {
        switch (packet.code)
        {
            // If the other player accepted to fight, move to next activity
            // otherwise disconnect
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
            // When a connection is made we receive the name from other player
            // and ask the user if he accepts to fight
            case PacketCodes.PLAYER_NAME:
                askIfUserAcceptsBattle(packet.value);
                break;
        }
    }

    @Override
    public void onConnectedSuccessfully(String connectedDeviceName)
    {
        if(_thisPlayerInvited)
        {
            // When a connection is made
            // This player name will be sent to the connected player, in order to prompt for fight
            _btController.sendPacket(new Packet(PacketCodes.PLAYER_NAME, ((BluetoothApplication) this.getApplicationContext())._username));
        }
    }

    // Show a dialog that allows user to deny battle with the connected opponent
    // Accepting will move both to SelectActivity
    // Otherwise disconnects them
    private void askIfUserAcceptsBattle(String name)
    {
        final Dialog acceptFightDialog = new Dialog(this);
        acceptFightDialog.setContentView(R.layout.connect_dialog);
        acceptFightDialog.setTitle("FIGHT!");

        // set the custom dialog components - text, image and button
        TextView text = (TextView)      acceptFightDialog.findViewById(R.id.text);
        text.setText(name + " wants to fight!");

        Button dialogOK = (Button) acceptFightDialog.findViewById(R.id.dialog_button_accept);
        Button dialogNO = (Button) acceptFightDialog.findViewById(R.id.dialog_button_deny);
        // if button is clicked, close the custom dialog
        dialogOK.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserInterfaceUtil.showSkullButtonClick(ConnectActivity.this, v);
                _btController.sendPacket(new Packet(PacketCodes.FIGHT_PROMPT, PacketCodes.YES));
                acceptFightDialog.dismiss();
                goToSelectActivity();
            }
        });

        dialogNO.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserInterfaceUtil.showSkullButtonClick(ConnectActivity.this, v);
                _btController.sendPacket(new Packet(PacketCodes.FIGHT_PROMPT, PacketCodes.NO));
                _btController.stopConnection();
                acceptFightDialog.dismiss();
            }
        });

        acceptFightDialog.show();
    }

    // Open a list to search and choose oponent
    public void onFindPlayers(View view)
    {
        UserInterfaceUtil.showSkullButtonClick(this, view);
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, BluetoothController.REQUEST_CONNECT_DEVICE_SECURE);
    }

    public void onSinglePlayer(View view)
    {
        UserInterfaceUtil.showSkullButtonClick(this, view);
        _isSinglePlayer = true;
        goToSelectActivity();
    }

    private void goToSelectActivity()
    {
        final Intent mainIntent = new Intent(ConnectActivity.this, SelectActivity.class);
        mainIntent.putExtra(SINGLE_PLAYER, _isSinglePlayer);
        ConnectActivity.this.startActivity(mainIntent);
        ConnectActivity.this.finish();
    }


    // When user chooses to find nearby players, it opens a DeviceListActivity
    // which enables him do pick one. When its chosen, the result is caugth by this function
    // which actually connects users.
    // It tags this user as the one INVITING
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
                    _btController.setupCommunication();
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
