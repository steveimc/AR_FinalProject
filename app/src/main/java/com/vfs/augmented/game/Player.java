package com.vfs.augmented.game;

/**
 * Created by steveimc on 8/17/15.
 * The player class has the necessary information of each player
 * We initialize the monster object here, and have information such as if the player is ready
 * and the username.
 */
public class Player
{

    private final int MAX_LIFE = 10;
    public boolean _ready = false; //Bool set to true when player is ready to fight
    public Monster _monster; //The player's monster
    public String _username;

    public Player(Monster.MonsterType pickedMonster)
    {
        _monster = new Monster(pickedMonster,MAX_LIFE);

    }

    // Set the damage to the monster
    public void takeDamage()
    {
        _monster.setDamage();
    }

    // Get the life of the monster
    public int getCurrentLifes()
    {
        return _monster.getLife();
    }

}
