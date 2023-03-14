package Game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class UI {
    GamePanel gamePanel;
    boolean drawBG;
    UIButton battle = new UIButton("rect_button", 400, 20, true);
    UIButton auto = new UIButton("rect_button", 750, 400, true);
    UIButton camera_right = new UIButton("camera_right", 910, 420, false);
    UIButton camera_left = new UIButton("camera_left", 2, 420, false);
    UIButton cannon_regular = new UIButton("cannon_regular",300, 5, false);
    UIButton cannon_explosive = new UIButton("cannon_explosive",390, 5, false);
    UIButton spyglass = new UIButton("spyglass",480, 5, false);
    UIButton sell = new UIButton("rect_button", 580, 25, false);
    UIButton exit = new UIButton("exit", 870, 15, false);

    String moneyString;
    Ship selected_ship;
    static Font piratesFont;

    static BufferedImage gameOver_screen;

    static {
        try {
            piratesFont = Font.createFont(Font.TRUETYPE_FONT , UI.class.getResourceAsStream("/fonts/PiratesWriters.ttf"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    {
        battle.setText("BATTLE");
        auto.setText("Auto");

        try{
            gameOver_screen = ImageIO.read(getClass().getResourceAsStream("/UI/gameOver_screen.png"));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public UI(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public int update(MouseHandler mouseH, int mouseX, int mouseY){

        battle.update(mouseH, mouseX, mouseY);
        auto.update(mouseH, mouseX, mouseY);
        camera_right.update(mouseH, mouseX, mouseY);
        camera_left.update(mouseH, mouseX, mouseY);
        cannon_regular.update(mouseH, mouseX, mouseY);
        cannon_explosive.update(mouseH, mouseX, mouseY);
        spyglass.update(mouseH, mouseX, mouseY);
        sell.update(mouseH,mouseX,mouseY);
        exit.update(mouseH,mouseX,mouseY);
        moneyString = "$" + gamePanel.self.money;

        camera_right.setDisabled(gamePanel.getCameraView() == 1 || gamePanel.getTurn() != GamePanel.getCurrentTurn());
        camera_left.setDisabled(gamePanel.getCameraView() == 0 || gamePanel.getTurn() != GamePanel.getCurrentTurn());

        cannon_regular.setDisabled(gamePanel.self.money < 10);
        cannon_explosive.setDisabled(gamePanel.self.money < 70);
        spyglass.setDisabled(gamePanel.self.money < 120);

        if (battle.isClicked() && gamePanel.self.check_ships_placed()){
            gamePanel.setZooming(true);
            drawBG = true;
            battle.setVisible(false);
            auto.setVisible(false);
            camera_right.setVisible(true);
            camera_left.setVisible(true);
            cannon_regular.setVisible(true);
            cannon_explosive.setVisible(true);
            spyglass.setVisible(true);
            exit.setVisible(true);
            if (!(gamePanel.enemy instanceof Computer)) gamePanel.switch_turn(true);
        }

        if (auto.isClicked()){
            for (Ship ship: gamePanel.self.ships){
                if (ship != null) ship.remove_from_board();
            }
            gamePanel.self.auto_place_ships();
        }

        else if (camera_right.isClicked() && !gamePanel.self.is_placing_ships && !gamePanel.enemy.is_placing_ships &&
                gamePanel.getTurn() == GamePanel.getCurrentTurn() && GamePanel.allow_camera_switch) {
            gamePanel.switch_cameraView(1);
        }

        else if (camera_left.isClicked() && !gamePanel.self.is_placing_ships && !gamePanel.enemy.is_placing_ships &&
                gamePanel.getTurn() == GamePanel.getCurrentTurn() && GamePanel.allow_camera_switch) {
            gamePanel.switch_cameraView(0);
        }

        else if (cannon_regular.isClicked() && (gamePanel.self.cannon == null || !gamePanel.self.cannon.is_shot)){
            gamePanel.self.cannon = new Cannon(Cannon.cannon_types.Regular);
            cannon_regular.setSelected(true);
            cannon_explosive.setSelected(false);
            spyglass.setSelected(false);
        }

        else if (cannon_explosive.isClicked() && (gamePanel.self.cannon == null || !gamePanel.self.cannon.is_shot)){
            gamePanel.self.cannon = new Cannon(Cannon.cannon_types.Explosive);
            cannon_explosive.setSelected(true);
            cannon_regular.setSelected(false);
            spyglass.setSelected(false);
        }

        else if (spyglass.isClicked() && (gamePanel.self.cannon == null || !gamePanel.self.cannon.is_shot)){
            gamePanel.self.spyglass = new Spyglass();
            gamePanel.self.cannon = null;
            spyglass.setSelected(true);
            cannon_regular.setSelected(false);
            cannon_explosive.setSelected(false);
        }

        else if (exit.isClicked()){
            GameHandler.exit_game();
            return -1;
        }

        if (selected_ship != null){
            sell.setVisible(true);
            sell.setText("Sell $" + selected_ship.sellPrice);
        }

        if (sell.isClicked() && selected_ship != null){
            for (Tile tile: selected_ship.surroundingTiles){
                if (tile != null) tile.color = new Color(0,0,0,0);
            }
            selected_ship.sell(gamePanel.self.ships);
            gamePanel.self.money += selected_ship.sellPrice;
            selected_ship = null;
            sell.setSelected(false);
            sell.setSelected(false);
            sell.setSelected(false);
        }

        if (mouseH.is_left_clicked && !gamePanel.self.is_placing_ships && !gamePanel.enemy.is_placing_ships && gamePanel.getTurn() == GamePanel.getCurrentTurn()) {
            if (selected_ship != null){
                for (Tile tile: selected_ship.surroundingTiles){
                    if (tile != null) tile.color = new Color(0,0,0,0);
                }
            }
            selected_ship = null;
            sell.setVisible(false);

            int remaining_ships = 0;
            for (Ship ship: gamePanel.self.ships){
                if (ship != null && !ship.is_destroyed) remaining_ships++;
            }

            if (remaining_ships > 1) {
                for (Ship ship : gamePanel.self.ships) {
                    if (ship == null) continue;
                    if (!ship.is_damaged && mouseX > ship.getScreenX(gamePanel.getCameraX()) && mouseX < ship.getScreenX(gamePanel.getCameraX()) + ship.sprite.getWidth()
                            && mouseY > ship.getScreenY(gamePanel.getCameraY()) && mouseY < ship.getScreenY(gamePanel.getCameraY()) + ship.sprite.getHeight()) {
                        selected_ship = ship;
                        gamePanel.self.gameBoard.clear_board_color();
                        for (Tile tile : selected_ship.surroundingTiles)
                            if (tile != null) tile.color = new Color(0, 255, 0, 150);
                        Ship.take.play();
                        break;
                    }
                }
            }
        }

        if (mouseX > 45 && mouseX < 255 && mouseY > 15 && mouseY < 80 && mouseH.is_left_clicked) {
            Computer.aim_bot_switch = !Computer.aim_bot_switch;
        }

//            g2.fillRoundRect(45, 15, 210, 65, 30, 30);
        battle.unclick();
        auto.unclick();
        camera_right.unclick();
        camera_left.unclick();
        cannon_regular.unclick();
        cannon_explosive.unclick();
        spyglass.unclick();
        sell.unclick();
        exit.unclick();

        return 0;
    }

    public void draw(Graphics2D g2){
        battle.draw(g2);
        auto.draw(g2);

        if (!GamePanel.isGameStarted) return;

        if (drawBG) {
            g2.setColor(new Color(187, 129, 65));
            g2.fillRect(0, 0, 960, 95);
            g2.setColor(new Color(101, 77, 44));
            g2.fillRect(0, 95, 960, 5);
            g2.fillRoundRect(45, 15, 210, 65, 30, 30);
            g2.setFont(piratesFont.deriveFont(45f));
            g2.setColor(new Color(255,191,0));
            g2.drawString(moneyString, 155 - moneyString.length()*10, 65);

            g2.setFont(piratesFont.deriveFont(35f));
            if (GamePanel.getCurrentTurn() == gamePanel.getTurn()){
                g2.setColor(new Color(0, 255, 0));
                g2.drawString("You're", 770,45);
                g2.drawString("Turn", 780,80);
            }
            else{
                g2.setColor(new Color(178,18,18));
                g2.drawString("Enemy's", 765,45);
                g2.drawString("Turn", 775,80);
            }
        }
        camera_right.draw(g2);
        camera_left.draw(g2);
        cannon_regular.draw(g2);
        cannon_explosive.draw(g2);
        spyglass.draw(g2);
        sell.draw(g2);
        exit.draw(g2);

        if (GamePanel.winner != null){
            g2.drawImage(gameOver_screen, 150, 200, null);
            g2.setColor(new Color(81,52,26));

            if (GamePanel.winner == gamePanel.self && GamePanel.end_reason.equals("ship")){
                g2.setFont(piratesFont.deriveFont(78f));
                g2.drawString("All Enemy Ships", 260, 380);
                g2.drawString("Destroyed!", 350, 480);
                g2.drawString("YOU WIN!", 365, 615);
            }
            else if (GamePanel.winner == gamePanel.self){
                g2.setFont(piratesFont.deriveFont(76f));
                g2.drawString("Enemy Has Run", 280, 360);
                g2.drawString("Out of Money!", 295, 465);
                g2.drawString("YOU WIN!", 365, 615);
            }
            else if (GamePanel.winner == gamePanel.enemy && GamePanel.end_reason.equals("ship")){
                g2.setFont(piratesFont.deriveFont(66f));
                g2.drawString("All of Your Ships", 290, 380);
                g2.drawString("Were Destroyed!", 315, 480);
                g2.drawString("YOU LOSE!", 360, 615);
            }
            else if (GamePanel.winner == gamePanel.enemy){
                g2.setFont(piratesFont.deriveFont(76f));
                g2.drawString("You Have Run", 280, 360);
                g2.drawString("Out of Money!", 295, 465);
                g2.drawString("YOU LOSE!", 360, 615);
            }
        }
    }

}

class UIButton {
    private BufferedImage sprite;
    private BufferedImage sprite_underMouse;
    private BufferedImage sprite_selected;
    private BufferedImage sprite_disabled;
    private String text;
    private final int x,y;
    private boolean underMouse;
    private boolean clicked;
    private boolean selected;
    private boolean disabled;
    private boolean visible;
    static final Sound selectSound = new Sound("buttonSelect", false, Sound.SoundTypes.SoundEffect);
    static final Sound clickSound = new Sound("buttonClick", false, Sound.SoundTypes.SoundEffect);

    public UIButton(String filename, int x, int y, boolean is_visible) {
        this.x = x;
        this.y = y;
        this.visible = is_visible;

        try{
            sprite = ImageIO.read(getClass().getResourceAsStream("/UI/Buttons/" + filename + ".png"));
            sprite_underMouse = ImageIO.read(getClass().getResourceAsStream("/UI/Buttons/" + filename + ".png"));
            sprite_selected = ImageIO.read(getClass().getResourceAsStream("/UI/Buttons/" + filename + ".png"));
            sprite_disabled = ImageIO.read(getClass().getResourceAsStream("/UI/Buttons/" + filename + ".png"));
        } catch (IOException e){
            return;
        }
        tint(sprite_underMouse, new Color(150,150,150));
        tint(sprite_selected, new Color(0, 150, 50));
        tint(sprite_disabled, new Color(0,0,0));
    }

    public void tint(BufferedImage image, Color color) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);
                int r = (pixelColor.getRed() + color.getRed()) / 2;
                int g = (pixelColor.getGreen() + color.getGreen()) / 2;
                int b = (pixelColor.getBlue() + color.getBlue()) / 2;
                int a = pixelColor.getAlpha();
                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgba);
            }
        }
    }

    public void update(MouseHandler mouseH, int mouseX, int mouseY){
        if (!visible || selected || disabled) return;

        if (mouseX > this.x && mouseX < this.x + sprite.getWidth() && mouseY > this.y && mouseY < this.y + sprite.getHeight()){
            if (mouseH.is_left_clicked){
                clicked = true;
                selectSound.play();
            }
            if (!underMouse) {
                underMouse = true;
                clickSound.play();
            }
        }
        else underMouse = false;

    }

    public void draw(Graphics2D g2){
        if (!visible) return;

        if (selected) g2.drawImage(this.sprite_selected,this.x,this.y,null);
        else if (disabled) g2.drawImage(this.sprite_disabled,this.x,this.y,null);
        else if (underMouse) g2.drawImage(this.sprite_underMouse,this.x,this.y,null);
        else g2.drawImage(this.sprite,this.x,this.y,null);

        if (this.text != null){
            g2.setFont(UI.piratesFont.deriveFont(40f));
            g2.setColor(new Color(82,53,20));
            g2.drawString(text, x+(80-text.length()*7), y+35);
        }
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void unclick(){
        this.clicked = false;
    }

    public boolean isClicked() {
        return clicked;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isSelected() {
        return selected;
    }
}
