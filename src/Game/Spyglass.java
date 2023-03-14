package Game;

public class Spyglass {
    static final Sound useSound = new Sound("spyglass", false, Sound.SoundTypes.SoundEffect);
    public void use(Tile[] targets){
        for (Tile target: targets){
            if (target != null && target.is_occupied && !target.is_hit) target.is_detected = true;
        }
        useSound.play();
    }
}
