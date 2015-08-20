package com.vfs.augmented.game;

import android.os.Handler;
import android.widget.Toast;

import com.vfs.augmented.activities.GameActivity;
import com.vfs.augmented.game.Monster.Moves;

import java.util.ArrayList;

/**
 * Created by andreia on 17/08/15.
 */
public class Game
{
    GameActivity        _gameActivity;
    private Player      _myPlayer;
    private Player      _enemyPlayer;
    private int         _currentTurn = -1;
    ArrayList<Turn>     _turns;
    private boolean     _hasStarted = false;

    public class Turn
    {
        public Turn(int count){this.count = count;}
        int count = -1;
        Moves playerMove;
        Moves enemyMove;
        Player winner;
    }



    public Game(Player myPlayer, Player enemyPlayer)
    {
        _myPlayer       = myPlayer;
        _enemyPlayer    = enemyPlayer;
    }

    public void onGameActivity(GameActivity activity)
    {
        _gameActivity = activity;
    }

    // If both players are in this activity, game may start
    public void startGame()
    {
        _hasStarted = true;
        _turns = new ArrayList<Turn>();
        nextTurn();
    }

    public Player getMyPlayer()
    {
        return _myPlayer;
    }

    public Player getEnemyPlayer()
    {
        return _enemyPlayer;
    }

    private void nextTurn()
    {
        _currentTurn++;
        Turn newTurn = new Turn(_currentTurn);
        _turns.add(newTurn);

        // Turns start at 0 but UI start at 1
        _gameActivity.updateTurn(_currentTurn + 1);
    }

    public void addPlayerMove(Moves pMove)
    {
        _turns.get(_currentTurn).playerMove = pMove;
    }

    public void addEnemyMove(Moves eMove)
    {
        _turns.get(_currentTurn).enemyMove = eMove;
    }

    public boolean bothPlayersSubmittedMoveForCurrentTurn()
    {
        return (_turns.get(_currentTurn).playerMove != null && _turns.get(_currentTurn).enemyMove != null);
    }

    public void doTurn()
    {
        // Change in UI
        // Call Animations
        _turns.get(_currentTurn).winner = calculateTurnWinner(_turns.get(_currentTurn).playerMove, _turns.get(_currentTurn).enemyMove);
        _gameActivity.animate(_gameActivity._myPlayerGeometry,Monster.getAnimation(_turns.get(_currentTurn).playerMove));
        _gameActivity.animate(_gameActivity._enemyPlayerGeometry,Monster.getAnimation(_turns.get(_currentTurn).enemyMove));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run()
            {
                _gameActivity.animate(_gameActivity._myPlayerGeometry,Monster.getAnimation(Moves.IDLE));
                _gameActivity.animate(_gameActivity._enemyPlayerGeometry,Monster.getAnimation(Moves.IDLE));
                onFinishTurn();
            }
        },2000);

    }

    public void myPlayerWon()
    {
        // Show I WIN in UI
        dealDamageToPlayer(_enemyPlayer); // Damage enemy
    }

    public void myPlayerLost()
    {
        // Show I LOSE in UI
        dealDamageToPlayer(_myPlayer); // Damage my char
    }

    // Called when animations are done
    public void onFinishTurn()
    {
        // Updates players hp
        // If I win
        if(_turns.get(_currentTurn).winner == null)
        {
            doTie();
        }
        else if(_turns.get(_currentTurn).winner == _myPlayer)
        {
            myPlayerWon();
        }
        else
        {
            myPlayerLost();
        }

        if(!isGameOver())
        {
            nextTurn();
        }
        else
        {
            _gameActivity.gameIsOver(didMyPlayerWin());
        }

    }

    private boolean isGameOver()
    {
        // if one of the players has 0 life, game is over
        if(_myPlayer.getCurrentLifes() == 0 || _enemyPlayer.getCurrentLifes() == 0)
            return true;

        return false;
    }

    private boolean didMyPlayerWin()
    {
        // Check who won, If this player is the one with 0 lifes, the enemy won
        if(_myPlayer.getCurrentLifes() == 0)
            return false;
        else
            return true;
    }

    private Player calculateTurnWinner(Moves p1Move, Moves p2Move)
    {
        if(p1Move == p2Move)
        {
            // TIE, winner is null
        }
        else
        {
            switch (p1Move)
            {
                case ATTACK:
                    if(p2Move == Moves.DEFEND)
                    {
                        return _enemyPlayer;
                    }
                    else if(p2Move == Moves.SPECIAL)
                    {
                        return _myPlayer;
                    }
                    break;
                case DEFEND:
                    if(p2Move == Moves.ATTACK)
                    {
                        return _myPlayer;
                    }
                    else if(p2Move == Moves.SPECIAL)
                    {
                        return _enemyPlayer;
                    }
                    break;
                case SPECIAL:
                    if(p2Move == Moves.ATTACK)
                    {
                        return _enemyPlayer;
                    }
                    else if(p2Move == Moves.DEFEND)
                    {
                        return _myPlayer;
                    }
                    break;
            }
        }

        return null;
    }

    public void doTie()
    {
        //shows in ui
        Toast.makeText(_gameActivity, "TIE", Toast.LENGTH_SHORT).show();
    }

    public void dealDamageToPlayer(Player player)
    {
        player.takeDamage();
        boolean isOwner = false;

        if(player == _myPlayer)
            isOwner = true;

        _gameActivity.updateHPView(isOwner, player.getCurrentLifes());
    }

}
