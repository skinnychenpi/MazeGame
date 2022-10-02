package game;

import java.beans.PropertyChangeSupport;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


public class Game {

    public GameHandler gameHandler;
    public String[] tracker; //[0] : IP ADDRESS; [1] : PORT NUMBER
    public Player localPlayer;
    public String primaryServer; //[0] : IP ADDRESS; [1] : PORT NUMBER
    public String backupServer;//[0] : IP ADDRESS; [1] : PORT NUMBER
    public GameState gameState;
    public GUI gui;
    public PropertyChangeSupport observable;

    public Game(String playerId, String trackerIP, int trackerPort) throws RemoteException {
        this.gameHandler = new GameHandlerImpl(this);
        this.localPlayer = new Player(playerId) ;
        this.tracker = new String[]{trackerIP,String.valueOf(trackerPort)};
        Registry registry = LocateRegistry.getRegistry(trackerIP,trackerPort);
        registry.rebind(playerId, this.gameHandler);
    }

    public void initializeGui(GameState gameState, String localPlayer){
        this.gui = new GUI(gameState, localPlayer);
        this.observable = new PropertyChangeSupport(this);
        this.observable.addPropertyChangeListener(gui);
    }

//    public boolean initialize(String firstPlayer, int playerListLength, int N, int K) {
//        //TODO : PLAYER LIST LENGTH MIGHT BE STALE !!!!!!
//        //Use this function when a game just start
//        //To initialize the gamestate and server info and update your local info to the server
//        if(playerListLength == 1){
//            //if you are the first player
//            //you create a new gamestate, add your info to gamestate
//            this.gameState = new GameState(N,K);
//
//            this.gameState.playerList.add(this.localPlayer.playerId);
//            this.localPlayer.position = this.gameState.randomValidPosition();
//            this.gameState.allPlayerPositions.put(this.localPlayer.playerId,this.localPlayer.position);
//            this.gameState.scoreBoard.put(this.localPlayer.playerId,this.localPlayer.score);
//
//            //set server info, because you're the first, you become both primary and backup server
//            this.primaryServer = this.localPlayer.playerId;
//            this.backupServer = this.localPlayer.playerId;
//
//            System.out.println("Initialize 1 succeed");
//        }
//        else if(playerListLength == 2){
//            //if you are the second
//            //firstly, set server info and tell the primary server that you will become the backup
//            this.primaryServer = firstPlayer;
//            this.backupServer = this.localPlayer.playerId;
//            this.updateServerSetting(this.primaryServer, this.primaryServer,this.backupServer);
//
//            //retrieve the latest game state and merge your gamestate to primary server's game state
//            GameHandler primaryGameHandler = null;
//            Registry registry = null;
//            try {
//                registry = LocateRegistry.getRegistry(this.tracker[0],Integer.parseInt(this.tracker[1]));
//                primaryGameHandler = (GameHandler) registry.lookup(this.primaryServer);
//                primaryGameHandler.addNewPlayer(this.localPlayer.playerId);
//                this.retrieveFromServer(this.primaryServer);
//                System.out.println("Initialize 2 succeed");
//            } catch (NotBoundException | RemoteException | NullPointerException e) {
//                System.out.println(e);
//                System.out.println("cannot add you as a new player");
//                return false;
//            }
//        }
//        else{
//            //if you are just some random player that do not have to become server
//            //set the primary and backup with the info that you get from the first player in tracker's playerlist
//            String[] servers = this.initializeServerSetting(firstPlayer);
//            this.primaryServer = servers[0];
//            this.backupServer = servers[1];
//
//            //retrieve and merge your gamestate to primary server's game state
//            GameHandler primaryGameHandler = null;
//            Registry registry = null;
//            try {
//                registry = LocateRegistry.getRegistry(this.tracker[0],Integer.parseInt(this.tracker[1]));
//                primaryGameHandler = (GameHandler) registry.lookup(this.primaryServer);
//                primaryGameHandler.addNewPlayer(this.localPlayer.playerId);
//                this.retrieveFromServer(this.primaryServer);
//                System.out.println("Initialize more than 2 succeed");
//                //TODO : SHOULD I ALSO ADD THIS INFO TO BACKUP SERVER ?
//            } catch (NotBoundException | RemoteException | NullPointerException e) {
//                System.out.println("cannot add you as a new player");
//                return false;
//            }
//        }
//        return true;
//    }

