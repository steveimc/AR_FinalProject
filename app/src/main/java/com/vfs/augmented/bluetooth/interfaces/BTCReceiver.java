package com.vfs.augmented.bluetooth.interfaces;
import com.vfs.augmented.bluetooth.packet.Packet;
/**
 * Created by andreia on 17/08/15.
 * Must be implemented by classes that want to be
 * receive packets from bluetooth connection.
 */
public interface BTCReceiver
{
    // Called everytime BluetoothController receives data from the current connection
    public void receivePacket(Packet packet);
}
