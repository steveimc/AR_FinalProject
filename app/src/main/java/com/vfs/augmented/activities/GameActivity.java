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

        _gameUI = mGUIView.findViewById(R.id.game_ui);
        _gameUI.setVisibility(View.INVISIBLE);


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

    @Override
    public void onDrawFrame()
    {
        super.onDrawFrame();

        if(_game.getMyPlayer()._ready && _game.getEnemyPlayer()._ready)
        {
            _gameUI.setVisibility(View.VISIBLE);
        }
        else
        {
            _gameUI.setVisibility(View.INVISIBLE);
        }
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
                    _btController.sendMessage(new Packet(PacketCodes.PLAYER_IS_TRACKING,PacketCodes.YES));
                }
                else
                {
                    _game.getMyPlayer()._ready = false;
                    _btController.sendMessage(new Packet(PacketCodes.PLAYER_IS_TRACKING,PacketCodes.NO));
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

    private void setUI()
    {
        if(_game.getMyPlayer()._ready && _game.getEnemyPlayer()._ready)
            _gameUI.setVisibility(View.VISIBLE);
        else
            _gameUI.setVisibility(View.INVISIBLE);
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
                break;
        }

        setUI();
    }

///   GAME    //////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////

    private void doMove(Moves move)
    {
        switch (move)
        {
            case ATTACK:
                _btController.sendMessage(new Packet(PacketCodes.PLAYER_MOVE, PacketCodes.MOVE_ATTACK));
                doPlayerAttack("attack");
                break;
            case DEFEND:
                _btController.sendMessage(new Packet(PacketCodes.PLAYER_MOVE, PacketCodes.MOVE_DEFEND));
                doPlayerAttack("defend");
                break;
            case SPECIAL:
                _btController.sendMessage(new Packet(PacketCodes.PLAYER_MOVE, PacketCodes.MOVE_SPECIAL));
                doPlayerAttack("special");
                break;
        }
    }

    private void doPlayerAttack(String move)
    {
        Toast.makeText(this, "Me: " + move, Toast.LENGTH_SHORT).show();
    }

    private void doEnemyAttack(String moveCode)
    {
        String move = "";
        switch (moveCode)
        {
            case PacketCodes.MOVE_ATTACK:
                move = "attack";
                break;
            case PacketCodes.MOVE_DEFEND:
                move = "defend";
                break;
            case PacketCodes.MOVE_SPECIAL:
                move = "special";
                break;
        }
        Toast.makeText(this, "Enemy: " + move, Toast.LENGTH_SHORT).show();
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

    int[] hpViews = new int[] {
            R.id.game_player_hp1,
            R.id.game_player_hp2,
            R.id.game_player_hp3,
            R.id.game_player_hp4,
            R.id.game_player_hp5,
            R.id.game_player_hp6,
            R.id.game_player_hp7,
            R.id.game_player_hp8,
            R.id.game_player_hp9,
            R.id.game_player_hp10};
}
