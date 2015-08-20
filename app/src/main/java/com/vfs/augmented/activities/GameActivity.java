package com.vfs.augmented.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.ETRACKING_STATE;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.tools.io.AssetsManager;
import com.vfs.augmented.BluetoothApplication;
import com.vfs.augmented.R;
import com.vfs.augmented.UserInterfaceUtil;
import com.vfs.augmented.bluetooth.interfaces.BTCReceiver;
import com.vfs.augmented.bluetooth.BluetoothController;
import com.vfs.augmented.bluetooth.packet.Packet;
import com.vfs.augmented.bluetooth.packet.PacketCodes;
import com.vfs.augmented.game.Game;
import com.vfs.augmented.game.Monster.Moves;
import com.vfs.augmented.game.Monster;

import java.io.File;
import java.util.Random;

public class GameActivity extends ARViewActivity implements BTCReceiver
{
    BluetoothController _btController;
    Game                _game;
    View                _gameUI;
    boolean             _gameCanStart = false;
    public IGeometry    _myPlayerGeometry;
    public IGeometry    _enemyPlayerGeometry;
    MediaPlayer         _mediaPlayer;
    boolean             _isSinglePlayer = false;
    View                _attackBar;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        _game = ((BluetoothApplication) this.getApplicationContext())._game;
        _game.onGameActivity(this);
        _gameUI = mGUIView;
        setupViews();

        _isSinglePlayer = getIntent().getBooleanExtra(ConnectActivity.SINGLE_PLAYER, false);

        if(!_isSinglePlayer)
        {
            _btController = ((BluetoothApplication) this.getApplicationContext())._bluetoothController;
            _btController.changeActivity(this, this);
            // Tell other player we are in this activity
            _btController.sendPacket(new Packet(PacketCodes.PLAYER_IS_READY, ""));
            // If the other player is in this activity
            if(((BluetoothApplication)this.getApplicationContext())._enemyIsInGameActivity)
                _gameCanStart = true;
        }
        else
        {
            _gameCanStart = true;
            _game.getEnemyPlayer()._ready = true;
        }

        _mediaPlayer = MediaPlayer.create(GameActivity.this,R.raw.pokemon_remastered);
        _mediaPlayer.setLooping(true);

        _game.startGame();
        //_gameUI.setAlpha(0);
    }

///   METAIO    //////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////

    private IMetaioSDKCallback metaioCallback;

    @Override
    protected int getGUILayout()
    {
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
            File texture = Monster.getTextureFile(getApplicationContext(), _game.getMyPlayer()._monster.getId());
            File texture2 = Monster.getTextureFile(getApplicationContext(), _game.getEnemyPlayer()._monster.getId());

            metaioCallback = getMetaioSDKCallbackHandler();
            metaioSDK.registerCallback(metaioCallback);

            File monsterFile1 = Monster.getModelFile(getApplicationContext(), _game.getMyPlayer()._monster.getId());
            File monsterFile2 = Monster.getModelFile(getApplicationContext(), _game.getEnemyPlayer()._monster.getId());

            if (monsterFile1 != null && monsterFile2 != null)
            {
                // Loading 3D geometry
                _myPlayerGeometry = metaioSDK.createGeometry(monsterFile1);
                _enemyPlayerGeometry = metaioSDK.createGeometry(monsterFile2);

                if (_myPlayerGeometry != null)
                {
                    // Set geometry properties
                    _myPlayerGeometry.setScale(20f);
                    _enemyPlayerGeometry.setScale(20f);

                    _myPlayerGeometry.setRotation(new Rotation(-30,0,0));
                    _enemyPlayerGeometry.setRotation(new Rotation(-30, 0, 0));

                    _myPlayerGeometry.setCoordinateSystemID(_game.getMyPlayer()._monster.getId().ordinal());
                    _enemyPlayerGeometry.setCoordinateSystemID(_game.getEnemyPlayer()._monster.getId().ordinal());

                    _myPlayerGeometry.setTexture(texture);
                    _enemyPlayerGeometry.setTexture(texture2);

                    _myPlayerGeometry.startAnimationRange(0,50,true);
                    _enemyPlayerGeometry.startAnimationRange(0,50,true);
                }
                else
                    MetaioDebug.log(Log.ERROR, "Error loading geometry: "+ monsterFile1);
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
                    if(!_isSinglePlayer)
                        _btController.sendPacket(new Packet(PacketCodes.PLAYER_IS_TRACKING, PacketCodes.YES));
                }
                else if(!value.getState().equals(ETRACKING_STATE.ETS_FOUND) && _game.getMyPlayer()._ready)
                {
                    _game.getMyPlayer()._ready = false;
                    if(!_isSinglePlayer)
                        _btController.sendPacket(new Packet(PacketCodes.PLAYER_IS_TRACKING, PacketCodes.NO));
                }
            }

            setUI();

        }

        @Override
        public void onAnimationEnd(IGeometry geometry, String name)
        {

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
                break;
            case PacketCodes.PLAYER_MOVE:
                doEnemyAttack(packet.value);
                break;
            case PacketCodes.PLAYER_IS_TRACKING:
                if(packet.value.equals(PacketCodes.YES))
                    _game.getEnemyPlayer()._ready = true;
                else
                    _game.getEnemyPlayer()._ready = false;

                setUI();
                break;
        }
    }

    public void animate(IGeometry monsterGeometry, Monster.Range range)
    {
        monsterGeometry.startAnimationRange(range.start, range.end,range.loop);
    }


