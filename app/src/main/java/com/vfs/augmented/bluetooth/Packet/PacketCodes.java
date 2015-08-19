package com.vfs.augmented.bluetooth.packet;

/**
 * Created by andreia on 18/08/15.
 */
public class PacketCodes
{
    public static final int PACKET_CODE_NULL    = -1;

    public static final int PLAYER_NAME         = 1;

    public static final int FIGHT_PROMPT        = 2;
    public static final String YES              = "y";
    public static final String NO               = "n";

    public static final int PICK_MONSTER        = 3;
    public static final String MONSTER1         = "1";
    public static final String MONSTER2         = "2";

    public static final int PLAYER_IS_READY     = 4;

    public static final int PLAYER_MOVE         = 5;
    public static final String MOVE_ATTACK      = "a";
    public static final String MOVE_DEFEND      = "d";
    public static final String MOVE_SPECIAL     = "s";

    public static final int PLAYER_MOVE_SEQUENCE    = 6;

    public static final int PLAYER_IS_TRACKING  = 7;


}
