package com.vfs.augmented.game;

/**
 * Created by steveimc on 8/17/15.
 */
public class Abilities
{
    private static int BASIC_DAMAGE = 100;
    private static int SPECIAL_DAMAGE = 200;

    public static enum Moves
    {
        ATTACK,
        DEFEND,
        SPECIAL
    }

    public static int getDamage(Moves move)
    {
        switch (move)
        {
            case ATTACK:
                return BASIC_DAMAGE;

            case DEFEND:
                return 0;

            case SPECIAL:
                return SPECIAL_DAMAGE;

            default:
                return 0;
        }
    }
}
