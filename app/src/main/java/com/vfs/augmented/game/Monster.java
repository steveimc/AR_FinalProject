package com.vfs.augmented.game;

import android.content.Context;

import com.metaio.sdk.jni.IGeometry;
import com.metaio.tools.io.AssetsManager;

import java.io.File;

/**
 * Created by steveimc on 8/17/15.
 */
public class Monster
{
    private MonsterType _id;
    private int _life;
    public IGeometry _geometry;

    public static class Range
    {
        public long start;
        public long end;
        public boolean loop;
    }

    public static enum MonsterType
    {
        NONE,
        MONSTER_ONE,
        MONSTER_TWO
    }

    public static enum Moves
    {
        ATTACK,
        DEFEND,
        SPECIAL,
        IDLE
    }

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

    public static File getModelFile(Context context, MonsterType id)
    {
        File modelFile;

        if(id == MonsterType.MONSTER_ONE)
            modelFile = AssetsManager.getAssetPathAsFile(context, "models/Monster1.mfbx");
        else
            modelFile = AssetsManager.getAssetPathAsFile(context, "models/Monster2.mfbx");

        return modelFile;
    }

    public static File getTextureFile(Context context, MonsterType id)
    {
        File textureFile;
        if(id == MonsterType.MONSTER_ONE)
            textureFile = AssetsManager.getAssetPathAsFile(context, "textures/MonsterTexture.png");
        else
            textureFile = AssetsManager.getAssetPathAsFile(context, "textures/MonsterTexture2.png");

        return textureFile;
    }

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
            case SPECIAL:
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
