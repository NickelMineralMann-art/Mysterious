import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import javax.swing.JFrame;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// RICORDA: Metodo abstract non necessario con runnable
public class Game extends JFrame implements Runnable{
    private static final long serialVersionUID = 1L;
    public int mapWidth = 15;
    public int mapHeight = 15;
    public ArrayList<Texture> textures;
    public ArrayList<Sprite> sprites;

    public Camera camera;
    public Screen screen;

    public String versione = "2.1.12 RELEASE";

    public boolean hasWon = false;
    public boolean wantContinue = false;

    public static int[][] map =
            {
                    {1,1,1,1,1,1,1,2,2,2,2,2,3,3,3},
                    {1,0,1,0,0,0,0,2,0,0,0,0,0,0,3},
                    {1,0,4,4,4,4,0,2,0,2,0,0,3,3,3},
                    {1,0,4,0,0,4,0,0,0,2,0,4,4,4,4},
                    {1,0,4,0,0,4,0,4,4,4,0,0,0,0,4},
                    {1,0,4,0,4,4,0,0,0,0,0,2,0,0,4},
                    {1,0,0,0,2,0,0,0,2,0,0,2,0,0,4},
                    {1,0,0,0,2,0,4,4,4,4,0,2,0,0,4},
                    {1,0,0,0,0,0,4,0,0,4,1,1,0,1,1},
                    {1,0,0,0,2,0,4,0,0,0,0,0,0,0,1},
                    {1,1,0,1,1,1,4,4,4,4,0,0,0,0,1},
                    {1,0,0,0,0,1,0,0,0,1,1,1,1,0,1},
                    {1,0,1,1,1,1,0,0,0,0,0,0,0,0,1},
                    {1,0,0,0,0,0,0,0,0,1,0,0,0,0,1},
                    {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
            };

    private Thread thread;
    private boolean running;

    private BufferedImage image; //Buffer per immagine mostrata su schermo in istante i
    public int[] pixels; // Array di tutti i pixel del BufferedImage

    public Game() {
        textures = new ArrayList<Texture>();
        textures.add(Texture.wood);
        textures.add(Texture.brick);
        textures.add(Texture.bluestone);
        textures.add(Texture.stone);
        textures.add(Texture.exit);

        sprites = new ArrayList<>();
        screen = new Screen(map, mapWidth, mapHeight, textures, 640, 480);

        camera = new Camera(4.5, 4.5, 1, 0, 0, -.66);
        addKeyListener(camera);
        thread = new Thread(this);
        image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

        // Dichiarazione per gli sprite/entità di gioco
        sprites.add(new Sprite(1.5, 13, 4));

        setSize(640, 480);
        setResizable(false);
        setTitle("Mysterious" + versione);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.black);
        setLocationRelativeTo(null);
        setVisible(true);
        start();

        while (!hasWon) {
            System.out.println("X: " + camera.getxPos());
            System.out.println("Y: " +camera.getyPos());
            // Debug per Pos giocatore e per scaramanzia (hasWon non viene registrato se non ci sono questi /:
            if (Math.floor(camera.getxPos()) == 1 && Math.floor(camera.getyPos()) == 13) {
                // System.out.println("raggiunta pos vittoria");
                hasWon = true;
                break;
            }
        }
    }

        // Metodo per Aprire la finestra
    private synchronized void start() {
        running = true;
        thread.start();
    }

        // Metodo per Chiudere la finestra
    public synchronized void stop() {
        running = false;
        try {
            thread.join();
            } catch (InterruptedException e) {
            e.printStackTrace();
            }
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        g.drawImage(image,0,0,image.getWidth(),image.getHeight(),null);
        bs.show();
    } // Necessaria a evitare stuttering durante il render

    public void run() {
        long lastTime = System.nanoTime();
        final double ns = 1000000000.0 / 60.0; // 60 volte al secondo = 60 FPS
        double delta = 0;
        requestFocus();

        SoundPlayer soundPlayer= new SoundPlayer();

        while(running) {
            if(hasWon && wantContinue) {
                    hasWon = false;
                    camera.setxPos(4.5);
                    camera.setyPos(4.5);
            }

            long now = System.nanoTime();
            delta = delta + ((now - lastTime) / ns);
            lastTime = now;
            if (hasWon == false) {
                while (delta >= 1) { //Accertati che avvenga solo 60 volte al secondo
                    screen.update(camera, pixels);
                    screen.drawSprite(camera, pixels, sprites);
                    camera.update(map);

                    delta--;
                }
            }
            else {
                try {
                    image = ImageIO.read(new File("res/texture/haivinto.png"));
                    // System.out.println("Caricata: " + image.getWidth() + "x" + image.getHeight());
                    soundPlayer.playSound("res/sound/victory_fanfare.wav");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            render();
        }
    }

    public static void main(String [] args) {
        Game game = new Game();
    }
}