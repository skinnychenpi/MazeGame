package game;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameHandler extends Remote {
    public GameState getGameState() throws RemoteException;
    public String getPrimary() throws RemoteException;
    public String getBackup() throws RemoteException;
    public void updatePrimaryAndBackup(String newPrimary, String newBackup) throws RemoteException;
    public void updateGameState(GameState gameState) throws RemoteException;
    public void updatePrimary(String primaryServer) throws RemoteException;
    public void updateBackup(String backupServer) throws RemoteException;
    public boolean ping() throws RemoteException;
    public void deletePlayer(String playerId) throws RemoteException;
    public boolean updateGameStateForPlayer(String playerId, int opNum) throws RemoteException;
    public void addNewPlayer(String playerId) throws RemoteException,NullPointerException;
    public void syncBackup() throws RemoteException;

}
