package com.vfs.augmented.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.ETRACKING_STATE;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.tools.io.AssetsManager;
import com.vfs.augmented.BluetoothApplication;
import com.vfs.augmented.R;
import com.vfs.augmented.bluetooth.interfaces.BTCReceiver;
import com.vfs.augmented.bluetooth.BluetoothController;
import com.vfs.augmented.bluetooth.packet.Packet;
import com.vfs.augmented.bluetooth.packet.PacketCodes;
import com.vfs.augmented.game.Game;
import com.vfs.augmented.game.Abilities.Moves;

import java.io.File;

public class GameActivity extends ARViewActivity implements BTCReceiver
{
    BluetoothController _btController;
    Game                _game;
    View                _gameUI;
    boolean             _gameCanStart = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        _btController = ((BluetoothApplication)this.getApplicationContext())._bluetoothController;
        _game = ((BluetoothApplication)this.getApplicationContext())._game;
        _btController.changeActivity(this, this);

        _game.onGameActivity(this);
        setupViews();

        // Tell other player we are in this activity
        _btController.sendMessage(new Packet(PacketCodes.PLAYER_IS_READY, ""));

        // If the other player is in this activity
        if(((BluetoothApplication)this.getApplicationContext())._enemyIsInGameActivity)
            _gameCanStart = true;

        _gameUI = mGUIView;
        _game.startGame();
        //_gameUI.setAlpha(0);
    }

