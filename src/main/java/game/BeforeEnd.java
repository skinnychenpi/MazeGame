package game;

import java.rmi.RemoteException;

public class BeforeEnd {
    BeforeEnd(TrackerHandler trackerHandler, String playerId) {
        Thread t = new Thread(() -> {
            try {
                trackerHandler.deletePlayer(playerId);
                System.out.println("Before end, delete yourself : " + playerId + " , from tracker...");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
        Runtime.getRuntime().addShutdownHook(t);
    }
}
