package com.vfs.augmented.game;

/**
 * Created by steveimc on 8/17/15.
 */
public class Player
{
    private final int maxLifes = 10;
    private int currentLifes = 10;

    public Monster monster;
    public int playerId;

    public String username;
    public Monster.MonsterType monsterType;

    public void takeOneLife()
    {
        currentLifes--;
    }

    public int getCurrentLifes()
    {
        return currentLifes;
    }

    public Player(Monster.MonsterType pickedMonster)
    {
        monsterType = pickedMonster;
    }
}
