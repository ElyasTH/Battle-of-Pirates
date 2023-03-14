package Game;

import java.util.Arrays;
import java.util.Random;

public class Computer extends Player {
    Random rand = new Random();
    int shoot_delay = 100;
    Tile[] targets = new Tile[5];
    int targets_count;
    Tile[] current_target_tiles = new Tile[8];
    int current_target_tiles_count;
    boolean is_targeting;
    boolean aim_bot = true; //for testing
    static boolean aim_bot_switch;

    public Computer(int size) {
        super(size,false);
    }

    public void place_ships(){
        auto_place_ships();
        this.is_placing_ships = false;
    }

    public void shoot_tile(GameBoard enemyBoard){
        //makes a small delay for computer to shoot the cannon
        if (shoot_delay > 0){
            shoot_delay--;
            return;
        }

        //if the targeted ship is destroyed reset current targets
        if (is_targeting && current_target_tiles[0].is_destroyed){
            Arrays.fill(current_target_tiles, null);
            current_target_tiles_count = 0;
            is_targeting = false;
            aim_bot = true;
        }

        //if one target is destroyed, remove that target and, shift the targets array
        while (targets[0] != null && targets[0].is_destroyed){
            for (int i = 0; i < targets_count; i++){
                targets[i] = targets[i+1];
            }
            targets_count--;
        }

        //if the computer is not targeting, switch to a new target if possible
        if (targets[0] != null && !is_targeting) {
            current_target_tiles[0] = targets[0];
            current_target_tiles_count++;
        }

        //if there's a tile remaining in current targets, set is_targeting to true
        for (Tile target: current_target_tiles){
            if (target != null) {
                if (!is_targeting) aim_bot = true;
                is_targeting = true;
                break;
            }
        }

        Tile target;
        do{
            //select a target
            target = choose_target(enemyBoard);
            if (is_targeting && current_target_tiles[0].is_destroyed){
                Arrays.fill(current_target_tiles, null);
                current_target_tiles_count = 0;
                is_targeting = false;
                aim_bot = true;
            }
        } while(target == null || target.is_hit);

        set_cannon_type(target, enemyBoard.tiles);

        cannon.shoot(target, enemyBoard);
        if (cannon.type == Cannon.cannon_types.Regular) money -= 10;
        else if (cannon.type == Cannon.cannon_types.Explosive) money -= 70;

        if (cannon.type == Cannon.cannon_types.Regular && target.is_occupied) current_target_tiles[current_target_tiles_count++] = target;
        else if (cannon.type == Cannon.cannon_types.Explosive){
            for (Tile tile: cannon.targets){
                if (tile != null && tile.is_occupied) targets[targets_count++] = tile;
            }
        }

        shoot_delay = 100;
    }

