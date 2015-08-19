package com.vfs.augmented.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
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
    boolean             _gameCanStart = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_activity);

        _btController = ((BluetoothApplication)this.getApplicationContext())._bluetoothController;
        _game = ((BluetoothApplication)this.getApplicationContext())._game;
        _btController.changeActivity(this, this);

        // Tell other player we are in this activity
        _btController.sendMessage(new Packet(PacketCodes.PLAYER_IS_READY, ""));

        // If the other player is in this activity
        if(((BluetoothApplication)this.getApplicationContext())._enemyIsInGameActivity)
            _gameCanStart = true;
    }

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
            File metaioManModel = AssetsManager.getAssetPathAsFile(getApplicationContext(), "models/metaioman.md2");
            File cube = AssetsManager.getAssetPathAsFile(getApplicationContext(), "models/cube.obj");

            metaioCallback = getMetaioSDKCallbackHandler();
            metaioSDK.registerCallback(metaioCallback);

            if (metaioManModel != null)
            {
                // Loading 3D geometry
                IGeometry geometry = metaioSDK.createGeometry(metaioManModel);
                IGeometry geometry2 = metaioSDK.createGeometry(cube);

                if (geometry != null)
                {
                    // Set geometry properties
                    geometry.setScale(4f);
                    geometry2.setScale(100f);

                    geometry.setCoordinateSystemID(1);
                    geometry2.setCoordinateSystemID(2);


                }
                else
                    MetaioDebug.log(Log.ERROR, "Error loading geometry: "+metaioManModel);
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
        }
    }

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
        _game.dealDamageToPlayer(true);
    }

//////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
