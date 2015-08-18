package com.vfs.augmented.bluetooth.interfaces;

import com.vfs.augmented.bluetooth.packet.Packet;
/**
 * Created by andreia on 17/08/15.
 */
public interface BTCReceiver
{
    public void receivePacket(Packet packet);
}
