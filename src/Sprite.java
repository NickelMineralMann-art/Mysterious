public class Sprite {
    public double x, y;         //Posizione rispetto alla mappa
    public int textureIndex;    //Quale Texture utilizzare per l'oggetto
    public double distance;     // calcolata al runtime, per ordinare gli oggetti nello spazio

    public Sprite(double x, double y, int texture) {
        this.x = x;
        this.y = y;
        textureIndex = texture;
    }
}
