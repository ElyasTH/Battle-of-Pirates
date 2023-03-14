package Game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class GameBoard {
    static int size;
    int startX, startY = 80;
    int tileDrawSize = 33;
    boolean is_left;
    Tile[][] tiles;

    public GameBoard(int size, boolean is_left) {

        if (size == 15 || size == 16 || size == 17)
            GameBoard.size = size;
        this.is_left = is_left;
        tiles = new Tile[size][size];

        switch (size) {
            case 15 -> Tile.size = 43;
            case 16 -> Tile.size = 41;
            case 17 -> Tile.size = 39;
            default -> {
                System.out.println("Invalid board size.");
                System.exit(-1);
            }
        }
        startX = tileDrawSize + 100;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {

                tiles[i][j] = new Tile(0, 0, i, j);

                if (is_left) {
                    tiles[i][j].x = i * tileDrawSize;
                }
                else{
                    tiles[i][j].x = i * tileDrawSize + 1500;
                }
                tiles[i][j].y = j * tileDrawSize - 25;
            }
        }
    }

    public void resize(){

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {

                if (is_left) {
                    tiles[i][j].x = i * tileDrawSize;
                }
                else{
                    tiles[i][j].x = i * tileDrawSize + 1500;
                }
                tiles[i][j].y = j * tileDrawSize+15;
            }
        }
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY) {

        for (Tile[] row: tiles) {
            for (Tile tile: row) {

                if (tile.is_hit && !tile.is_occupied && !tile.color.equals(new Color(0,255,0,150))) tile.color = new Color(0,0,255,150);

                g2.setColor(tile.color);
                g2.fillRect(tile.getScreenX(cameraX) , tile.getScreenY(cameraY) , tileDrawSize, tileDrawSize);

                g2.setColor(Color.WHITE);
                g2.drawRect(tile.getScreenX(cameraX) , tile.getScreenY(cameraY), tileDrawSize, tileDrawSize);

            }
        }
    }

    public Tile selectTile(int mouseX, int mouseY, int cameraX, int cameraY) {

        for (Tile[] row : tiles) {
            for (Tile tile : row) {

                if (mouseX > tile.getScreenX(cameraX)-1 && mouseX < tile.getScreenX(cameraX)+1 + tileDrawSize && mouseY > tile.getScreenY(cameraY)-1 && mouseY < tile.getScreenY(cameraY)+1 + tileDrawSize) {
                    return tile;
                }
            }
        }
        return null;
    }

    public Tile select_random_tile(){
        Random rand = new Random();

        int i = rand.nextInt(GameBoard.size);
        int j = rand.nextInt(GameBoard.size);
        return tiles[i][j];
    }


    public void clear_board_color(){

        for (Tile[] row: tiles){
            for (Tile tile: row){
                if (tile.is_hit) continue;
                tile.color = new Color(0,0,0,0);
            }
        }
    }
}

class Tile {
    int x, y;
    int i, j;
    static int size;
    int surroundings;
    boolean is_occupied;
    boolean is_detected;
    boolean is_hit;
    boolean is_destroyed;
    Color color = new Color(0,0,0,0);
    FX splash;
    FX explosion;
    static BufferedImage cross;
    static BufferedImage flag;

    static{
        try {
            cross = ImageIO.read(Tile.class.getResourceAsStream("/cross.png"));
            flag = ImageIO.read(Tile.class.getResourceAsStream("/flag.png"));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public Tile(int x, int y, int i, int j) {
        this.x = x;
        this.y = y;
        this.i = i;
        this.j = j;
    }

    public int getScreenX(int cameraX){
        return this.x - cameraX + GamePanel.screenCenterX;
    }

    public int getScreenY(int cameraY){
        return this.y - cameraY + GamePanel.screenCenterY;
    }
}