    public boolean initialize(String firstPlayer, int playerListLength, int N, int K) {
        //TODO : PLAYER LIST LENGTH MIGHT BE STALE !!!!!!
        //Use this function when a game just start
        //To initialize the gamestate and server info and update your local info to the server
        if(firstPlayer.equals(this.localPlayer.playerId)){
            //if you are the first player
            //you create a new gamestate, add your info to gamestate
            this.gameState = new GameState(N,K);

            this.gameState.playerList.add(this.localPlayer.playerId);
            this.localPlayer.position = this.gameState.randomValidPosition();
            this.gameState.allPlayerPositions.put(this.localPlayer.playerId,this.localPlayer.position);
            this.gameState.scoreBoard.put(this.localPlayer.playerId,this.localPlayer.score);

            //set server info, because you're the first, you become both primary and backup server
            this.primaryServer = this.localPlayer.playerId;
            this.backupServer = this.localPlayer.playerId;

            System.out.println("[" + java.time.LocalTime.now() + "] " + "Initialize 1 succeed");
        }
        else{
            //if you are just some random player that do not have to become server
            //set the primary and backup with the info that you get from the first player in tracker's playerlist
            String[] servers = this.initializeServerSetting(firstPlayer);

            if(servers[0] == null) return false;

            //TODO : ANY BUG ?
            if(servers[0].equals(servers[1])){
                //if you are the second
                //firstly, set server info and tell the primary server that you will become the backup
                this.primaryServer = firstPlayer;
                this.backupServer = this.localPlayer.playerId;
                this.updateServerSetting(this.primaryServer, this.primaryServer,this.backupServer);

                //retrieve the latest game state and merge your gamestate to primary server's game state
                GameHandler primaryGameHandler = null;
                Registry registry = null;
                try {
                    registry = LocateRegistry.getRegistry(this.tracker[0],Integer.parseInt(this.tracker[1]));
                    primaryGameHandler = (GameHandler) registry.lookup(this.primaryServer);
                    primaryGameHandler.addNewPlayer(this.localPlayer.playerId);
                    this.retrieveFromServer(this.primaryServer);
                    System.out.println("[" + java.time.LocalTime.now() + "] " +"Initialize 2 succeed");
                } catch (NotBoundException | RemoteException | NullPointerException e) {
                    e.printStackTrace();
                    System.out.println("[" + java.time.LocalTime.now() + "] " +"Initialize 2 cannot add you as a new player");
                    return false;
                }
            }

            else{
                this.primaryServer = servers[0];
                this.backupServer = servers[1];


                //retrieve and merge your gamestate to primary server's game state
                GameHandler primaryGameHandler = null;
                Registry registry = null;
                try {
                    registry = LocateRegistry.getRegistry(this.tracker[0],Integer.parseInt(this.tracker[1]));
                    primaryGameHandler = (GameHandler) registry.lookup(this.primaryServer);
                    System.out.println("[" + java.time.LocalTime.now() + "] " +"start add new player...");
                    primaryGameHandler.addNewPlayer(this.localPlayer.playerId);
                    primaryGameHandler.syncBackup();
                    this.retrieveFromServer(this.primaryServer);
                    System.out.println("[" + java.time.LocalTime.now() + "] " +"Initialize more than 2 succeed");
                    //TODO : SHOULD I ALSO ADD THIS INFO TO BACKUP SERVER ?
                } catch (NotBoundException | RemoteException | NullPointerException e) {
                    e.printStackTrace();
                    System.out.println("[" + java.time.LocalTime.now() + "] " +"Initialize more than 2 cannot add you as a new player");
                    return false;
                }
            }
        }
        return true;
    }

    public boolean initializeV2(String firstPlayer, int N, int K) {
        //TODO : write more comments
        //Use this function when a game just start
        //To initialize the gamestate and server info and update your local info to the server
        if(firstPlayer.equals(this.localPlayer.playerId)){
            //first player alive is you, you become primary
            //you create a new gamestate, add your info to gamestate
            this.gameState = new GameState(N,K);

            this.gameState.playerList.add(this.localPlayer.playerId);
            this.localPlayer.position = this.gameState.randomValidPosition();
            this.gameState.allPlayerPositions.put(this.localPlayer.playerId,this.localPlayer.position);
            this.gameState.scoreBoard.put(this.localPlayer.playerId,this.localPlayer.score);

            //set server info, because you're the first, you become both primary and backup server
            this.primaryServer = this.localPlayer.playerId;
            this.backupServer = this.localPlayer.playerId;

            System.out.println("[" + java.time.LocalTime.now() + "] " + "InitializeV2 1 succeed");
            return true;
        }
        else{
            return false;
        }
    }

