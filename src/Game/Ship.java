package Game;

import Game.Exceptions.InvalidTileException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

public class Ship {
    Types type;
    int tilesH;
    int tilesV;
    int width;
    int height;
    Tile[] occupiedTiles;
    Tile[] surroundingTiles;
    final int sellPrice;
    final int buyPrice;
    final GameBoard gameBoard;
    int defaultX, defaultY;
    int x, y;
    Tile mainTile;
    BufferedImage sprite;
    boolean is_onBoard;
    boolean is_placed_onBoard;
    boolean is_selected;
    static boolean is_ship_selected;
    boolean is_movable = true;
    boolean is_vertical = true;
    boolean is_damaged;
    boolean is_destroyed;
    static final Sound take = new Sound("takeShip", false, Sound.SoundTypes.SoundEffect);
    static final Sound place = new Sound("placeShip", false, Sound.SoundTypes.SoundEffect);
    static final Sound sell = new Sound("sellShip", false, Sound.SoundTypes.SoundEffect);
    static final Sound destroyed = new Sound("shipDestroyed", false, Sound.SoundTypes.SoundEffect);

    enum Types{
        Boat,
        Sloop,
        Brigantine,
        Galleon
    }

    Ship(Types type, GameBoard gameBoard, int tilesH, int tilesV, int sellPrice, int buyPrice) {
        this.type = type;
        this.gameBoard = gameBoard;
        this.tilesH = tilesH;
        this.tilesV = tilesV;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        occupiedTiles = new Tile[tilesH * tilesV];

        switch (type) {
            case Boat -> surroundingTiles = new Tile[8];
            case Sloop -> surroundingTiles = new Tile[10];
            case Brigantine -> surroundingTiles = new Tile[12];
            case Galleon -> surroundingTiles = new Tile[16];
        }

        //load ship's sprite
        try{
            this.sprite = ImageIO.read(getClass().getResourceAsStream("/ships/" + this.type + ".png"));
        } catch (IOException e){
            e.printStackTrace();
        }

        this.width = (int) (this.sprite.getWidth()/(55.0/Tile.size));
        this.height = (int) (this.sprite.getHeight()/(55.0/Tile.size));
    }

    public static Ship newShip (Types type, GameBoard gameBoard){

        //set ship's value and size on board, based on it's type
        return switch (type) {
            case Boat -> new Ship(type, gameBoard ,1, 1, 50, 60);
            case Sloop -> new Ship(type, gameBoard,1, 2, 80, 100);
            case Brigantine -> new Ship(type, gameBoard,1, 3, 120, 170);
            case Galleon -> new Ship(type, gameBoard,2, 4, 250, 260);
        };
    }

    public void set_default_position(int x, int y) {

        this.x = x;
        this.y = y;
        this.defaultX = x;
        this.defaultY = y;
    }

    //getting ships location based on camera's current location
    public int getScreenX(int cameraX){
        return this.x - cameraX + GamePanel.screenCenterX;
    }

    public int getScreenY(int cameraY){
        return this.y - cameraY + GamePanel.screenCenterY;
    }

    public void place_onBoard(Tile mainTile) {
        InvalidTileException invalidTileException = new InvalidTileException();
        if (mainTile == null) throw invalidTileException;

        boolean flag = false;

        //check if the selected location for the ship is valid
        for (int i = mainTile.i, tileIndex = 0; i < mainTile.i + this.tilesH; i++) {
            for (int j = mainTile.j; j < mainTile.j + this.tilesV; j++, tileIndex++) {

                try {
                    if (gameBoard.tiles[i][j].is_occupied){
                        remove_from_board();
                        throw invalidTileException;
                    }
                    occupiedTiles[tileIndex] = gameBoard.tiles[i][j];
                    gameBoard.tiles[i][j].is_occupied = true;
                }catch (ArrayIndexOutOfBoundsException e) {
                    remove_from_board();
                    throw invalidTileException;
                }
            }
        }

        this.mainTile = mainTile;
        if (is_vertical){
            this.x = mainTile.x + (gameBoard.tileDrawSize*this.tilesH - this.width + 3)/2 ;
            this.y = mainTile.y;
        }
        else{
            this.x = mainTile.x + (gameBoard.tileDrawSize*this.tilesH - this.height)/2;
            this.y = mainTile.y + (gameBoard.tileDrawSize*this.tilesV - this.width)/2;
        }

        surroundingTiles = get_surrounding_tiles();

        //check if any of the surrounding tiles is occupied
        for (Tile tile: surroundingTiles){
            if (tile == null) break;
            if (tile.is_occupied){
                flag = true;
            }
            tile.color = new Color(0,255,0,150);
        }

        if (flag){
            for (Tile tile: surroundingTiles){
                if (tile == null) break;
                tile.color = new Color(255,0,0,150);
            }
            remove_from_board();
            throw  invalidTileException;
        }

        is_placed_onBoard = true;
    }

