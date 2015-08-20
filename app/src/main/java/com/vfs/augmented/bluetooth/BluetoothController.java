package com.vfs.augmented.bluetooth;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.vfs.augmented.BluetoothApplication;
import com.vfs.augmented.bluetooth.interfaces.BTCConnectionCallback;
import com.vfs.augmented.bluetooth.interfaces.BTCReceiver;
import com.vfs.augmented.bluetooth.packet.Packet;
import com.vfs.augmented.bluetooth.packet.PacketCodes;
import com.vfs.augmented.bluetooth.packet.PacketSerializer;

/**
 * Created by andreia on 17/08/15.
 * BluetoothController controls bluetooth to communicate with other devices through Packets
 * - adapted from BluetoothChatFragment of the BluetoothChat Sample from Android
 */
public class BluetoothController
{
    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE_SECURE   = 1;
    public static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    public static final int REQUEST_ENABLE_BT = 3;

    private String                  mConnectedDeviceName = null;
    private StringBuffer            mOutStringBuffer;
    private BluetoothAdapter        mBluetoothAdapter = null;

    private BluetoothChatService    mChatService = null;

    BTCReceiver                     _btcReceiver;
    BTCConnectionCallback           _connectionCallback;
    Activity                        _currentActivity;       // bluetoothController persists through activities

    public BluetoothController(Activity activity, BTCReceiver receiver)
    {
        _currentActivity    = activity;
        _btcReceiver        = receiver;
        mBluetoothAdapter   = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null)
        {
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }
    }

    // Update activity we're in, and the new receiver
    public void changeActivity (Activity newAct, BTCReceiver newReceiver)
    {
        _currentActivity = newAct;
        _btcReceiver = newReceiver;
    }

    // Is only used in the ConnectActivity
    public void addConnectionCallback(BTCConnectionCallback callback)
    {
        _connectionCallback = callback;
    }

    public void onActivityStart()
    {
        if (!mBluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            _currentActivity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }
        else if (mChatService == null)
        {
            setupCommunication();
        }
    }

    public void onActivityResume()
    {
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    public void setupCommunication()
    {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(_currentActivity.getApplicationContext(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    // Make sure device bluetooth is discoverable
    public void ensureDiscoverable()
    {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
        {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            _currentActivity.startActivity(discoverableIntent);
        }
    }

    // Sends a packet object to the connected device
    public void sendPacket(Packet packetToSend)
    {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED)
        {
            Toast.makeText(_currentActivity, "not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (packetToSend != null)
        {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = PacketSerializer.serialize(packetToSend);
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    private void setStatus(CharSequence subTitle)
    {
        Activity activity = _currentActivity;
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the Chat sErvice
     */
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1)
                    {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus("Connected to" + mConnectedDeviceName);
                            _connectionCallback.onConnectedSuccessfully(mConnectedDeviceName);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus("connection");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus("not connected");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a packet from the valid bytes in the buffer
                    Packet receivedPacket = PacketSerializer.deserialize(readBuf);
                    if(receivedPacket.code == PacketCodes.PLAYER_IS_READY)
                    {
                        // This is stored in application because we dont knoe in which activity we might receive it
                        ((BluetoothApplication) _currentActivity.getApplicationContext())._enemyIsInGameActivity = true;
                    }
                    _btcReceiver.receivePacket(receivedPacket);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != _currentActivity) {
                        Toast.makeText(_currentActivity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != _currentActivity)
                    {
                        Toast.makeText(_currentActivity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void stopConnection()
    {
        mChatService.stop();
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link android.content.Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public void connectDevice(Intent data, boolean secure)
    {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

}
