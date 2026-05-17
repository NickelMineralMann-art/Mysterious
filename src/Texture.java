import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Texture {
    public int[] pixels;
    private String loc;
    public final int SIZE;

    public Texture(String location, int size) {
        loc = location;
        SIZE = size;
        pixels = new int[SIZE * SIZE];
        load();
    }

    private void load() {
        try {
            BufferedImage image = ImageIO.read(new File(loc));
            int w = image.getWidth();
            int h = image.getHeight();
            image.getRGB(0, 0, w, h, pixels, 0, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Texture wall = new Texture("res/texture/wall.png", 64);
    public static Texture tile = new Texture("res/texture/tiles.png", 64);
    public static Texture wall_tear = new Texture("res/texture/wall_tear.png", 64);
    public static Texture tile_pipe = new Texture("res/texture/tiles_pipe.png", 64);

    public static Texture ceiling = new Texture("res/texture/ceiling.png", 64);
    public static Texture floor = new Texture("res/texture/moquette.png", 64);

    public static Texture exit = new Texture("res/texture/exit.png", 64);
}