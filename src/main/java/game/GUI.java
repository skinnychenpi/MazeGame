package game;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;


public class GUI extends JFrame implements PropertyChangeListener {
    private String localPlayer;
    private JLabel[][] mapGrids;
    private JLabel infoLabel;
    private GameState gameState;

    private void updateInfoLabel(){
        StringBuilder sb = new StringBuilder("<html> ScoreBoard <br>");
        for(Map.Entry<String,Integer> playerAndScore : this.gameState.scoreBoard.entrySet()){
            sb.append("Player : " + playerAndScore.getKey()).
                    append(" Score : ").
                    append(playerAndScore.getValue()).
                    append("<br>");
        }
        sb.append("</html>");
        infoLabel.setText(sb.toString());
    }

    private void updateMapGrids() {
        int rows = this.gameState.mazeMap.length;
        int cols = this.gameState.mazeMap.length;
        for(int i=0; i<rows; i++) {
            for(int j=0; j<cols; j++) {
                int pos = i * rows + j;
                Color backgroundColor = Color.white;
                StringBuilder cell = new StringBuilder();
                boolean hasTreasure = false;
                if(gameState.mazeMap[i][j] == '*') {
                    backgroundColor = Color.yellow;
                    hasTreasure = true;
                }
                for (Map.Entry<String, int[]> playerAndPosition : gameState.allPlayerPositions.entrySet()) {
                    if(playerAndPosition.getValue()[0] == i && playerAndPosition.getValue()[1] == j) {
                        cell.append(playerAndPosition.getKey());
                        if(playerAndPosition.getKey().equals(localPlayer)) {
                            backgroundColor = hasTreasure ? Color.red : Color.green;
                        }
                        // can break because we cannot have two players in the same cell
                        break;
                    }
                }
                mapGrids[i][j].setText(cell.toString());
                mapGrids[i][j].setBackground(backgroundColor);
            }
        }
    }

    public GUI (GameState gameState, String localPlayer) {
        setVisible(true);
        int rows = gameState.mazeMap.length;
        int cols = gameState.mazeMap.length;
        this.localPlayer = localPlayer;
        this.gameState = gameState;

        // Info
        Panel legend = new Panel(new FlowLayout());
        infoLabel = new JLabel();
        updateInfoLabel();
        infoLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        infoLabel.setSize(300, 300);
        legend.add(infoLabel);

        // Map
        Panel map = new Panel(new GridLayout(rows, cols));
        mapGrids = new JLabel[rows][cols];
        for(int i=0; i<rows; i++) {
            for (int j = 0; j < cols; j++) {
                mapGrids[i][j] = new JLabel();
                mapGrids[i][j].setOpaque(true);
                mapGrids[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
                map.add(mapGrids[i][j]);
            }
        }
        updateMapGrids();
        setLayout(new BorderLayout());
        add(map, BorderLayout.CENTER);
        add(legend, BorderLayout.WEST);
        setTitle(localPlayer);
        setSize(400, 400);
        setAlwaysOnTop(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Terminating ...");
                System.exit(0);
            }
        });
    }

    public void propertyChange(PropertyChangeEvent event) {
        gameState = (GameState) event.getNewValue();
        updateInfoLabel();
        updateMapGrids();
    }
}
