package com.vfs.augmented;

import android.app.Application;

import com.vfs.augmented.bluetooth.BluetoothController;
import com.vfs.augmented.game.Game;

/**
 * Created by andreia on 17/08/15.
 * We subclassed application so the bluetooth connection may persist accross activities
 */
public class BluetoothApplication extends Application
{
    public BluetoothController  _bluetoothController; // Object that holds connection
    public Game                 _game;                // Game is initialized in SelectActivity

    public boolean _enemyIsInGameActivity = false;    // This message might be received both in Select or Game
    public String _username = "MonBot";               // username is set in LoginActivity and persists throughout all activities
}
