package game;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

public class PingThread extends Thread{
    public Game game;

    public PingThread(Game game){
        this.game = game;
    }

    public String getRandomPlayer(){
        int size = this.game.gameState.playerList.size();
        Random r = new Random();
        String randomPlayer = this.game.gameState.playerList.get(r.nextInt(size));
        return randomPlayer;
    }


    public boolean ping(String playerId){
        Registry registry = null;
        GameHandler gameHandler = null;
        try {
            registry = LocateRegistry.getRegistry(this.game.tracker[0],Integer.parseInt(this.game.tracker[1]));
            gameHandler = (GameHandler) registry.lookup(playerId);
            return gameHandler.ping();
        } catch (NotBoundException | RemoteException e) {
            //System.out.println("ping failed : " + playerId);
            return false;
        }
    }

    public void handleFail(String type, String failServer) throws MalformedURLException, NotBoundException, RemoteException {
        Registry registry = LocateRegistry.getRegistry(this.game.tracker[0],Integer.parseInt(this.game.tracker[1]));
        TrackerHandler trackerHandler = null;
        trackerHandler = (TrackerHandler) registry.lookup("Tracker");

        synchronized (registry.lookup(this.game.localPlayer.playerId)){

        System.out.println("[" + java.time.LocalTime.now() + "] " +"fail type : " + type);
        System.out.println("[" + java.time.LocalTime.now() + "] " +"fail server : " + failServer);
        System.out.println("[" + java.time.LocalTime.now() + "] " +"Before My primary : " + this.game.primaryServer);
        System.out.println("[" + java.time.LocalTime.now() + "] " +"Before My backup : " + this.game.backupServer);
        /*
         * fail type : primary
         * fail server : P2
         * Before My primary : P2
         * Before My backup : P3
         * After My primary : P3
         * After My backup : P2
         */


          /*
          PingThread start...
          fail type : primary
          fail server : P1
          Before My primary : P2
          Before My backup : P3
          bug !!!!!!!
          fail type : random
          fail server : P1
          Before My primary : P2
          Before My backup : P3
          After My primary : P2
          After My backup : P3
          */


        if(type.equals("primary") && !this.game.primaryServer.equals(failServer)){
            System.out.println("[" + java.time.LocalTime.now() + "] " +"primary fail bug !!!!!!!");
            return;
        }
        if(type.equals("backup") && !this.game.backupServer.equals(failServer)){
            System.out.println("[" + java.time.LocalTime.now() + "] " +"backup fail bug !!!!!!!");
            return;
        }
        if(type.equals("random") &&
                (this.game.primaryServer.equals(failServer) || this.game.backupServer.equals(failServer))
        ){
            System.out.println("[" + java.time.LocalTime.now() + "] " +"random fail bug !!!!!!!");
            return;
        }

        //if fail is from primary server, contact your backup server
        if(type.equals("primary")){
            GameHandler backupHandler = null;
            try {
                backupHandler = (GameHandler) registry.lookup(this.game.backupServer);
                String backup_primary = backupHandler.getPrimary();//backup's primary
                String thisGame_primary = this.game.primaryServer;//this game's primary

                if(backup_primary.equals(thisGame_primary)){
                    //YOU ARE THE FIRST ONE THAT FIND THE CRASH
                    //update both yourself and the new primary server(former backup server)
                    backupHandler.updatePrimaryAndBackup(this.game.backupServer,this.game.localPlayer.playerId);
                    this.game.primaryServer = this.game.backupServer;
                    this.game.backupServer = this.game.localPlayer.playerId;
                }
                else {
                    //YOU ARE NOT THE FIRST ONE
                    String newPrimary = backupHandler.getPrimary();
                    String newBackup = backupHandler.getBackup();
                    if(newPrimary.equals(newBackup)){
                        // if backup server is the first one, you become new backup
                        backupHandler.updateBackup(this.game.localPlayer.playerId);
                        this.game.primaryServer = this.game.backupServer;
                        this.game.backupServer = this.game.localPlayer.playerId;
                    }
                    else{
                        this.game.primaryServer = newPrimary;
                        this.game.backupServer = newBackup;
                    }

                }
            } catch (NotBoundException | RemoteException e) {
                System.out.println("[" + java.time.LocalTime.now() + "] " +"regenerate primary fail through backup : " + this.game.backupServer);
            }
            trackerHandler.deletePlayer(failServer);
        }

        //if fail is from backup server, contact your primary server
        else if(type.equals("backup")){
            GameHandler primaryHandler = null;
            try {
                primaryHandler = (GameHandler) registry.lookup(this.game.primaryServer);
                String primary_backup = primaryHandler.getBackup();//primary's backup
                String thisGame_backup = this.game.backupServer;//this game's backup
                if(primary_backup.equals(thisGame_backup)){
                    //YOU ARE THE FIRST ONE THAT FIND THE CRASH
                    //update both yourself and the primary server
                    primaryHandler.updateBackup(this.game.localPlayer.playerId);
                    this.game.backupServer = this.game.localPlayer.playerId;
                    //new backup should synchronize the latest game state with primary server
                    this.game.retrieveFromServer(this.game.primaryServer);
                }
                else {
                    //YOU ARE NOT THE FIRST ONE
                    if(primary_backup.equals(this.game.primaryServer)){
                        //if primary server is the first one, you become the new backup
                        primaryHandler.updateBackup(this.game.localPlayer.playerId);
                        this.game.backupServer = this.game.localPlayer.playerId;
                    }
                    else
                        this.game.backupServer = primaryHandler.getBackup();
                }
            } catch (NotBoundException | RemoteException e) {
                System.out.println("[" + java.time.LocalTime.now() + "] " +"regenerate backup fail through primary : " + this.game.primaryServer);
            }
            trackerHandler.deletePlayer(failServer);
        }
        //TODO : ELSE CLAUSE CAN BE DELETED ?

        //if fail is from random client, delete it globally and locally
        else {
            GameHandler primaryHandler = null;
            GameHandler backupHandler = null;

            try {
                trackerHandler.deletePlayer(failServer);

                primaryHandler = (GameHandler) registry.lookup(this.game.primaryServer);
                backupHandler = (GameHandler) registry.lookup(this.game.backupServer);

                primaryHandler.deletePlayer(failServer);
                backupHandler.deletePlayer(failServer);
                this.game.gameHandler.deletePlayer(failServer);


            }catch (NotBoundException | RemoteException e){
                System.out.println("[" + java.time.LocalTime.now() + "] " +"delete random dead player fail");
            }
        }
        System.out.println("[" + java.time.LocalTime.now() + "] " +"After My primary : " + this.game.primaryServer);
        System.out.println("[" + java.time.LocalTime.now() + "] " +"After My backup : " + this.game.backupServer);
        }
        //this.game.retrieveFromServer(this.game.primaryServer);
    }

