import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundPlayer {

    private Clip clip;
    private boolean loop;
    private boolean playing = false;

    public SoundPlayer(String pathname, boolean loop) {
        this.loop = loop;

        try {
            AudioInputStream audioIn =
                    AudioSystem.getAudioInputStream(new File(pathname));

            clip = AudioSystem.getClip();
            clip.open(audioIn);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // metodo costruttore, carica audio con stream a partire da parametro "pathname"


    public void play() {
        if (clip == null || playing) return;

        playing = true;
        clip.setFramePosition(0);

        if (loop) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            clip.start();
        }
        System.out.println("RIPRODUCENDO");
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            playing = false;
        }
    }

    public void reset() {
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
            playing = false;
        }
    }

    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }
    // Controlla se l'audio sta già venendo riprodotto, evita di sovrapporlo di nuovo a se stesso
}