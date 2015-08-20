package com.vfs.augmented.bluetooth.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by andreia on 18/08/15.
 * Utility class used to serialize/deserialize the packet into/from byte[]
 */
public class PacketSerializer
{
    public static byte[] serialize(Object obj)
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = null;
        try
        {
            o = new ObjectOutputStream(b);
            o.writeObject(obj);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return b.toByteArray();
    }

    public static Packet deserialize(byte[] bytes)
    {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = null;
        Packet packet = null;

        try
        {
            o = new ObjectInputStream(b);
            packet = (Packet)o.readObject();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return packet;
    }
}
