package com.vfs.augmented.bluetooth.packet;

import java.io.Serializable;

/**
 * Created by andreia on 18/08/15.
 */
public class Packet implements Serializable
{
    public int      code = -1;
    public String   value = "";
    public int[]    sequence = null;

    public Packet()
    {

    }

    public Packet(int code, String value)
    {
        this.code   = code;
        this.value  = value;
    }
/*
    public Packet(int[] sequence)
    {
        this.code = PacketCodes.PLAYER_MOVE_SEQUENCE;
        this.sequence = sequence;
    }
    */
}