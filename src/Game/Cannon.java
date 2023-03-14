package Game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

public class Cannon {

    enum cannon_types{
        Regular,
        Explosive;
    }

    cannon_types type;
    BufferedImage sprite;
    Tile mainTarget;
    Tile[] targets = new Tile[5];
    int x,y;
    int speed = 30;
    int direction;
    boolean is_moving;
    boolean is_shot;
    static final Sound shoot = new Sound("cannonShoot", false, Sound.SoundTypes.SoundEffect);
    static final Sound hit = new Sound("cannonHit", false, Sound.SoundTypes.SoundEffect);
    static final Sound miss = new Sound("cannonMiss", false, Sound.SoundTypes.SoundEffect);

    public Cannon(cannon_types type) {
        this.type = type;

        try {
            if (type.equals(cannon_types.Regular)) sprite = ImageIO.read(getClass().getResourceAsStream("/cannonball_regular.png"));
            else sprite = ImageIO.read(getClass().getResourceAsStream("/cannonball_explosive.png"));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void shoot(Tile mainTarget, GameBoard gameBoard){
        this.mainTarget = mainTarget;
        this.x = 1300;
        this.y = mainTarget.y + 4;
        GamePanel.allow_camera_switch = false;

        is_moving = true;
        is_shot = true;

        if (mainTarget.x > this.x) direction = 1;
        else direction = -1;

        Arrays.fill(targets, null);
        targets[0] = mainTarget;

        if (type.equals(cannon_types.Explosive)){
            if (mainTarget.i - 1 >= 0 && !gameBoard.tiles[mainTarget.i - 1][mainTarget.j].is_hit) targets[1] = gameBoard.tiles[mainTarget.i - 1][mainTarget.j];
            if (mainTarget.i + 1 < GameBoard.size && !gameBoard.tiles[mainTarget.i + 1][mainTarget.j].is_hit) targets[2] = gameBoard.tiles[mainTarget.i + 1][mainTarget.j];
            if (mainTarget.j - 1 >= 0 && !gameBoard.tiles[mainTarget.i][mainTarget.j - 1].is_hit) targets[3] = gameBoard.tiles[mainTarget.i][mainTarget.j - 1];
            if (mainTarget.j + 1 < GameBoard.size && !gameBoard.tiles[mainTarget.i][mainTarget.j + 1].is_hit) targets[4] = gameBoard.tiles[mainTarget.i][mainTarget.j + 1];
        }

        for (Tile target: targets){
            if (target == null) continue;
            if (!target.is_occupied)target.splash = new FX("splash", 12, 2,target);
            else target.explosion = new FX("explosion", 9, 3,target);
        }

        shoot.play();
    }

    public boolean update(Player player){
        if ((this.x > targets[0].x + Tile.size || this.x < targets[0].x) && is_moving) {
            this.x += direction * speed;
        }
        else if (is_moving){
            is_moving = false;
            boolean playHit = false;
            for (Tile target: targets) {
                if (target == null) continue;
                if (target.is_occupied) {
                    playHit = true;
                    target.explosion.play();
                }
                else target.splash.play();
            }
            if (playHit) hit.play();
            if (!playHit || type == cannon_types.Explosive) miss.play();
        }

        boolean switchTurn_flag = false;
        if (!is_moving){
            for (Tile target: targets){
                if (target == null) continue;
                if (target.explosion != null && target.explosion.is_playing) target.explosion.update();
                else if (target.splash != null && target.splash.is_playing) target.splash.update();
                else if (!target.is_hit){
                    target.is_hit = true;
                    target.is_detected = false;
                    target.explosion = null;
                    target.splash = null;
                    is_shot = false;
                    GamePanel.allow_camera_switch = true;
                    switchTurn_flag = true;
                    if (target.is_occupied) player.money += 45;
                }
            }

            if (switchTurn_flag) {
                if (this.type == cannon_types.Explosive || !this.mainTarget.is_occupied) {
                    return true;
                } else return false;
            }
        }
        return false;
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY){
        int screenX = this.x - cameraX + GamePanel.screenCenterX;
        int screenY = this.y - cameraY + GamePanel.screenCenterY;

        if (is_moving) g2.drawImage(sprite, screenX, screenY, 30,30,null);
        else{
            for (Tile target: targets){
                if (target == null) continue;
                if (target.splash != null && target.splash.is_playing) target.splash.draw(g2, cameraX, cameraY);
                if (target.explosion != null && target.explosion.is_playing) target.explosion.draw(g2, cameraX, cameraY);
            }
        }

    }
}
