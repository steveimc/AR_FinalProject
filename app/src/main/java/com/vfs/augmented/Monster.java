package com.vfs.augmented;

import android.content.Context;

import com.metaio.tools.io.AssetsManager;

import java.io.File;

/**
 * Created by steveimc on 8/17/15.
 */
public class Monster
{
    private MonsterType _id;
    private int _life;
    private File _modelFile;

    public static enum MonsterType
    {
        MONSTER_ONE,
        MONSTER_TWO
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

    public void setDamage(int damage)
    {
        _life -= damage;
    }

    public MonsterType getId()
    {
        return _id;
    }

    public int getLife()
    {
        return _life;
    }

    public File getGeometry(Context context, MonsterType id)
    {
        if(id == MonsterType.MONSTER_ONE)
            _modelFile = AssetsManager.getAssetPathAsFile(context, "Monster1.mfbx");
        else
            _modelFile = AssetsManager.getAssetPathAsFile(context, "Monster2.mfbx");

        return _modelFile;
    }

}