    public void remove_from_board(){

        for (Tile tile: this.occupiedTiles){
            if (tile == null) continue;
            tile.is_occupied = false;
        }

        for (Tile tile : this.surroundingTiles) {
            if (tile != null) {
                tile.surroundings--;
                if (tile.surroundings < 0) tile.surroundings = 0;
            }
        }

        Arrays.fill(surroundingTiles, null);
        Arrays.fill(occupiedTiles, null);
    }

    public Tile[] get_surrounding_tiles(){
        Tile[] output = new Tile[16];
        Tile firstTile, lastTile = null;
        firstTile = occupiedTiles[0];

        for (Tile tile: occupiedTiles){
            if (tile != null) lastTile = tile;
        }
        if (lastTile == null) return null;

        for (int i = firstTile.i-1, index = 0; i <= lastTile.i+1; i++){
            if (i < 0 || i >= GameBoard.size) continue;

            thisLoop: for (int j = firstTile.j-1; j <= lastTile.j+1; j++){
                if (j < 0 || j >= GameBoard.size) continue;

                for (Tile tile: occupiedTiles){
                    if (tile == gameBoard.tiles[i][j]) continue thisLoop;
                }

                output[index] = gameBoard.tiles[i][j];
                index++;
            }
        }
        return output;
    }

    public void sell(Ship[] playerShips){
        remove_from_board();
        //remove the ship from player's ships
        for (int i = 0; i < playerShips.length; i++){
            if (this == playerShips[i]) playerShips[i] = null;
        }
        sell.play();
    }

    private BufferedImage rotate_sprite(BufferedImage sprite, int angle){
        final double rads = Math.toRadians(angle);
        final double sin = Math.abs(Math.sin(rads));
        final double cos = Math.abs(Math.cos(rads));
        final int w = (int) Math.floor(sprite.getWidth() * cos + sprite.getHeight() * sin);
        final int h = (int) Math.floor(sprite.getHeight() * cos + sprite.getWidth() * sin);
        final BufferedImage rotatedImage = new BufferedImage(w, h, sprite.getType());
        final AffineTransform at = new AffineTransform();
        at.translate(w / 2.0, h / 2.0);
        at.rotate(rads,0, 0);
        at.translate(-sprite.getWidth() / 2.0, -sprite.getHeight() / 2.0);
        final AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        rotateOp.filter(sprite,rotatedImage);
        return rotatedImage;
    }

    public void set_vertical(boolean is_vertical){

        if (this.is_vertical == is_vertical) return;

        //set the ship vertical or horizontal
        if (is_vertical) {
            remove_from_board();
            this.sprite = rotate_sprite(sprite, 90);
            this.is_vertical = true;
        } else{
            remove_from_board();
            this.sprite = rotate_sprite(sprite, -90);
            this.is_vertical = false;
        }

        int temp = tilesH;
        tilesH = tilesV;
        tilesV = temp;
    }

