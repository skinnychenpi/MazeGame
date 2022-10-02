package game;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Tracker {
    public int N;
    public int K;
    public ConcurrentHashMap<String,Boolean> initSet;
    public ConcurrentLinkedDeque<String> playerList;
    public TrackerHandler trackerHandler;


    public Tracker(int port, int N, int K) throws RemoteException {
        this.N = N;
        this.K = K;
        this.initSet = new ConcurrentHashMap<>();
        this.playerList = new ConcurrentLinkedDeque<>();
        this.trackerHandler = new TrackerHandlerImpl(this);
    }


    public static void main(String[] args) throws RemoteException {

        int port = Integer.parseInt(args[0]);
        int N = Integer.parseInt(args[1]);
        int K = Integer.parseInt(args[2]);

        Tracker tracker = new Tracker(port,N,K);
        Registry registry = null;
        try {
            registry = LocateRegistry.createRegistry(port);
            registry.rebind("Tracker", tracker.trackerHandler);
            System.out.println("[" + java.time.LocalTime.now() + "] " +" Tracker is ready at port " + port);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
