package com.vfs.augmented;

import android.app.Application;

import com.vfs.augmented.bluetooth.BluetoothController;
import com.vfs.augmented.game.Game;

/**
 * Created by andreia on 17/08/15.
 */
public class BluetoothApplication extends Application
{
    public BluetoothController _bluetoothController;
    public Game _game;

    public boolean _enemyIsInGameActivity = false;
}
