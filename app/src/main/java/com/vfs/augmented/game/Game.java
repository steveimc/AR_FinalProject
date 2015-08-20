package com.vfs.augmented.game;

import android.os.Handler;
import android.widget.Toast;

import com.vfs.augmented.activities.GameActivity;
import com.vfs.augmented.game.Monster.Moves;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by andreia on 17/08/15.
 * This class handles all the game information
 * We create one game clone per device on multiplayer mode
 *
 * GAME RULES:
 * Mons is a Rock-Paper-Scissors clone.
 * - Each turn, both players choose a move.
 * - When defeated, the player loses 1 Life.
 * - If there's a tie, nothing happens.
 *
 *  ATTACK  >   MAGIC
 *  MAGIC   >   DEFEND
 *  DEFEND  >   ATTACK
 */
public class Game
{
    GameActivity        _gameActivity;
    private Player      _myPlayer;
    private Player      _enemyPlayer;
    private int         _currentTurn = -1;
    private ArrayList<Turn>     _turns;

    public class Turn
    {
        public Turn(int count){this.count = count;}
        int count = -1;
        Moves playerMove;
        Moves enemyMove;
        Player winner;
    }

    // Initialize a game with the two players
    public Game(Player myPlayer, Player enemyPlayer)
    {
        _myPlayer       = myPlayer;
        _enemyPlayer    = enemyPlayer;
    }

    public void onGameActivity(GameActivity activity)
    {
        _gameActivity = activity;
    }

    // If both players are in the game activity, this will be called
    public void startGame()
    {
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
        Turn currentTurn = _turns.get(_currentTurn);
        currentTurn.winner = calculateTurnWinner(currentTurn.playerMove, currentTurn.enemyMove);

        // Call Animations in the monsters
        _gameActivity.animate(_gameActivity._myPlayerGeometry,Monster.getAnimation(currentTurn.playerMove));
        _gameActivity.animate(_gameActivity._enemyPlayerGeometry,Monster.getAnimation(currentTurn.enemyMove));

        // After animation is done go back to Idle
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

    // Called when all turn processing and feedback is done
    private void nextTurn()
    {
        _currentTurn++;
        Turn newTurn = new Turn(_currentTurn);
        _turns.add(newTurn);

        // Turns start at 0 but UI start at 1
        _gameActivity.updateTurn(_currentTurn + 1);
    }

    public void myPlayerWon()
    {
        Toast.makeText(_gameActivity, "You Won the Turn", Toast.LENGTH_SHORT).show();
        dealDamageToPlayer(_enemyPlayer); // Damage enemy
    }

    public void myPlayerLost()
    {
        Toast.makeText(_gameActivity, "You Lost the Turn", Toast.LENGTH_SHORT).show();
        dealDamageToPlayer(_myPlayer); // Damage my char
    }

    public void doTie()
    {
        Toast.makeText(_gameActivity, "It's a Tie!", Toast.LENGTH_SHORT).show();
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
                    else if(p2Move == Moves.MAGIC)
                    {
                        return _myPlayer;
                    }
                    break;
                case DEFEND:
                    if(p2Move == Moves.ATTACK)
                    {
                        return _myPlayer;
                    }
                    else if(p2Move == Moves.MAGIC)
                    {
                        return _enemyPlayer;
                    }
                    break;
                case MAGIC:
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

    public void dealDamageToPlayer(Player player)
    {
        // player always loses one life only
        player.takeDamage();
        boolean isOwner = false;

        if(player == _myPlayer)
            isOwner = true;

        _gameActivity.updateHPView(isOwner, player.getCurrentLifes());
    }

    // In case of single player, a random attack replaces the enemies attack
    public void doBotAttack()
    {
        Random rand = new Random();
        int  random = rand.nextInt(3) + 1;
        switch (random)
        {
            case 1:
                addEnemyMove(Moves.ATTACK);
                break;
            case 2:
                addEnemyMove(Moves.DEFEND);
                break;
            case 3:
                addEnemyMove(Moves.MAGIC);
                break;
        }
    }

}
