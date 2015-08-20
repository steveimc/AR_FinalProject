package com.vfs.augmented.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.vfs.augmented.BluetoothApplication;
import com.vfs.augmented.R;
import com.vfs.augmented.UserInterfaceUtil;

public class GameOverActivity extends Activity
{
    public static final String GAME_OVER =  "gameOver";
    String _win     = "YOU WIN!";
    String _lose    = "YOU LOSE...";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_over_activity);

        boolean _playerWon = getIntent().getBooleanExtra(GAME_OVER, false);
        showOutcome(_playerWon);
        resetGame();
    }

    void resetGame()
    {
        // Reset the App Global variables and disconnect so a new game can be started
        BluetoothApplication thisApp = ((BluetoothApplication)this.getApplicationContext());
        thisApp._bluetoothController = null;
        thisApp._game = null;
        thisApp._enemyIsInGameActivity = false;
    }

    public void onMenuButton(View view)
    {
        UserInterfaceUtil.showSkullButtonClick(GameOverActivity.this, view);
        final Intent mainIntent = new Intent(GameOverActivity.this, ConnectActivity.class);
        this.startActivity(mainIntent);
        this.finish();
    }


    void showOutcome(boolean win)
    {
        ImageView img = (ImageView) findViewById(R.id.gameover_image);
        TextView text = (TextView) findViewById(R.id.gameover_text);

        if(!win)
        {
            img.setImageResource(R.drawable.mons_gameover_lose);
            text.setText(_lose);
        }
        else
        {
            img.setImageResource(R.drawable.mons_gameover_win);
            text.setText(_win);
        }
    }
}
