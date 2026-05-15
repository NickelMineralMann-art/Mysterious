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

            // numero grosso = riprodotto una volta solo :D
            while (clip.isRunning()) {
                Thread.sleep(10);
            }
            clip.close();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}   