package com.vfs.augmented.bluetooth.packet;

/**
 * Created by andreia on 18/08/15.
 *
 * Codes and predefined packet messages used to communicate through bluetooth
 */
public class PacketCodes
{
    public static final int PACKET_CODE_NULL    = -1;   // Used to check if code is valid

    public static final int PLAYER_NAME         = 1;    // Flags that the packet value is enemy name

    public static final int FIGHT_PROMPT        = 2;    // Flags that the packet value is the answer to the fight prompt

    public static final String YES              = "y";  // TRUE
    public static final String NO               = "n";  // FALSE

    public static final int PICK_MONSTER        = 3;    // Flags that the packet value is the monster picked by player
    public static final String MONSTER1         = "1";  // MonsterType.MONSTER_1
    public static final String MONSTER2         = "2";  // MonsterType.MONSTER_2

    public static final int PLAYER_IS_READY     = 4;    // Flags that player is already in gameActivity, so game may start

    public static final int PLAYER_MOVE         = 5;    // Flags that the packet value is a game move
    public static final String MOVE_ATTACK      = "a";  // Moves.ATTACK;
    public static final String MOVE_DEFEND      = "d";  // Moves.DEFEND;
    public static final String MOVE_MAGIC       = "s";  // Moves.MAGIC;

    public static final int PLAYER_IS_TRACKING  = 6;    // Flags that player is tracking AR


}
