package Game;

import javax.swing.*;
import java.awt.*;

public class GameHandler {
    static Player player1;
    static Player player2;
    static JFrame window = new JFrame();
    static JFrame window2;
    static MenuPanel menuPanel = new MenuPanel();
    static GamePanel gamePanel1;
    static GamePanel gamePanel2;

    public static void main(String[] args){

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Battle of Pirates");
        window.add(menuPanel);
        window.setLocation((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2)-(GamePanel.screenWidth/2),0);
        window.setVisible(true);
        window.pack();
        MenuPanel.menuMusic.play();
    }

    public static void startNewGame(int size, boolean isSinglePlayer){
        MenuPanel.menuMusic.stop();
        player1 = new Player(size, true);

        if (isSinglePlayer) player2 = new Computer(size);
        else player2 = new Player(size,false);

        gamePanel1 = new GamePanel(player1,player2);
        gamePanel2 = new GamePanel(player2,player1);
        window.remove(menuPanel);
        window.repaint();
        window.add(gamePanel1);
        if (!isSinglePlayer) window.setLocation(0,0);
        window.pack();

        if (!isSinglePlayer){
            window2 = new JFrame();
            window2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window2.setResizable(false);
            window2.setTitle("Battle of Pirates");
            window2.add(gamePanel2);
            window2.pack();
            window2.setLocation(570,0);
            window2.setVisible(true);
        }

        gamePanel1.startGameThread();
        gamePanel2.startGameThread();

        GamePanel.setCurrentTurn(1);
        GamePanel.allow_cannon_shoot = true;
        GamePanel.allow_camera_switch = true;
        GamePanel.winner = null;
    }

    public static void exit_game(){
        MenuPanel.menuMusic.play();
        window.remove(gamePanel1);

        if (window2 != null) {
            window2.remove(gamePanel2);
            window2.repaint();
            window2.setVisible(false);
        }
        gamePanel1.gameThread.interrupt();
        gamePanel2.gameThread.interrupt();
        gamePanel1.gameThread = null;
        gamePanel2.gameThread = null;
        gamePanel1 = null;
        gamePanel2 = null;

        window.add(menuPanel);
        window.setVisible(true);
        window.setLocation((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2)-(GamePanel.screenWidth/2),0);

        GamePanel.gameMusic.stop();
        GamePanel.isGameStarted = false;

        window.repaint();
    }
}
