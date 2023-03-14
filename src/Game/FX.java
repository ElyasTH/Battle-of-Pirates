package Game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FX{
    BufferedImage[] frames;
    private String filename;
    private int totalFrames;
    private int frameCount = 1;
    private int frameDelay;
    private int delayCounter;
    private int currentFrame;
    boolean is_playing;
    private Tile tile;

    public FX(String filename, int totalFrames, int frameDelay, Tile tile) {
        this.filename = filename;
        this.totalFrames = totalFrames;
        this.frameDelay = frameDelay;
        this.tile = tile;
        frames = new BufferedImage[totalFrames];

        for (int i = 0; i < totalFrames; i++, frameCount++){

            try {
                frames[i] = ImageIO.read(getClass().getResourceAsStream("/" + filename + "/" + frameCount + ".png"));
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void play(){
        currentFrame = 0;
        is_playing = true;
    }

    public void update(){
        delayCounter++;

        if (currentFrame < totalFrames && delayCounter > frameDelay){
            currentFrame++;
            delayCounter = 0;
        }
        else if (currentFrame == totalFrames) is_playing = false;
    }

    public void draw(Graphics2D g2, int cameraX, int cameraY){
        if (currentFrame < totalFrames && is_playing)
            g2.drawImage(frames[currentFrame], tile.getScreenX(cameraX)-(frames[currentFrame].getWidth()-15-Tile.size)/2,
                    tile.getScreenY(cameraY)-(frames[currentFrame].getWidth()-15-Tile.size)/2,
                    frames[0].getWidth()-15, frames[0].getHeight()-15 ,null);
    }

}
