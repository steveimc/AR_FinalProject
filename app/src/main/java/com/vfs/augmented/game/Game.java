package com.vfs.augmented.game;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vfs.augmented.R;
import com.vfs.augmented.activities.GameActivity;
import com.vfs.augmented.game.Abilities.Moves;
/**
 * Created by andreia on 17/08/15.
 */
public class Game
{
    GameActivity _gameActivity;

    private Player _myPlayer;
    private Player _enemyPlayer;

    int _currentTurn = 0;
    int _movesPerTurn = 3;

    public Game(Player myPlayer, Player enemyPlayer)
    {
        _myPlayer       = myPlayer;
        _enemyPlayer    = enemyPlayer;
    }

    public Player getMyPlayer()
    {
        return _myPlayer;
    }

    public Player getEnemyPlayer()
    {
        return _enemyPlayer;
    }

    public void onGameActivity(GameActivity activity)
    {
        _gameActivity = activity;
    }

    public void updateGame(int turn, Moves[] player1Moves, Moves[] player2Moves)
    {
        calculateDamage(player1Moves[0], player2Moves[0]);
        calculateDamage(player1Moves[1], player2Moves[1]);
        calculateDamage(player1Moves[2], player2Moves[2]);
    }

    private void calculateDamage(Moves p1Move, Moves p2Move)
    {
        // Update players life & stuff
        // Update UI
        // Do Animations
    }

    public void nextTurn()
    {
        _currentTurn++;
        _gameActivity.updateTurn(_currentTurn);
    }

    public void dealDamageToPlayer(boolean isOwner)
    {
        if(isOwner)
        {
            _myPlayer.takeOneLife();
            _gameActivity.updateHPView(true, _myPlayer.getCurrentLifes());
        }
    }

}
