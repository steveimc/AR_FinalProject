package com.vfs.augmented.bluetooth.packet;

import java.io.Serializable;

/**
 * Created by andreia on 18/08/15.
 *
 * Model Object that is passed through the bluetooth connection
 */
public class Packet implements Serializable
{
    public int      code = -1;
    public String   value = "";

    //public Packet(){};

    public Packet(int code, String value)
    {
        this.code   = code;
        this.value  = value;
    }
}