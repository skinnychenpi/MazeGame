package game;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GameHandlerImpl extends UnicastRemoteObject implements GameHandler{

    public Game game;

    public GameHandlerImpl(Game game) throws RemoteException {
        super();
        this.game = game;
    }

    @Override
    public GameState getGameState() throws RemoteException{
        return this.game.gameState;
    }

    @Override
    public String getPrimary() throws RemoteException{
        return this.game.primaryServer;
    }

    @Override
    public String getBackup() throws RemoteException{
        return this.game.backupServer;
    }

    @Override
    public void updateGameState(GameState gameState) throws RemoteException{
        this.game.gameState = gameState;
    }

    @Override
    public synchronized void updatePrimary(String newPrimary) throws RemoteException{
        this.game.primaryServer = newPrimary;
    }

    @Override
    public synchronized void updateBackup(String newBackup) throws RemoteException{
        this.game.backupServer = newBackup;
    }

    @Override
    public synchronized void updatePrimaryAndBackup(String newPrimary, String newBackup) throws RemoteException{
        this.game.primaryServer = newPrimary;
        this.game.backupServer = newBackup;
    }

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }

    @Override
    public void deletePlayer(String playerId) throws RemoteException{
        synchronized (this.game.gameState){
            this.game.gameState.playerList.remove(playerId);
            this.game.gameState.allPlayerPositions.remove(playerId);
            this.game.gameState.scoreBoard.remove(playerId);
        }
    }

    @Override
    public boolean updateGameStateForPlayer(String playerId, int opNum) throws RemoteException{
        synchronized (this.game.gameState){
            //only primary server can run this function
            boolean res = false;
            switch (opNum){
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    res = this.game.moveForPlayer(playerId, opNum);
                    break;
                case 9:
                    break;
                default:
                    System.out.println("invalid operation Number");
                    //System.exit(0);
            }
            return res;
        }
    }

    @Override
    public void addNewPlayer(String playerId) throws RemoteException, NullPointerException{
        synchronized (this.game.gameState){
            this.game.gameState.playerList.add(playerId);
            int[] randomValidPosition = this.game.gameState.randomValidPosition();
            this.game.gameState.allPlayerPositions.put(playerId,randomValidPosition);
            this.game.gameState.scoreBoard.put(playerId,0);
        }
    }

    @Override
    public void syncBackup() throws RemoteException {
        synchronized (this.game.gameState){
            this.game.updateServerGameState(this.game.backupServer);
        }
    }
}
