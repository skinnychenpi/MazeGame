package game;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface TrackerHandler extends Remote {

    public int getN() throws RemoteException;
    public int getK() throws RemoteException;
    public String getPlayer() throws RemoteException;
    public int getPlayerListLength() throws RemoteException;
    public void initRegister(String playerId) throws RemoteException;
    public void register(String playerId) throws RemoteException;
    public void deletePlayer(String playerId) throws RemoteException;
    public void deleteInitSet(String playerId) throws RemoteException;
    public List<Object> getPlayerListInfo() throws RemoteException;
    public ConcurrentHashMap<String,Boolean> getInitSet() throws RemoteException;

}