    public Tile choose_target(GameBoard enemyBoard){
        Tile selected_tile = null;
        if (!is_targeting && aim_bot_switch){
            while(selected_tile == null || !selected_tile.is_occupied){
                selected_tile = enemyBoard.select_random_tile();
            }
            aim_bot = false;
        }
        //if there is no target, select one randomly
        else if (!is_targeting){
            selected_tile = enemyBoard.select_random_tile();
        }

        //if there are targets present, select the next target based on the others
        else{
            if (current_target_tiles[1] != null && current_target_tiles[0].j == current_target_tiles[1].j){
                int max_i = -1, min_i = GameBoard.size;
                int j = current_target_tiles[0].j;

                for (Tile target: current_target_tiles){
                    if (target == null) break;
                    if (target.i > max_i) max_i = target.i;
                    if (target.i < min_i) min_i = target.i;
                }

                if (max_i+1 < GameBoard.size && !enemyBoard.tiles[max_i+1][j].is_hit) selected_tile = enemyBoard.tiles[max_i+1][j];
                else if (min_i-1 >= 0 && !enemyBoard.tiles[min_i-1][j].is_hit) selected_tile = enemyBoard.tiles[min_i-1][j];
                else if (j+1 < GameBoard.size && !enemyBoard.tiles[max_i][j+1].is_hit) selected_tile = enemyBoard.tiles[max_i][j+1];
                else if (j-1 >= 0 && !enemyBoard.tiles[max_i][j-1].is_hit) selected_tile = enemyBoard.tiles[max_i][j-1];
                else{
                    current_target_tiles[0] = current_target_tiles[current_target_tiles_count-1];
                    Arrays.fill(current_target_tiles, 1, 5, null);
                    current_target_tiles_count = 1;
                }

            }
            else if (current_target_tiles[1] != null && current_target_tiles[0].i == current_target_tiles[1].i){
                int max_j = -1, min_j = GameBoard.size;
                int i = current_target_tiles[0].i;

                for (Tile target: current_target_tiles){
                    if (target == null) break;
                    if (target.j > max_j) max_j = target.j;
                    if (target.j < min_j) min_j = target.j;
                }

                if (max_j+1 < GameBoard.size && !enemyBoard.tiles[i][max_j+1].is_hit && current_target_tiles_count < 4) selected_tile = enemyBoard.tiles[i][max_j+1];
                else if (min_j-1 >= 0 && !enemyBoard.tiles[i][min_j-1].is_hit && current_target_tiles_count < 4) selected_tile = enemyBoard.tiles[i][min_j-1];
                else if (i+1 < GameBoard.size && !enemyBoard.tiles[i+1][max_j].is_hit) selected_tile = enemyBoard.tiles[i+1][max_j];
                else if (i-1 >= 0 && !enemyBoard.tiles[i-1][max_j].is_hit) selected_tile = enemyBoard.tiles[i-1][max_j];
                else{
                    current_target_tiles[0] = current_target_tiles[current_target_tiles_count-1];
                    Arrays.fill(current_target_tiles, 1, 5, null);
                    current_target_tiles_count = 1;
                }
            }
            else if (current_target_tiles[1] == null){
                int n = rand.nextInt(4);
                int i = current_target_tiles[0].i;
                int j = current_target_tiles[0].j;

                if (i-1 >= 0 && !enemyBoard.tiles[i - 1][j].is_hit) selected_tile = enemyBoard.tiles[i - 1][j];
                else if (i+1 < GameBoard.size && !enemyBoard.tiles[i + 1][j].is_hit) selected_tile = enemyBoard.tiles[i + 1][j];
                else if (j-1 >= 0 && !enemyBoard.tiles[i][j - 1].is_hit) selected_tile = enemyBoard.tiles[i][j - 1];
                else if (j+1 < GameBoard.size && !enemyBoard.tiles[i][j + 1].is_hit) selected_tile = enemyBoard.tiles[i][j + 1];
                else{
                    switch (n) {
                        case 0: if (i-1 >= 0 && enemyBoard.tiles[i-1][j].is_occupied) {
                            current_target_tiles[0] = enemyBoard.tiles[i - 1][j];
                            break;
                        }
                        case 1: if (i+1 < GameBoard.size && enemyBoard.tiles[i+1][j].is_occupied) {
                            current_target_tiles[0] = enemyBoard.tiles[i + 1][j];
                            break;
                        }
                        case 2: if (j-1 >= 0 && enemyBoard.tiles[i][j-1].is_occupied) {
                            current_target_tiles[0] = enemyBoard.tiles[i][j - 1];
                            break;
                        }
                        case 3: if (j+1 < GameBoard.size && enemyBoard.tiles[i][j+1].is_occupied) {
                            current_target_tiles[0] = enemyBoard.tiles[i][j + 1];
                            break;
                        }
                    }
                }
            }
        }
        return selected_tile;
    }

    //set the cannon type based on target's location and money
    public void set_cannon_type(Tile target, Tile[][] enemyTiles){
        int i = target.i, j = target.j;
        int target_count = 1;

        if (i-1 >= 0 && !enemyTiles[i-1][j].is_hit) target_count++;
        if (i+1 < GameBoard.size && !enemyTiles[i+1][j].is_hit) target_count++;
        if (j-1 >= 0 && !enemyTiles[i][j-1].is_hit) target_count++;
        if (j+1 < GameBoard.size && !enemyTiles[i][j+1].is_hit) target_count++;

        if (!is_targeting && target_count >= 4 && money >= 150){
            cannon = new Cannon(Cannon.cannon_types.Explosive);
        }
        else cannon = new Cannon(Cannon.cannon_types.Regular);
    }

    public void sell_ship(){
        Random rand = new Random();
        int i;

        do{
            i = rand.nextInt(10);
        } while (ships[i] == null || ships[i].is_damaged);

        money += ships[i].sellPrice;
        ships[i].sell(this.ships);
    }

    public void update(GamePanel gamePanel) {

        if (!is_placing_ships && !gamePanel.enemy.is_placing_ships && gamePanel.getTurn() == GamePanel.getCurrentTurn() &&
                GamePanel.allow_cannon_shoot && money >= 10 && (cannon == null || !cannon.is_shot)) shoot_tile(gamePanel.enemy.gameBoard);
        //sell a ship randomly if money is lower than 10 and there is a ship available for sale
        else if (money < 10 && (cannon == null || !cannon.is_shot)){
            int undamaged_ships = 0;
            int damaged_ships = 0;
            for (Ship ship: gamePanel.self.ships){
                if (ship != null && !ship.is_damaged) undamaged_ships++;
                else if (ship != null && !ship.is_destroyed) damaged_ships++;
            }

            if (undamaged_ships > 1 || (undamaged_ships == 1 && damaged_ships >= 1)) sell_ship();
        }

        super.update(gamePanel);
    }
}
