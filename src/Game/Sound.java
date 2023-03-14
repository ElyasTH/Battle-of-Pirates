package Game;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class Sound {
    private Clip clip;
    private long clipTime;
    private final boolean loop;
    private static double musicVolume = 1;
    private static double seVolume = 1;
    private SoundTypes type;

    public enum SoundTypes{
        SoundEffect,
        Music
    }

    public Sound(String fileName, boolean loop, SoundTypes type){
        this.loop = loop;
        this.type = type;

        try{
            AudioInputStream ais = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/" + fileName + ".wav"));
            clip = AudioSystem.getClip();
            clip.open(ais);
        } catch (Exception e){
            e.printStackTrace();
        }

        setVolume();
    }

    public void play(){
        clip.setMicrosecondPosition(0);
        clip.start();
        if (loop) clip.loop(clip.LOOP_CONTINUOUSLY);
    }

    public void stop(){
        clip.stop();
    }

    public void pause(){
        clipTime = clip.getMicrosecondPosition();
        clip.stop();
    }

    public void resume(){
        clip.setMicrosecondPosition(clipTime);
        this.play();
    }

    public boolean isPlaying(){
        return clip.isRunning();
    }

    public void setVolume(){
        double volume;
        if (type == SoundTypes.Music) volume = musicVolume;
        else volume = seVolume;
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
        gainControl.setValue(dB);
    }

    public static void setMusicVolume(double musicVolume) {
        Sound.musicVolume = musicVolume/100.0;
    }

    public static void setSeVolume(double seVolume) {
        Sound.seVolume = seVolume/100.0;
    }
}
