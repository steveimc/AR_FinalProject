package com.vfs.augmented.bluetooth.interfaces;

/**
 * Created by andreia on 17/08/15.
 * Must be implemented by classes that want to be
 * notified of successful bluetooth connection.
 */
public interface BTCConnectionCallback
{
    public void onConnectedSuccessfully(String connectedDeviceName);
}
