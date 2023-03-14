package Game;

import Game.Exceptions.InvalidTileException;

import java.awt.*;
import java.util.Random;

public class Player {
    int money;
    boolean is_left;
    boolean is_placing_ships = true;
    Ship[] ships = new Ship[10];
    GameBoard gameBoard;
    Cannon cannon;
    Spyglass spyglass;

    public Player(int size, boolean is_left) {
        this.is_left = is_left;
        gameBoard = new GameBoard(size,is_left);

        //set starting money based on the game board's size
        switch (GameBoard.size) {
            case 15 -> this.money = 300;
            case 16 -> this.money = 360;
            case 17 -> this.money = 420;
        }

        //adding players ships
        ships[0] = Ship.newShip(Ship.Types.Boat, gameBoard);
        ships[1] = Ship.newShip(Ship.Types.Boat, gameBoard);
        ships[2] = Ship.newShip(Ship.Types.Boat, gameBoard);
        ships[3] = Ship.newShip(Ship.Types.Boat, gameBoard);
        ships[4] = Ship.newShip(Ship.Types.Sloop, gameBoard);
        ships[5] = Ship.newShip(Ship.Types.Sloop, gameBoard);
        ships[6] = Ship.newShip(Ship.Types.Sloop, gameBoard);
        ships[7] = Ship.newShip(Ship.Types.Brigantine, gameBoard);
        ships[8] = Ship.newShip(Ship.Types.Brigantine, gameBoard);
        ships[9] = Ship.newShip(Ship.Types.Galleon, gameBoard);

        set_default_values();
    }

    private void set_default_values(){

        //set the default location for each ship inside game screen
        if (is_left){
            ships[0].set_default_position(gameBoard.tileDrawSize, 560);
            ships[1].set_default_position(gameBoard.tileDrawSize,610);
            ships[2].set_default_position(gameBoard.tileDrawSize*3,560);
            ships[3].set_default_position(gameBoard.tileDrawSize*3,610);
            ships[4].set_default_position(gameBoard.tileDrawSize*6,560);
            ships[5].set_default_position(gameBoard.tileDrawSize*8,560);
            ships[6].set_default_position(gameBoard.tileDrawSize*10,560);
            ships[7].set_default_position(gameBoard.tileDrawSize*13,560);
            ships[8].set_default_position(gameBoard.tileDrawSize*15,560);
            ships[9].set_default_position(gameBoard.tileDrawSize*18,540);
        }
        else {
            ships[0].set_default_position(gameBoard.tileDrawSize + 1500, 560);
            ships[1].set_default_position(gameBoard.tileDrawSize + 1500,610);
            ships[2].set_default_position(gameBoard.tileDrawSize*3 + 1500,560);
            ships[3].set_default_position(gameBoard.tileDrawSize*3 + 1500,610);
            ships[4].set_default_position(gameBoard.tileDrawSize*6 + 1500,560);
            ships[5].set_default_position(gameBoard.tileDrawSize*8 + 1500,560);
            ships[6].set_default_position(gameBoard.tileDrawSize*10 + 1500,560);
            ships[7].set_default_position(gameBoard.tileDrawSize*13 + 1500,560);
            ships[8].set_default_position(gameBoard.tileDrawSize*15 + 1500,560);
            ships[9].set_default_position(gameBoard.tileDrawSize*18 + 1500,540);
            }
    }

    public boolean check_ships_placed(){

        //check if all ships are placed on the game board
        for (Ship ship : ships) {
            if (ship == null) continue;
            if (!ship.is_placed_onBoard) {
                return false;
            }
        }

        //make all ships immovable
        for (Ship ship : ships) {
            if (ship == null) continue;
            ship.is_movable = false;
        }
        is_placing_ships = false;
        return true;
    }

    public void auto_place_ships(){
        //place ships on the game board automatically
        Random rand = new Random();
        for (Ship ship: ships) {
            if (ship == null) continue;
            int vertical = rand.nextInt(2);
            ship.set_vertical(vertical == 0);

            while (true) {
                try {
                    ship.place_onBoard(gameBoard.select_random_tile());
                } catch (ArrayIndexOutOfBoundsException | InvalidTileException e){
                    ship.remove_from_board();
                    continue;
                }
                break;
            }
            this.gameBoard.clear_board_color();
        }
    }

    public void shoot_tile(Cannon cannon, GameBoard enemyBoard, int mouseX, int mouseY, int cameraX, int cameraY){

        for (Tile[] row: enemyBoard.tiles) {
            for (Tile target: row) {
                //if a tile on enemy's board is selected, shoot that tile
                if (mouseX > target.getScreenX(cameraX) && mouseX < target.getScreenX(cameraX) + enemyBoard.tileDrawSize &&
                        mouseY > target.getScreenY(cameraY) && mouseY < target.getScreenY(cameraY) + enemyBoard.tileDrawSize && !target.is_hit) {

                    cannon.shoot(target, enemyBoard);
                    if (cannon.type == Cannon.cannon_types.Regular) this.money -= 10;
                    else if (cannon.type == Cannon.cannon_types.Explosive) this.money -= 70;
                    enemyBoard.clear_board_color();
                    return;
                }
            }
        }
    }

