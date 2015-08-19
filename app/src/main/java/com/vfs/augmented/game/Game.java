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

    LinearLayout _myPlayerHPView;
    LinearLayout _enemyPlayerHPView;

    int _currentTurn = 0;
    int _movesPerTurn = 3;

    TextView turnNumber;

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
        setupViews();
        turnNumber = (TextView) _gameActivity.findViewById(R.id.game_turn);
        updateTurn();
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

    void setupViews()
    {
        TextView playerName =  (TextView) _gameActivity.findViewById(R.id.game_player_username);
        TextView enemyName  =  (TextView) _gameActivity.findViewById(R.id.game_enemy_username);
        playerName.setText(_myPlayer.username);
        enemyName.setText(_enemyPlayer.username);

        _myPlayerHPView = (LinearLayout) _gameActivity.findViewById(R.id.game_playerhp);
        _enemyPlayerHPView = (LinearLayout) _gameActivity.findViewById(R.id.game_enemyhp);
    }

    public void nextTurn()
    {
        _currentTurn++;
        updateTurn();
    }

    public void dealDamageToPlayer(boolean isOwner)
    {
        if(isOwner)
        {
            _myPlayer.takeOneLife();
            updateHPView(true, _myPlayer.getCurrentLifes());
        }
    }

    void updateTurn()
    {
        turnNumber.setText(Integer.toString(_currentTurn));
    }

    void updateHPView(boolean isOwner, int currentHp)
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

    int[] hpViews = new int[]{R.id.game_player_hp1, R.id.game_player_hp2, R.id.game_player_hp3, R.id.game_player_hp4,
    R.id.game_player_hp5, R.id.game_player_hp6, R.id.game_player_hp7, R.id.game_player_hp8, R.id.game_player_hp10};
}
