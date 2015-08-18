package com.vfs.augmented.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.vfs.augmented.BluetoothApplication;
import com.vfs.augmented.R;
import com.vfs.augmented.bluetooth.interfaces.BTCReceiver;
import com.vfs.augmented.bluetooth.BluetoothController;
import com.vfs.augmented.bluetooth.packet.Packet;
import com.vfs.augmented.bluetooth.packet.PacketCodes;
import com.vfs.augmented.game.Game;
import com.vfs.augmented.game.Monster;
import com.vfs.augmented.game.Player;

public class SelectActivity extends ActionBarActivity implements BTCReceiver
{
    BluetoothController _btController;
    Game _game;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_activity);

        _btController = ((BluetoothApplication)this.getApplicationContext())._bluetoothController;
        _btController.changeActivity(this, this);

    }

    Player _myPlayer;
    Player _enemyPlayer;

    @Override
    public void receivePacket(Packet packet)
    {
        if (_myPlayer != null)
            return;

        switch (packet.code)
        {
            case PacketCodes.PICK_MONSTER:
                if(packet.value.equals(PacketCodes.MONSTER1))
                {
                    //Enemy picked M1
                    _myPlayer       = new Player(Monster.MonsterType.MONSTER_TWO);
                    _enemyPlayer    = new Player(Monster.MonsterType.MONSTER_ONE);
                    createGame();
                }
                else
                {
                    //Enemy picked M2
                    _myPlayer       = new Player(Monster.MonsterType.MONSTER_ONE);
                    _enemyPlayer    = new Player(Monster.MonsterType.MONSTER_TWO);
                    createGame();
                }
        }
    }

    public void onMonster1(View view)
    {
        if(_myPlayer == null)
        {
            _btController.sendMessage(new Packet(PacketCodes.PICK_MONSTER, PacketCodes.MONSTER1));
            _myPlayer       = new Player(Monster.MonsterType.MONSTER_ONE);
            _enemyPlayer    = new Player(Monster.MonsterType.MONSTER_TWO);
            createGame();
        }
    }

    public void onMonster2(View view)
    {
        if(_myPlayer == null)
        {
            _btController.sendMessage(new Packet(PacketCodes.PICK_MONSTER, PacketCodes.MONSTER2));

            _myPlayer       = new Player(Monster.MonsterType.MONSTER_TWO);
            _enemyPlayer    = new Player(Monster.MonsterType.MONSTER_ONE);
            createGame();
        }
    }

    private void createGame()
    {
        if(_game == null)
        {
            ((BluetoothApplication) this.getApplicationContext())._game = new Game(_myPlayer, _enemyPlayer);
            _game = ((BluetoothApplication)this.getApplicationContext())._game;
            goToGameActivity();
        }
    }

    private void goToGameActivity()
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run() {
                final Intent mainIntent = new Intent(SelectActivity.this, GameActivity.class);
                SelectActivity.this.startActivity(mainIntent);
                SelectActivity.this.finish();
            }
        }, 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