    public boolean printMap(){
        //print all player and treasure in terminal
        for(int i = 0; i < gameState.mazeMap.length; i++){
            for(int j = 0; j < gameState.mazeMap.length; j++){
                if(i == this.localPlayer.position[0] && j == this.localPlayer.position[1])
                    System.out.print("▲" + "   ");
                else{
                    boolean otherPlayerIsThere = false;
                    for(int[] position : this.gameState.allPlayerPositions.values()){
                        if(i == position[0] && j == position[1]){
                            otherPlayerIsThere = true;
                            break;
                        }
                    }
                    if(otherPlayerIsThere)
                        System.out.print("▽" + "   ");
                    else
                        System.out.print(gameState.mazeMap[i][j] + "   ");
                }

            }
            System.out.println();
        }
        System.out.println("position: " + this.localPlayer.position[0] + "," + this.localPlayer.position[1]);
        for(Map.Entry playerAndScore : this.gameState.scoreBoard.entrySet())
            System.out.println("Player: " + playerAndScore.getKey() + "  Score: " + playerAndScore.getValue());
        return true;
    }

    public List<Object> retrieveFromTracker(TrackerHandler trackerHandler, String playerId) throws RemoteException {

        //retrieve basic game info from tracker

        List<Object> res = new ArrayList<>();

        res.add(trackerHandler.getN());
        res.add(trackerHandler.getK());

        List<Object> playerListInfo = new ArrayList<>(trackerHandler.getPlayerListInfo());
        res.add(playerListInfo.get(0));
        res.add(playerListInfo.get(1));

        ConcurrentHashMap<String,Boolean> initSet = new ConcurrentHashMap<>(trackerHandler.getInitSet());
        res.add(initSet);

        return res;
    }

