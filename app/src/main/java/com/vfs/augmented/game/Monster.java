package com.vfs.augmented.game;

import android.content.Context;
import com.metaio.tools.io.AssetsManager;
import java.io.File;

/**
 * Created by steveimc on 8/17/15.
 * This class implements getters and setters according to the monster object
 * We have the necessary monster information like life, id, animations, and files
 */
public class Monster
{
    private MonsterType _id;
    private int _life;

    //This class is used to get the animations range
    public static class Range
    {
        public long start;
        public long end;
        public boolean loop;
    }

    //The types of monsters available in the game
    public static enum MonsterType
    {
        NONE,
        MONSTER_ONE,
        MONSTER_TWO
    }

    //The moves each monster has
    public static enum Moves
    {
        ATTACK,
        DEFEND,
        MAGIC,
        IDLE
    }

    //Initialize the monster object
    public Monster(MonsterType id, int life)
    {
        setId(id);
        setLife(life);
    }

    public void setId(MonsterType id)
    {
        _id = id;
    }

    public void setLife(int life)
    {
        _life = life;
    }

    public void setDamage()
    {
        _life--;
    }

    public MonsterType getId()
    {
        return _id;
    }

    public int getLife()
    {
        return _life;
    }

    //Return the model file depending on what the user picked
    public static File getModelFile(Context context, MonsterType id)
    {
        File modelFile;

        if(id == MonsterType.MONSTER_ONE)
            modelFile = AssetsManager.getAssetPathAsFile(context, "models/Monster1.mfbx");
        else
            modelFile = AssetsManager.getAssetPathAsFile(context, "models/Monster2.mfbx");

        return modelFile;
    }

    //Return the texture depending of the monster type
    public static File getTextureFile(Context context, MonsterType id)
    {
        File textureFile;
        if(id == MonsterType.MONSTER_ONE)
            textureFile = AssetsManager.getAssetPathAsFile(context, "textures/MonsterTexture.png");
        else
            textureFile = AssetsManager.getAssetPathAsFile(context, "textures/MonsterTexture2.png");

        return textureFile;
    }

    //Return the frames of the animation depending on the monster move
    public static Range getAnimation(Moves move)
    {
        Range animationRange = new Range();

        switch (move)
        {
            case IDLE:
                animationRange.start = 1;
                animationRange.end = 50;
                animationRange.loop = true;
                break;
            case MAGIC:
                animationRange.start = 50;
                animationRange.end = 100;
                animationRange.loop = false;
                break;
            case DEFEND:
                animationRange.start = 100;
                animationRange.end = 150;
                animationRange.loop = false;
                break;
            case ATTACK:
                animationRange.start = 150;
                animationRange.end = 210;
                animationRange.loop = false;
                break;
        }

        return animationRange;
    }

}