    public void run(){
        //RUN THE thread IN THE FOLLOWING STEPS every 1.5 seconds:
        //1. PING PRIMARY server and handle fail if needed
        //2. Ping backup server and handle fail if needed
        //3. ping some random player and handle fail if needed
        System.out.println("[" + java.time.LocalTime.now() + "] " +"PingThread start...");
        while (true)
        {
            System.out.println("[" + java.time.LocalTime.now() + "] " + " New ping round start : ");
            String primaryServer = this.game.primaryServer;
            if(!this.ping(primaryServer)){
                //PRIMARY REGENERATE
                try {
                    handleFail("primary",primaryServer);
                } catch (MalformedURLException | NotBoundException | RemoteException e) {
                    e.printStackTrace();
                }
            }

            String backupServer = this.game.backupServer;
            if(!this.ping(backupServer)){
                //BACKUP REGENERATE
                try {
                    handleFail("backup",backupServer);
                } catch (MalformedURLException | NotBoundException | RemoteException e) {
                    e.printStackTrace();
                }
            }
            String randomPlayer;
            do{
                randomPlayer  = this.getRandomPlayer();
            }while (randomPlayer.equals(primaryServer) || randomPlayer.equals(backupServer));

            if(!this.ping(randomPlayer)){
                //DEAL WITH NORMAL PING FAIL
                try {
                    handleFail("random",randomPlayer);
                } catch (MalformedURLException | NotBoundException | RemoteException e) {
                    e.printStackTrace();
                }
            }
            try{
                System.out.println("[" + java.time.LocalTime.now() + "] " + " This ping round end, sleep");
                Thread.sleep(500);//sleep for 1100 ms
            } catch (InterruptedException ex){
                //TODO : DO WHAT ?
            }
        }
    }
}