///   GAME    //////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////

    // Comes from a local choice
    private void doPlayerAttack(Moves move)
    {
        _game.addPlayerMove(move);

        if(_isSinglePlayer)
        {
            doBotAttack();
            _game.doTurn();
        }
        else
        {
            switch (move)
            {
                case ATTACK:
                    _btController.sendPacket(new Packet(PacketCodes.PLAYER_MOVE, PacketCodes.MOVE_ATTACK));
                    break;
                case DEFEND:
                    _btController.sendPacket(new Packet(PacketCodes.PLAYER_MOVE, PacketCodes.MOVE_DEFEND));
                    break;
                case MAGIC:
                    _btController.sendPacket(new Packet(PacketCodes.PLAYER_MOVE, PacketCodes.MOVE_MAGIC));
                    break;
            }

            if(_game.bothPlayersSubmittedMoveForCurrentTurn())
            {
                // If both players are done, do turn
                _game.doTurn();
            }
            else
            {
                //  Otherwise means this player is waiting for enemys input
                //  Hide Buttons & Show waiting in ui
            }
        }
    }

    private void doBotAttack()
    {
        Random rand = new Random();
        int  random = rand.nextInt(3) + 1;
        switch (random)
        {
            case 1:
                _game.addEnemyMove(Moves.ATTACK);
                break;
            case 2:
                _game.addEnemyMove(Moves.DEFEND);
                break;
            case 3:
                _game.addEnemyMove(Moves.MAGIC);
                break;
        }
    }

    // Comes from a packet
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
            case PacketCodes.MOVE_MAGIC:
                enemyMove = Moves.MAGIC;

                break;
        }
        //Toast.makeText(this, "Enemy: " + enemyMove, Toast.LENGTH_SHORT).show();
        _game.addEnemyMove(enemyMove);

        if(_game.bothPlayersSubmittedMoveForCurrentTurn())
        {
            _game.doTurn();
        }
    }

    public void gameIsOver(boolean playerWon)
    {
        if(!_isSinglePlayer)
            _btController.stopConnection();

        final Intent mainIntent = new Intent(GameActivity.this, GameOverActivity.class);
        mainIntent.putExtra(GameOverActivity.GAME_OVER, playerWon);
        this.startActivity(mainIntent);
        this.finish();
    }

///   BUTTONS    //////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////

    public void onAttackButton(View view)
    {
        UserInterfaceUtil.hideView(_attackBar);
        doPlayerAttack(Moves.ATTACK);
        popSound();
    }

    public void onDefenseButton(View view)
    {
        UserInterfaceUtil.hideView(_attackBar);
        doPlayerAttack(Moves.DEFEND);
        popSound();
    }

    public void onSpecialButton(View view)
    {
        UserInterfaceUtil.hideView(_attackBar);
        doPlayerAttack(Moves.MAGIC);
        popSound();
    }

    public void popSound()
    {
        MediaPlayer mediaPlayer = MediaPlayer.create(GameActivity.this,R.raw.blob_select);
        if(!mediaPlayer.isPlaying())
        {
            mediaPlayer.start();
        }
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

        String player   = _game.getMyPlayer()._username;
        String enemy    = _game.getEnemyPlayer()._username;

        playerName.setText(player);
        enemyName.setText(enemy);
        Log.e("playerName", playerName.getText().toString());

        _myPlayerHPView     = (View) mGUIView.findViewById(R.id.game_playerhp);
        _enemyPlayerHPView  = (View) mGUIView.findViewById(R.id.game_enemyhp);
        _turnNumber         = (TextView) mGUIView.findViewById(R.id.game_turn);
        _attackBar          = (View) mGUIView.findViewById(R.id.game_attack_bar);
        UserInterfaceUtil.hideView(_attackBar);
    }

    private void setUI()
    {
        if(_gameUI == null)
        {
            Log.e("setUI", "_gameUI is null");
            return;
        }

        if(_game.getMyPlayer()._ready && _game.getEnemyPlayer()._ready)
        {
            if(!_mediaPlayer.isPlaying())
                _mediaPlayer.start();
            //_gameUI.setAlpha(255);
        }
        else
        {
            if(_mediaPlayer.isPlaying())
            {
                _mediaPlayer.pause();
                _mediaPlayer.seekTo(0);
            }
            //_gameUI.setAlpha(0);
        }
    }


    public void updateTurn(int turn)
    {
        _turnNumber.setText(Integer.toString(turn));
        UserInterfaceUtil.showView(_attackBar);
    }

    public void updateHPView(boolean isOwner, int currentHp)
    {
        if(currentHp < 0)
            return;

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