    public boolean check_ships_destroyed(){
        //check if all of player's ships are destroyed
        for (Ship ship: ships){
            if (ship == null) continue;
            if (!ship.is_destroyed) return false;
        }
        return true;
    }

    public void update(GamePanel gamePanel){

        //updating player's ships
        for (Ship ship: ships){
            if (ship == null) continue;
            ship.update(gamePanel.mouseH, gamePanel.mouseX, gamePanel.mouseY, gameBoard, gamePanel.getCameraX(), gamePanel.getCameraY());
        }

        //aiming and shooting the cannon
        if (!gamePanel.self.is_placing_ships && !gamePanel.enemy.is_placing_ships && gamePanel.getTurn() == GamePanel.getCurrentTurn() && GamePanel.allow_cannon_shoot) {
            gamePanel.enemy.gameBoard.clear_board_color();
            Tile selectedTile = gamePanel.enemy.gameBoard.selectTile(gamePanel.mouseX, gamePanel.mouseY, gamePanel.getCameraX(), gamePanel.getCameraY());

            if ((cannon == null || !cannon.is_shot) && (selectedTile == null || selectedTile.is_hit)) return;

            if (cannon != null) {

                //highlighting tiles in cannon/spyglass hit area
                if (cannon.type.equals(Cannon.cannon_types.Regular) && !cannon.is_shot && gamePanel.mouseH.is_mouse_on_screen)
                    selectedTile.color = new Color(0, 0, 0, 50);
                else if (!cannon.is_shot && gamePanel.mouseH.is_mouse_on_screen) {
                    int i = selectedTile.i, j = selectedTile.j;
                    Tile[][] tiles = gamePanel.enemy.gameBoard.tiles;
                    Color gray = new Color(0, 0, 0, 50);

                    tiles[i][j].color = gray;
                    if (i - 1 >= 0 && !tiles[i - 1][j].is_hit) tiles[i - 1][j].color = gray;
                    if (i + 1 < GameBoard.size && !tiles[i + 1][j].is_hit) tiles[i + 1][j].color = gray;
                    if (j - 1 >= 0 && !tiles[i][j - 1].is_hit) tiles[i][j - 1].color = gray;
                    if (j + 1 < GameBoard.size && !tiles[i][j + 1].is_hit) tiles[i][j + 1].color = gray;
                }

                if (gamePanel.mouseH.is_left_clicked && !cannon.is_shot) {
                    shoot_tile(cannon, gamePanel.enemy.gameBoard, gamePanel.mouseX, gamePanel.mouseY, gamePanel.getCameraX(), gamePanel.getCameraY());
                }

                if (cannon.is_shot) {
                    if (cannon.update(this)) {
                        gamePanel.switch_turn(false);
                    }
                }
            } else if (spyglass != null) {
                Tile[][] tiles = gamePanel.enemy.gameBoard.tiles;
                Color gray = new Color(0, 0, 0, 50);
                Tile[] targets = new Tile[9];

                for (int index = 0, i = selectedTile.i-1; i < selectedTile.i+2; i++){
                    for (int j = selectedTile.j-1; j < selectedTile.j+2; j++){
                        try {
                            if (!tiles[i][j].is_hit) targets[index++] = tiles[i][j];
                        } catch (ArrayIndexOutOfBoundsException e){
                            continue;
                        }
                    }
                }

                for (Tile target: targets){
                    if (target == null) continue;
                    target.color = gray;
                }

                if (gamePanel.mouseH.is_left_clicked){
                    gamePanel.enemy.gameBoard.clear_board_color();
                    spyglass.use(targets);
                    money -= 120;
                    gamePanel.switch_turn(false);
                }
            }
        }

        //check if player's money is lower than 10 and the player doesn't have more ships to sell, end game
        if (money < 10 && (cannon == null || !cannon.is_shot)){
            int damaged_ships = 0;
            int undamaged_ships = 0;
            for (Ship ship: ships){
                if (ship != null && !ship.is_damaged) undamaged_ships++;
                else if (ship != null && !ship.is_destroyed) damaged_ships++;
                if (undamaged_ships > 1 || (undamaged_ships == 1 && damaged_ships >= 1)) return;
            }
            GamePanel.end_game(this, "money");
        }
    }

    public void draw(Graphics2D g2, boolean is_enemy, int cameraX, int cameraY){

        gameBoard.draw(g2, cameraX, cameraY);

        for (Ship ship : ships) {
            if (ship != null) ship.draw(g2, cameraX, cameraY, is_enemy);
        }
    }
}
