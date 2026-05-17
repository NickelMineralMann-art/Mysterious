import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundPlayer {
    public static void playSound(String pathname) {
        try {
            File soundFile = new File(pathname);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();

            clip.open(audioIn);
            clip.start();

            boolean isPlayingSound;
            if (clip.isRunning()) {
                isPlayingSound = true;
            } else {
                isPlayingSound = false;
            }
            clip.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}   