    public boolean retrieveFromServer(String primaryServer){

        //retrieve latest game state from server
        GameHandler primaryGameHandler = null;
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(this.tracker[0],Integer.parseInt(this.tracker[1]));
            primaryGameHandler = (GameHandler) registry.lookup(primaryServer);
            this.gameState = primaryGameHandler.getGameState();
            return true;
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
            System.out.println("[" + java.time.LocalTime.now() + "] " +"cannot retrieve game state from server : " + primaryServer);
            return false;
            //TODO : DEAL WITH CONNECTION EXCEPTION
        }

    }

    public boolean updateServerGameState(String playerId) {
        //only primary server can run this function
        //update the playerId's game state with local game state
        GameHandler gameHandler = null;
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(this.tracker[0],Integer.parseInt(this.tracker[1]));
            gameHandler = (GameHandler) registry.lookup(playerId);
            gameHandler.updateGameState(this.gameState);
            return true;
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
            System.out.println("[" + java.time.LocalTime.now() + "] " +"cannot update server " + playerId + " game state");
            return false;
            //TODO : DEAL WITH CONNECTION EXCEPTION
        }
    }

    public String[] initializeServerSetting(String playerId){
        //get the playerId's primary and backup server
        GameHandler gameHandler = null;
        Registry registry = null;
        String[] res = new String[2];
        try {
            registry = LocateRegistry.getRegistry(this.tracker[0],Integer.parseInt(this.tracker[1]));
            gameHandler = (GameHandler) registry.lookup(playerId);
            String primaryServer = gameHandler.getPrimary();
            String backupServer = gameHandler.getBackup();

            res[0] = primaryServer;
            res[1] = backupServer;
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
            System.out.println("[" + java.time.LocalTime.now() + "] " +"cannot initialize Primary And Backup through player : " + playerId);
        }
        return res;
    }

    public void updateServerSetting(String playerId, String newPrimary, String newBackup){
        //tell the randomServer, who is the new primary and new backup
        GameHandler gameHandler = null;
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(this.tracker[0],Integer.parseInt(this.tracker[1]));
            gameHandler = (GameHandler) registry.lookup(playerId);
            gameHandler.updatePrimaryAndBackup(newPrimary,newBackup);
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
            System.out.println("[" + java.time.LocalTime.now() + "] " +"cannot update Primary And Backup Server Setting to server : " + playerId);
        }
    }


    public boolean moveForPlayer(String playerId, int opNum){
        //only primary server can run this function
        int[] playerPosition = this.gameState.allPlayerPositions.get(playerId);
        int playerScore = this.gameState.scoreBoard.get(playerId);
        switch (opNum){
            case 0:
                break;
            case 1:
                if(this.gameState.valid(playerPosition[0], playerPosition[1]  - 1))
                    playerPosition[1]--;
                else{
                    System.out.println("invalid move for player : " + playerId);
                    return false;
                }
                break;
            case 2:
                if(this.gameState.valid(playerPosition[0] + 1, playerPosition[1]))
                    playerPosition[0]++;
                else{
                    System.out.println("invalid move for player : " + playerId);
                    return false;
                }
                break;
            case 3:
                if(this.gameState.valid(playerPosition[0], playerPosition[1]  + 1))
                    playerPosition[1]++;
                else{
                    System.out.println("invalid move for player : " + playerId);
                    return false;
                }
                break;
            case 4:
                if(this.gameState.valid(playerPosition[0] - 1, playerPosition[1]))
                    playerPosition[0]--;
                else{
                    System.out.println("invalid move for player : " + playerId);
                    return false;
                }
                break;
        }
        if(this.gameState.mazeMap[playerPosition[0]][playerPosition[1]] == '*') {
            //if you find treasure
            this.gameState.scoreBoard.put(playerId,playerScore+1);
            this.gameState.removeAndGenerate(playerPosition[0],playerPosition[1]);
        }
        // update your local gamestate after your move
        this.gameState.allPlayerPositions.put(playerId,playerPosition);
        //primary server is responsible for synchronizing the latest game state with backup server
        this.updateServerGameState(this.backupServer);
        //only invalid move returns false, even though cannot connect to backup, still return true
        return true;
    }

    public boolean move(int opNum) {
        boolean res = false;

        GameHandler primaryGameHandler = null;
        Registry registry = null;
        String primaryServer = this.primaryServer;
        try {
            registry = LocateRegistry.getRegistry(this.tracker[0],Integer.parseInt(this.tracker[1]));
            primaryGameHandler = (GameHandler) registry.lookup(primaryServer);
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
            System.out.println("[" + java.time.LocalTime.now() + "] " +"cannot connect primary server" + primaryServer + "to move");
            return false;
            //TODO : DEAL WITH CONNECTION EXCEPTION
        }
        switch (opNum){
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                //if cannot connect -> invalid move
                try{
                    res = primaryGameHandler.updateGameStateForPlayer(this.localPlayer.playerId,opNum);
                    res = res && retrieveFromServer(primaryServer);
                    this.localPlayer.position = this.gameState.allPlayerPositions.get(this.localPlayer.playerId);
                    this.localPlayer.score = this.gameState.scoreBoard.get(this.localPlayer.playerId);
                }
                catch (RemoteException e){
                    e.printStackTrace();
                    System.out.println("[" + java.time.LocalTime.now() + "] " +
                            "Primary :" + primaryServer + " crashed, stay , just retrieve game state from backup " + this.backupServer);
                    retrieveFromServer(this.backupServer);
                    this.localPlayer.position = this.gameState.allPlayerPositions.get(this.localPlayer.playerId);
                    this.localPlayer.score = this.gameState.scoreBoard.get(this.localPlayer.playerId);
                }
                //TODO : THIS PART SHOULD SYNCHRONIZE
                break;
            case 9:
                break;
            default:
                System.out.println("invalid operation number");
        }
        if(res == false)
            System.out.println("---------------invalid move---------------");
        else
            System.out.println("[" + java.time.LocalTime.now() + "] " +"Primary server: " + primaryServer + " successfully move for player " + this.localPlayer.playerId);
        return res;
    }


    public static void main(String[] args) throws RemoteException {

        System.out.println("[" + java.time.LocalTime.now() + "] " +"Game start...");

        //initialization
        String[] tracker = {args[0],args[1]}; // tracker IP, tracker port
        String playerId = args[2]; // player ID

        Game game = new Game(playerId, tracker[0], Integer.parseInt(tracker[1]));

        Registry registry = null;
        TrackerHandler trackerHandler = null;
        try {
            System.out.println("[" + java.time.LocalTime.now() + "] " +"start connecting tracker...");
            registry = LocateRegistry.getRegistry(tracker[0], Integer.parseInt(tracker[1]));
            trackerHandler = (TrackerHandler) registry.lookup("Tracker");
            // register yourself to tracker initSet and playerList
            trackerHandler.register(playerId);
            trackerHandler.initRegister(playerId);
        }
        catch (NotBoundException | RemoteException e) {
            System.out.println("[" + java.time.LocalTime.now() + "] " +"cannot connect to tracker !");
        }


        boolean initialized = false;
        do {
            System.out.println("[" + java.time.LocalTime.now() + "] " +"start initializing...");
            //retrieve basic game info from tracker
            List<Object> trackerInfo = game.retrieveFromTracker(trackerHandler, playerId); // also register this player
            int N = (int) trackerInfo.get(0);
            int K = (int) trackerInfo.get(1);
            String firstPlayer = (String) trackerInfo.get(2);
            int playerListLength = (int) trackerInfo.get(3);
            ConcurrentHashMap<String, Boolean> initSet = (ConcurrentHashMap<String, Boolean>) trackerInfo.get(4);

            if(!initSet.containsKey(firstPlayer)){
                //normal situation : firstPlayer is not initializing
                initialized = game.initialize(firstPlayer, playerListLength, N, K);
                if(!initialized){
                    trackerHandler.deletePlayer(firstPlayer);
                    System.out.println("[" + java.time.LocalTime.now() + "] " +"Initialize unsuccessful through player : " + firstPlayer);
                    continue;
                }
                else
                    trackerHandler.deleteInitSet(game.localPlayer.playerId);
            }
            else {
                //initialize V2
                initialized = game.initializeV2(firstPlayer,N,K);
                if(!initialized){
                    try {
                        GameHandler gameHandler = (GameHandler) registry.lookup(firstPlayer);
                        gameHandler.ping();
                    }catch (NotBoundException | RemoteException e){
                        trackerHandler.deleteInitSet(firstPlayer);
                        System.out.println("[" + java.time.LocalTime.now() + "] " +" InitializeV2 unsuccessful through DEAD player in initSet: " + firstPlayer);
                    }
                    System.out.println("[" + java.time.LocalTime.now() + "] " +" Wait...InitializeV2 unsuccessful through player : " + firstPlayer);
                    continue;
                }
                else {
                    trackerHandler.deleteInitSet(game.localPlayer.playerId);
                    System.out.println("[" + java.time.LocalTime.now() + "] " +"InitializeV2 successful through yourself : " + firstPlayer);
                }
            }

            //initialize game state and set primary/backup servers
            //TODO : WHAT IF NO ACTIVE PLAYER IN TRACKER'S PLAYERLIST
        if(initialized){

            game.initializeGui(game.gameState, playerId);
            System.out.println("[" + java.time.LocalTime.now() + "] " +"Initialize successful with player : " + firstPlayer);
            System.out.println("[" + java.time.LocalTime.now() + "] " +"Initialize result : " + "Primary : " + game.primaryServer + " Backup : " + game.backupServer);
        }
        }while(!initialized);







        new BeforeEnd(trackerHandler,game.localPlayer.playerId);

        //START PING THREAD
        PingThread pingThread = new PingThread(game);
        pingThread.start();


        Scanner scanner = new Scanner(System.in);
        //game.printMap();
        System.out.println("input operation(0- >stay | 1 ->left | 2 ->down | 3 ->right | 4 ->up | 9 ->exit) : ");



        //operation in loop
        while(scanner.hasNextInt()){
            int opNum = scanner.nextInt();
            if(opNum == 9) break;
            game.move(opNum);
            game.observable.firePropertyChange("gameState", null, game.gameState);
            //game.printMap();
            System.out.println("input operation(0- >stay | 1 ->left | 2 ->down | 3 ->right | 4 ->up | 9 ->exit) : ");
        }
        //trackerHandler.deletePlayer(game.localPlayer.playerId);
        System.out.println("Exit...");
        System.exit(0);
    }
}

