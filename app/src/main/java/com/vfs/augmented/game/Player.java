package com.vfs.augmented.game;

/**
 * Created by steveimc on 8/17/15.
 */
public class Player
{
    private final int lifes = 10;

    public Monster monster;
    public int playerId;

    public String username;
    public Monster.MonsterType monsterType;

    public Player(Monster.MonsterType pickedMonster)
    {
        monsterType = pickedMonster;
    }
}
