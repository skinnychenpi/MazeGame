package game;
import java.io.Serializable;
import java.util.*;

public class GameState implements Serializable {

    private static final long serialVersionUID = 42L;

    public char[][] mazeMap;
    public List<String> playerList;
    public Map<String,Integer> scoreBoard;
    public Map<String,int[]> allPlayerPositions;

    public GameState(int N, int K){
        this.mazeMap = new char[N][N];
        //Firstly, fill with 'o' (not treasure)
        for(char[] row : mazeMap)
            Arrays.fill(row,'o');
        //Secondly, randomly generate K treasures
        for(int i = 0; i < K; i++){
            Random r = new Random();
            int newX = 0, newY = 0;
            while(mazeMap[newX][newY] != 'o'){
                newX = r.nextInt(mazeMap.length);
                newY = r.nextInt(mazeMap.length);
            }
            this.mazeMap[newX][newY] = '*';
        }
        this.playerList = new ArrayList<>();
        this.scoreBoard = new HashMap<>();
        this.allPlayerPositions = new HashMap<>();
    }

    public boolean removeAndGenerate(int x, int y){
        //remove treasure that is picked up by a player and generate a new treasure
        Random r = new Random();
        int newX = x, newY = y;
        while(mazeMap[newX][newY] != 'o'){
            newX = r.nextInt(mazeMap.length);
            newY = r.nextInt(mazeMap.length);
        }
        this.mazeMap[x][y] = 'o';
        this.mazeMap[newX][newY] = '*';
        return true;
    }

    public boolean valid(int x, int y){
        //out of map!
        if(!(x >= 0 && x < mazeMap.length && y >= 0 && y < mazeMap.length))
            return false;
        else{
            for(int[] position : this.allPlayerPositions.values()){
                if(x == position[0] && y == position[1])
                    return false;// two player in same position
            }
            return true;
        }
    }

    public int[] randomValidPosition(){
        Random r = new Random();
        int newX = r.nextInt(mazeMap.length);
        int newY = r.nextInt(mazeMap.length);
        while(!valid(newX,newY) || mazeMap[newX][newY] == '*'){
            newX = r.nextInt(mazeMap.length);
            newY = r.nextInt(mazeMap.length);
        }
        return new int[]{newX,newY};
    }
}
