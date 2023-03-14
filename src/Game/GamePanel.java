package Game;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {

    static final int screenWidth = 960;
    static final int screenHeight = 800;
    static final int screenCenterX = screenWidth/2;
    static final int screenCenterY = screenHeight/2;
    static final int FPS = 60;
    private int cameraX;
    private int cameraY = GamePanel.screenCenterY - 110;
    private int cameraView;
    private int auto_camera_switch_delay = 70;
    private static int currentTurn;
    private int turn;
    private boolean zooming;
    private boolean auto_camera_switch;static boolean allow_camera_switch = true;
    static boolean allow_cannon_shoot = true;
    static final Sound gameMusic = new Sound("PirateOrchestra", true, Sound.SoundTypes.Music);
    static final Sound gameOver = new Sound("DrunkenSailor", false, Sound.SoundTypes.Music);
    static boolean isGameStarted;
    static graphicsPreset graphics = graphicsPreset.Rare;

    public enum graphicsPreset{
        Cursed,
        Common,
        Rare
    }

    MouseHandler mouseH = new MouseHandler();
    UI ui = new UI(this);

    Thread gameThread;

    Player self;
    Player enemy;

    static Player winner;
    static String end_reason;
    int mouseX, mouseY;

    public GamePanel(Player self, Player enemy){
        this.self = self;
        this.enemy = enemy;

        //set the location of each player's game board
        if (self.is_left){
            cameraX = GamePanel.screenCenterX - Tile.size - 100;
            cameraView = 0;
            turn = 1;
        }
        else{
            cameraX = GamePanel.screenCenterX - Tile.size + 1400;
            cameraView = 1;
            turn = 2;
        }

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(new Color(77,112,255));
        this.setDoubleBuffered(true);
        this.addMouseListener(mouseH);
        this.setFocusable(true);

        //set the game board of the computer in single player
        if (self instanceof Computer computer){
            computer.place_ships();
            zooming = true;
        }
    }

    public void startGameThread(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run(){

        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while(gameThread != null){

            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            //rendering the game each frame
            if (delta >= 1){
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update(){

        //getting mouse location on screen
        if (!(self instanceof Computer)) {
            try {
                mouseX = MouseInfo.getPointerInfo().getLocation().x - this.getLocationOnScreen().x;
                mouseY = MouseInfo.getPointerInfo().getLocation().y - this.getLocationOnScreen().y;
            } catch (IllegalComponentStateException e) {
                return;
            }
        }

        //return if exit button is clicked
        if (ui.update(mouseH, mouseX, mouseY) == -1) return;

        //while there is no winner, continue updating the game
        if (winner == null) {
            self.update(this);

            //if the game is in multiplayer, update the enemy object
            if (!(self instanceof Computer) && !(enemy instanceof Computer)) {
                if (this == GameHandler.gamePanel1) enemy.update(GameHandler.gamePanel2);
                else enemy.update(GameHandler.gamePanel1);
            }

            //if auto camera switch delay is finished, switch the camera view
            if (auto_camera_switch) {
                if (auto_camera_switch_delay > 0) auto_camera_switch_delay--;
                else {
                    switch_cameraView(-1);
                    auto_camera_switch_delay = 70;
                    auto_camera_switch = false;
                }
            }
            //for moving the camera
            update_camera();

            //if all ships of a player are destroyed, end game
            if (self.check_ships_destroyed()) GamePanel.end_game(self, "ship");
            else if (enemy.check_ships_destroyed()) GamePanel.end_game(enemy, "ship");
        }

        mouseH.reset_keys();

        //checks and handles zooming
        if (isZooming() && self.gameBoard.tileDrawSize != Tile.size) zoom(1);
        else if (self.gameBoard.tileDrawSize == Tile.size && isZooming()){
            setZooming(false);
            switch_window();
        }

        //checks and handles window switching
        if (turn != currentTurn && !zooming){
            if ((turn == 1 && cameraX == GamePanel.screenCenterX - Tile.size - 100) ||
                (turn == 2 && cameraX == GamePanel.screenCenterX - Tile.size + 1400)) switch_window();
        }

        //if both players have placed their ships, start the game
        if (!isGameStarted && !self.is_placing_ships && !enemy.is_placing_ships && !MenuPanel.menuMusic.isPlaying()){
            isGameStarted = true;
            gameMusic.play();
        }
    }
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        //graphics quality settings
        switch (graphics){
            case Cursed -> {g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);}

            case Common -> {g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_DEFAULT);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);}

            case Rare -> {g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);}
        }

        self.draw(g2, false, cameraX, cameraY);
        enemy.draw(g2, true, cameraX, cameraY);

        //drawing when fired
        if (self.cannon != null && self.cannon.is_shot) self.cannon.draw(g2,cameraX,cameraY);
        if (enemy.cannon != null && enemy.cannon.is_shot) enemy.cannon.draw(g2,cameraX,cameraY);

        ui.draw(g2);
        g2.dispose();
    }

    public void zoom(int i){
        self.gameBoard.tileDrawSize += i;
        self.gameBoard.resize();
    }

    public void switch_cameraView(int i){
        //for auto camera switch
        if (i == -1){
            if (cameraView == 0) cameraView = 1;
            else cameraView = 0;
        }
        //for manual camera switch
        else if (i != cameraView) cameraView = i;
    }

    public void update_camera(){
        if (cameraView == 0 && cameraX > GamePanel.screenCenterX - Tile.size - 100) cameraX -= 40;
        else if (cameraView == 1 && cameraX < GamePanel.screenCenterX - Tile.size + 1400) cameraX += 40;
        else if (cameraView == 0 && cameraX < GamePanel.screenCenterX - Tile.size - 100) cameraX = GamePanel.screenCenterX - Tile.size - 100;
        else if (cameraView == 1 && cameraX > GamePanel.screenCenterX - Tile.size + 1400) cameraX = GamePanel.screenCenterX - Tile.size + 1400;
    }

    public void switch_turn(boolean flag){
        if (currentTurn == 1) currentTurn = 2;
        else currentTurn = 1;

        if (!flag) {
            ui.cannon_regular.setSelected(false);
            ui.cannon_explosive.setSelected(false);
            ui.spyglass.setSelected(false);
            self.cannon = null;
            self.spyglass = null;
            auto_camera_switch = true;
        }
    }

    public void switch_window(){
        if (enemy instanceof Computer || self instanceof Computer || winner != null) return;
        if (turn == 1){
            GameHandler.window.setVisible(false);
            GameHandler.window2.setVisible(true);
        }
        else{
            GameHandler.window.setVisible(true);
            GameHandler.window2.setVisible(false);
        }
    }

    public static void end_game(Player loser, String reason){
        if (loser == GameHandler.player1) winner = GameHandler.player2;
        else if (loser == GameHandler.player2) winner = GameHandler.player1;
        GamePanel.end_reason = reason;
        GameHandler.window.setVisible(true);
        if (GameHandler.window2 != null) GameHandler.window2.setVisible(true);
        gameMusic.stop();
        gameOver.play();
    }

    public int getCameraX() {
        return cameraX;
    }

    public int getCameraY() {
        return cameraY;
    }

    public int getCameraView() {
        return this.cameraView;
    }

    public static int getCurrentTurn() {
        return currentTurn;
    }

    public static void setCurrentTurn(int currentTurn) {
        GamePanel.currentTurn = currentTurn;
    }

    public int getTurn() {
        return turn;
    }

    public boolean isZooming() {
        return zooming;
    }

    public void setZooming(boolean zooming) {
        this.zooming = zooming;
    }

    public void setCameraView(int i){
        this.cameraView = i;
    }
}