    public void update(MouseHandler mouse, int mouseX, int mouseY, GameBoard gameBoard, int cameraX, int cameraY) {

        if (is_movable) {

            //set ships location to mouse if it's not on the game board
            if (this.is_selected && !is_onBoard) {

                this.x = mouseX - this.width / 2 + cameraX - GamePanel.screenCenterX;
                this.y = mouseY - this.height / 2 + cameraY - GamePanel.screenCenterY;
            }

            //set ships location to default if it's not selected and not on the game board
            if (!is_selected && !is_onBoard && !is_placed_onBoard) {
                set_default_position(defaultX, defaultY);
                set_vertical(true);
            }

            //select the ship
            if (mouse.is_left_clicked && !is_ship_selected && mouseX > this.getScreenX(cameraX) && mouseX < this.getScreenX(cameraX) + sprite.getWidth()
                    && mouseY > this.getScreenY(cameraY) && mouseY < this.getScreenY(cameraY) + sprite.getHeight() && !is_selected) {

                is_selected = true;
                is_ship_selected = true;
                mouse.is_left_clicked = false;
                take.play();
            }

            //place the ship
            if (is_selected && mouse.is_left_clicked) {
                is_selected = false;
                is_ship_selected = false;
                mouse.is_left_clicked = false;
                place.play();
            }

            //place the ship on game board
            if (is_selected && mouseX > gameBoard.startX && mouseX < gameBoard.startX + gameBoard.tileDrawSize * gameBoard.size &&
                    mouseY > gameBoard.startY && mouseY < gameBoard.startY + gameBoard.tileDrawSize * gameBoard.size) {

                Tile currentTile = gameBoard.selectTile(mouseX, mouseY, cameraX, cameraY);

                if (is_placed_onBoard) remove_from_board();

                gameBoard.clear_board_color();

                try {
                    place_onBoard(currentTile);
                } catch (InvalidTileException e) {};

                is_onBoard = true;
            } else if (is_selected) {
                is_onBoard = false;
                gameBoard.clear_board_color();
            }

            if (is_onBoard && !is_selected) {
                Tile selectedTile = gameBoard.selectTile(mouseX, mouseY, cameraX, cameraY);

                try{
                    this.place_onBoard(selectedTile);
                    is_onBoard = false;
                    gameBoard.clear_board_color();
                }catch (InvalidTileException e){
                    is_selected = true;
                    is_ship_selected = true;
                }
            }

            //take the ship from the game board
            if (is_placed_onBoard && is_selected) {
                is_placed_onBoard = false;
                remove_from_board();
            }

            //rotate the ship if right-clicked
            if (is_selected && mouse.is_right_clicked) {

                try {
                    place_onBoard(gameBoard.selectTile(mouseX, mouseY, cameraX, cameraY));
                    set_vertical(!this.is_vertical);
                } catch (InvalidTileException e) {}

                mouse.is_right_clicked = false;
            }
        }

        //set the ship's location on game board
        if (is_vertical) {
            this.width = (int) (this.sprite.getWidth() / (55.0 / gameBoard.tileDrawSize));
            this.height = (int) (this.sprite.getHeight() / (55.0 / gameBoard.tileDrawSize));
            if (is_placed_onBoard) {
                this.x = mainTile.x + (gameBoard.tileDrawSize * this.tilesH - this.width + 3) / 2;
                this.y = mainTile.y;
            }
        }
        else {
            this.height = (int) (this.sprite.getWidth() / (55.0 / gameBoard.tileDrawSize));
            this.width = (int) (this.sprite.getHeight() / (55.0 / gameBoard.tileDrawSize));
            if (is_placed_onBoard) {
                this.x = mainTile.x + (gameBoard.tileDrawSize * this.tilesH - this.height) / 2;
                this.y = mainTile.y + (gameBoard.tileDrawSize * this.tilesV - this.width) / 2;
            }
        }

        //destroy the ship, if all of it's occupied tiles are hit
        if (is_placed_onBoard && !is_destroyed) {
            for (Tile tile : occupiedTiles) {
                if (tile == null || !tile.is_hit) return;
            }

            boolean flag = false; //to check if explosion FX is finished
            for (Tile tile: occupiedTiles){
                if(tile.explosion == null) {
                    tile.explosion = new FX("explosion", 9, 3,tile);
                    tile.explosion.play();
                    tile.is_destroyed = true;
                    destroyed.play();
                    flag = true;
                }
                else if (tile.explosion.is_playing){
                    tile.explosion.update();
                    GamePanel.allow_cannon_shoot = false;
                    flag = true;
                }
            }
            if (flag) return;


            for (Tile tile: surroundingTiles){
                if (tile == null) continue;
                tile.is_hit = true;
            }

            is_destroyed = true;
            GamePanel.allow_cannon_shoot = true;

            //load ship's destroyed sprite
            try {
                sprite = ImageIO.read(getClass().getResourceAsStream("/ships/" + this.type + "_destroyed.png"));
            } catch (IOException e){
                e.printStackTrace();
            }
            if (!is_vertical){
               sprite = rotate_sprite(sprite,-90);
            }
        }
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY, boolean is_enemy) {

        //draw the ship
        if (!is_enemy || is_destroyed) {
            if (is_vertical) g2.drawImage(sprite, this.getScreenX(cameraX), this.getScreenY(cameraY), width, height, null);
            else g2.drawImage(sprite, this.getScreenX(cameraX), this.getScreenY(cameraY), height, width, null);
        }

        //draw cross and flag on hit and detected tiles
        if (is_placed_onBoard) {
            for (Tile tile : occupiedTiles) {
                if (tile != null && tile.is_hit && !is_destroyed) {
                    is_damaged = true;
                    g2.drawImage(Tile.cross, tile.getScreenX(cameraX)+5, tile.getScreenY(cameraY)+5, Tile.size-10,Tile.size-10,null);
                }
                else if (tile != null && tile.is_detected){
                    g2.drawImage(Tile.flag, tile.getScreenX(cameraX)+5, tile.getScreenY(cameraY)+5, Tile.size-10,Tile.size-10,null);
                }
            }
        }

        //draw the explosion FX
        for (Tile tile: occupiedTiles){
            if (tile != null && tile.explosion != null && tile.explosion.is_playing){
                tile.explosion.draw(g2,cameraX,cameraY);
            }
        }
    }
}
