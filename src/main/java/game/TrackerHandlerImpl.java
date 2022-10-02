package game;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TrackerHandlerImpl extends UnicastRemoteObject implements TrackerHandler{

    public Tracker tracker;

    public TrackerHandlerImpl(Tracker tracker) throws RemoteException {
        super();
        this.tracker = tracker;
    }

    @Override
    public int getN() throws RemoteException{
        return this.tracker.N;
    }

    @Override
    public int getK() throws RemoteException{
        return this.tracker.K;
    }

    @Override
    public String getPlayer() throws RemoteException{
        return this.tracker.playerList.peekFirst();
    }



    @Override
    public int getPlayerListLength() throws RemoteException{
        synchronized (this.tracker.playerList){
            return this.tracker.playerList.size();
        }
    }

    @Override
    public List<Object> getPlayerListInfo() throws RemoteException{
        synchronized (this.tracker.playerList){
            List<Object> res = new ArrayList<>();
            res.add(this.tracker.playerList.peekFirst());
            res.add(this.tracker.playerList.size());
            return res;
        }
    }

    @Override
    public ConcurrentHashMap<String,Boolean> getInitSet() throws RemoteException{
        synchronized (this.tracker.initSet){
            return this.tracker.initSet;
        }
    }

    @Override
    public void initRegister(String playerId) throws RemoteException{
        this.tracker.initSet.put(playerId,true);
    }

    @Override
    public void register(String playerId) throws RemoteException{
        this.tracker.playerList.add(playerId);
    }

    @Override
    public void deletePlayer(String playerId) throws RemoteException{
        this.tracker.playerList.remove(playerId);
    }

    @Override
    public void deleteInitSet(String playerId) throws RemoteException{
        this.tracker.initSet.remove(playerId);
    }
}
