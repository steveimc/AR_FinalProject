package com.vfs.augmented.activities;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.tools.io.AssetsManager;
import com.vfs.augmented.R;

import java.io.File;

public class GameActivity extends ARViewActivity
{

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
    protected void onGeometryTouched(IGeometry geometry)
    {

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
