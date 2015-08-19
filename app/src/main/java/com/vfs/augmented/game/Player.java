package com.vfs.augmented.game;

/**
 * Created by steveimc on 8/17/15.
 */
public class Player
{
    private final int MAX_LIFE = 10;

    public boolean _ready = false;

    public Monster _monster;

    public String _username;

    public void takeDamage()
    {
        _monster.setDamage();
    }

    public int getCurrentLifes()
    {
        return _monster.getLife();
    }

    public Player(Monster.MonsterType pickedMonster)
    {
        _monster = new Monster(pickedMonster,MAX_LIFE);

    }
}