///   METAIO    //////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////

    private IMetaioSDKCallback metaioCallback;

    @Override
    protected int getGUILayout()
    {
        Log.e("getGUILayout", "called");
        return R.layout.game_activity;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
    {
        return new MetaioSDKCallbackHandler();
    }

    @Override
    protected void loadContents()
    {
        try
        {
            // Getting a file path for tracking configuration XML file
            AssetsManager.extractAllAssets(this, true);
            File trackingConfigFile = AssetsManager.getAssetPathAsFile(getApplicationContext(), "TrackingData.xml");

            // Assigning tracking configuration
            boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
            MetaioDebug.log("Tracking data loaded: " + result);

            // Getting a file path for a 3D geometry
            File monster1 = AssetsManager.getAssetPathAsFile(getApplicationContext(), "models/Monster1.mfbx");
            File monster2 = AssetsManager.getAssetPathAsFile(getApplicationContext(), "models/Monster1.mfbx");
            File texture = AssetsManager.getAssetPathAsFile(getApplicationContext(), "textures/MonsterTexture.png");

            metaioCallback = getMetaioSDKCallbackHandler();
            metaioSDK.registerCallback(metaioCallback);

            if (monster1 != null)
            {
                // Loading 3D geometry
                IGeometry geometry = metaioSDK.createGeometry(monster1);
                IGeometry geometry2 = metaioSDK.createGeometry(monster2);

                if (geometry != null)
                {
                    // Set geometry properties
                    geometry.setScale(50f);
                    geometry2.setScale(50f);

                    geometry.setCoordinateSystemID(1);
                    geometry2.setCoordinateSystemID(2);

                    geometry.setTexture(texture);
                    geometry2.setTexture(texture);

                    geometry.startAnimationRange(0,50,true);
                    geometry2.startAnimationRange(0,50,true);

                }
                else
                    MetaioDebug.log(Log.ERROR, "Error loading geometry: "+ monster1);
            }
        }
        catch (Exception e)
        {
            MetaioDebug.printStackTrace(Log.ERROR, e);
        }
    }

    @Override
    protected void onGeometryTouched(IGeometry geometry) {

    }

    private class MetaioSDKCallbackHandler extends IMetaioSDKCallback
    {
        @Override
        public void onTrackingEvent(TrackingValuesVector trackingValues)
        {
            for (int i=0; i<trackingValues.size(); i++)
            {
                final TrackingValues value = trackingValues.get(i);

                if(value.getState().equals(ETRACKING_STATE.ETS_FOUND) && !_game.getMyPlayer()._ready)
                {
                    _game.getMyPlayer()._ready = true;
                    _btController.sendMessage(new Packet(PacketCodes.PLAYER_IS_TRACKING, PacketCodes.YES));
                    Log.e("onTacking", value.getState().toString()+ "_Enemy ready = " + _game.getEnemyPlayer()._ready);
                }
                else if(!value.getState().equals(ETRACKING_STATE.ETS_FOUND) && _game.getMyPlayer()._ready)
                {
                    _game.getMyPlayer()._ready = false;
                    _btController.sendMessage(new Packet(PacketCodes.PLAYER_IS_TRACKING,PacketCodes.NO));
                    Log.e("onTacking", value.getState().toString() + "_Enemy ready = " + _game.getEnemyPlayer()._ready);
                }
            }

            setUI();

        }

        @Override
        public void onAnimationEnd(IGeometry geometry, String name)
        {
            /*
            Log.i("xmetaio", "tracking event: " + trackingValues.size());
            for (int i=0; i<trackingValues.size(); i++)
            {
                final TrackingValues v = trackingValues.get(i);
                MetaioDebug.log("Tracking state for COS " + v.getCoordinateSystemID() + " is " + v.getState());
                Log.i("xmetaio", "Tracking state for COS " + v.getCoordinateSystemID() + " is " + v.getState());
            }
            */
        }

    }


///   BLUETOOTH    //////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////

    @Override
    public void receivePacket(Packet packet)
    {
        switch (packet.code)
        {
            case PacketCodes.PLAYER_IS_READY:
                _gameCanStart = true;
                Toast.makeText(this, "other player in. My Monster: " + _game.getMyPlayer().monsterType, Toast.LENGTH_SHORT).show();
                break;
            case PacketCodes.PLAYER_MOVE:
                doEnemyAttack(packet.value);
                break;
            case PacketCodes.PLAYER_IS_TRACKING:
                if(packet.value.equals(PacketCodes.YES))
                    _game.getEnemyPlayer()._ready = true;
                else
                    _game.getEnemyPlayer()._ready = false;

                Log.e("playerIsTrackinPAcket", "enemy Ready:" + _game.getEnemyPlayer()._ready);
                setUI();
                break;
        }
    }

///   GAME    //////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////

    private void doMove(Moves move)
    {
        switch (move)
        {
            case ATTACK:
                _btController.sendMessage(new Packet(PacketCodes.PLAYER_MOVE, PacketCodes.MOVE_ATTACK));
                doPlayerAttack(Moves.ATTACK);
                break;
            case DEFEND:
                _btController.sendMessage(new Packet(PacketCodes.PLAYER_MOVE, PacketCodes.MOVE_DEFEND));
                doPlayerAttack(Moves.DEFEND);
                break;
            case SPECIAL:
                _btController.sendMessage(new Packet(PacketCodes.PLAYER_MOVE, PacketCodes.MOVE_SPECIAL));
                doPlayerAttack(Moves.SPECIAL);
                break;
        }
    }

    private void doPlayerAttack(Moves move)
    {
        Toast.makeText(this, "Me: " + move, Toast.LENGTH_SHORT).show();
        _game.addPlayerMove(move);

        // If both players are done, do turn
        if(_game.bothPlayersSubmittedMoveForCurrentTurn())
        {
            _game.doTurn();
        }
        else
        {
            //  Otherwise means this player is waiting for enemys input
            //  Hide Buttons & Show waiting in ui
        }
    }

    private void doEnemyAttack(String moveCode)
    {
        Moves enemyMove = null;
        switch (moveCode)
        {
            case PacketCodes.MOVE_ATTACK:
                enemyMove = Moves.ATTACK;
                break;
            case PacketCodes.MOVE_DEFEND:
                enemyMove = Moves.DEFEND;
                break;
            case PacketCodes.MOVE_SPECIAL:
                enemyMove = Moves.SPECIAL;
                break;
        }
        Toast.makeText(this, "Enemy: " + enemyMove, Toast.LENGTH_SHORT).show();
        _game.addEnemyMove(enemyMove);

        if(_game.bothPlayersSubmittedMoveForCurrentTurn())
        {
            _game.doTurn();
        }
    }

///   BUTTONS    //////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////

    public void onAttackButton(View view)
    {
        doMove(Moves.ATTACK);
    }

    public void onDefenseButton(View view)
    {
        doMove(Moves.DEFEND);
    }

    public void onSpecialButton(View view)
    {
        doMove(Moves.SPECIAL);
    }

    public void dealDamageToMyPlayer (View view)
    {
        _game.dealDamageToPlayer(_game.getMyPlayer());
    }

///   UI    //////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////

 
    View        _myPlayerHPView;
    View        _enemyPlayerHPView;
    TextView    _turnNumber;


    void setupViews()
    {
        // Metaio creates a View on top of its camera with the activity xml.
        // It stores our view mGUIView
        TextView playerName =  (TextView) mGUIView.findViewById(R.id.game_player_username);
        TextView enemyName  =  (TextView) mGUIView.findViewById(R.id.game_enemy_username);

        String player   = _game.getMyPlayer().username;
        String enemy    = _game.getEnemyPlayer().username;

        playerName.setText(player);
        enemyName.setText(enemy);
        Log.e("playerName", playerName.getText().toString());

        _myPlayerHPView     = (View) mGUIView.findViewById(R.id.game_playerhp);
        _enemyPlayerHPView  = (View) mGUIView.findViewById(R.id.game_enemyhp);
        _turnNumber = (TextView) mGUIView.findViewById(R.id.game_turn);
    }

    private void setUI()
    {
        /*
        if(_gameUI == null)
        {
            Log.e("setUI", "_gameUI is null");
            return;
        }

        if(_game.getMyPlayer()._ready && _game.getEnemyPlayer()._ready)
        {
            Log.e("setUI", "both ready. Set UI ON");
            //_gameUI.setVisibility(View.VISIBLE);
            _gameUI.setAlpha(255);
        }
        else
        {
            Log.e("setUI", "not ready. Set UI OFF");
            //_gameUI.setVisibility(View.INVISIBLE);
            _gameUI.setAlpha(0);
        }*/
    }


    public void updateTurn(int turn)
    {
        _turnNumber.setText(Integer.toString(turn));
    }

    public void updateHPView(boolean isOwner, int currentHp)
    {
        if(isOwner)
        {
            _myPlayerHPView.findViewById(hpViews[currentHp]).setVisibility(View.GONE);
        }
        else
        {
            _enemyPlayerHPView.findViewById(hpViews[currentHp]).setVisibility(View.GONE);
        }
    }

    // Players share the same resource of hp bars, we use this to find the one we want to hide
    int[] hpViews = new int[] { R.id.game_player_hp1, R.id.game_player_hp2, R.id.game_player_hp3, R.id.game_player_hp4, R.id.game_player_hp5,
                                R.id.game_player_hp6, R.id.game_player_hp7, R.id.game_player_hp8, R.id.game_player_hp9, R.id.game_player_hp10};
}
