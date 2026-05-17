import java.util.ArrayList;
// import java.awt.Color; -- Refuso da quando non c'era il raycaster ad interpolazione per il pavimento

public class Screen {
    public int[][] map;
    public int mapWidth, mapHeight, width, height;
    public ArrayList<Texture> textures;

    public double [] zBuffer;
    // Indica la distanza perpendicolare dall'oggetto Camera del giocatore, così posso sovrapporre oggetti in profondità

    public void drawSprite(Camera camera, int[] pixels, ArrayList<Sprite> sprites) {

        // Calcola la distanza tra lo sprite e la telecamera del giocatore (Controlla da lontano a vicino)
        for (Sprite s: sprites) {
            s.distance = ((camera.xPos - s.x) * (camera.xPos - s.x)
                        + (camera.yPos - s.y) * (camera.yPos - s.y));
        }
        sprites.sort((a, b) -> Double.compare(b.distance, a.distance));

        for (Sprite sprite : sprites) {

            // Pos dello sprite rispetto alla telecamera
            double spriteX = sprite.x - camera.xPos;
            double spriteY = sprite.y - camera.yPos;

            // Sistema di trasformazioni nel piano di riferimento delle coordinate camera
            double invDet = 1.0 / (camera.xPlane * camera.yDir - camera.xDir * camera.yPlane);
            double transformX = invDet * (camera.yDir * spriteX - camera.xDir * spriteY);
            double transformY = invDet * (-camera.yPlane * spriteX + camera.xPlane * spriteY);
            // TransformY è l'equivalente wallPerpDist per lo Sprite

            if (transformY <= 0) continue; // sprite è dietro alla telecamera, tipo BackFace Culling ma + semplice

            int spriteScreenX = (int)((width / 2) * (1 + transformX / transformY));

            // Dimensione dello sprite per la proiezione sullo schermo
            int spriteHeight = Math.abs((int)(height / transformY));
            int drawStartY = Math.max(0, height / 2 - spriteHeight / 2);
            int drawEndY = Math.min(height - 1, height / 2 + spriteHeight / 2);

            int spriteWidth = spriteHeight; // per semplicità, sprite quadrato, alè
            int drawStartX = Math.max(0, spriteScreenX - spriteWidth / 2);
            int drawEndX = Math.min(width - 1, spriteScreenX + spriteWidth / 2);

            Texture tex = textures.get(sprite.textureIndex);

            for (int x = drawStartX; x < drawEndX; x++) {

                // [CONTROLLO Z BUFFER]
                if (transformY >= zBuffer[x]) continue;

                int texX = (int)((x - (spriteScreenX - spriteWidth / 2)) * tex.SIZE / spriteWidth);

                for (int y = drawStartY; y < drawEndY; y++) {
                    int texY = (int)((y - (height / 2 - spriteHeight / 2)) * tex.SIZE / spriteHeight);
                    int colour = tex.pixels[texX + texY * tex.SIZE];

                    // Salta i pixel definiti trasparenti (Nero puro = 0x00FFFFFF)
                    if ((colour & 0x00FFFFFF) == 0) continue;

                    pixels[x + y * width] = colour;
                }
            }
        }
    }

    public Screen(int[][] m, int mapW, int mapH, ArrayList<Texture> tex, int w, int h) {
        map = m;
        mapWidth = mapW;
        mapHeight = mapH;
        textures = tex;
        width = w;
        height = h;
        zBuffer = new double[w];
    }

    public int[] update(Camera camera, int[] pixels) {

        Texture floorTex = textures.get(5);
        Texture ceilTex = textures.get(4);

        // Floor/Ceiling Caster [!]
        for (int y = height / 2 + 1;  y < height; y++) {
            // direzione dei raggi (striscia di texture) agli estremi della riga corrente
            double rayDirX0 = camera.xDir - camera.xPlane;
            double rayDirY0 = camera.yDir - camera.yPlane;

            double rayDirX1 = camera.xDir + camera.xPlane;
            double rayDirY1 = camera.yDir + camera.yPlane;

            // Distanza verticale da centro della telecamera a riga (Soffitto o Pavimento)
            int p = y - height / 2;
            double rowDistance = (height / 2.0) / p;

            // Di quanto si sposta la coordinata pavimento per ogni pixel della riga
            double floorStepX = rowDistance * (rayDirX1 - rayDirX0) / width;
            double floorStepY = rowDistance * (rayDirY1 - rayDirY0) / width;

            // Coordinata pavimento del pixel più a sinistra della riga
            double floorX = camera.xPos + rowDistance * rayDirX0;
            double floorY = camera.yPos + rowDistance * rayDirY0;

            for (int x = 0; x < width; x++) {

                // Cella della mappa (Griglia in Game) in cui cade il punto
                int cellX = (int) floorX;
                int cellY = (int) floorY;

                // Coordinata del pixel da scannerizzare dalla texture (0...SIZE-1)
                int texX = (int) (floorTex.SIZE * (floorX - cellX)) & (floorTex.SIZE - 1);
                int texY = (int) (floorTex.SIZE * (floorY - cellY)) & (floorTex.SIZE - 1);

                floorX += floorStepX;
                floorY += floorStepY;

                // [[PAVIMENTO]]
                int floorColour = floorTex.pixels[texY * floorTex.SIZE + texX];
                //scurisce il pavimento, come per le pareti ma ruotato di 90°
                floorColour = (floorColour >> 1) & 0x7F7F7F;
                pixels[x + y * width] = floorColour;

                // [[SOFFITTO]]
                int ceilColour = ceilTex.pixels[texY * ceilTex.SIZE + texX];
                ceilColour = (ceilColour >> 1) & 0x7F7F7F;
                pixels[x + (height - y - 1) * width] = ceilColour;
            }
        }

        for(int x=0; x<width; x++) {
            double cameraX = 2 * x / (double)(width) -1;
            double rayDirX = camera.xDir + camera.xPlane * cameraX;
            double rayDirY = camera.yDir + camera.yPlane * cameraX;

            //Posizione della Mappa
            int mapX = (int)camera.xPos;
            int mapY = (int)camera.yPos;

            //Distanza del raggio dal prossimo lato X e/o Y
            double sideDistX;
            double sideDistY;

            //Distanza del raggio dal prossimo
            double deltaDistX = Math.sqrt(1 + (rayDirY*rayDirY) / (rayDirX*rayDirX));
            double deltaDistY = Math.sqrt(1 + (rayDirX*rayDirX) / (rayDirY*rayDirY));
            double perpWallDist;

            //Direzione movimento data dal vettore giocatore
            int stepX, stepY;
            boolean hit = false;//ha colpito un muro?
            int side=0;//era verticale o orizzontale? (Necessario per shading)

            if (rayDirX < 0)
            {
                stepX = -1;
                sideDistX = (camera.xPos - mapX) * deltaDistX;
            }
            else
            {
                stepX = 1;
                sideDistX = (mapX + 1.0 - camera.xPos) * deltaDistX;
            }
            if (rayDirY < 0)
            {
                stepY = -1;
                sideDistY = (camera.yPos - mapY) * deltaDistY;
            }
            else
            {
                stepY = 1;
                sideDistY = (mapY + 1.0 - camera.yPos) * deltaDistY;
            }

            //Loop per raycaster, continua per controllare se colpisce una parete
            while(!hit) {
                //Salta alla prossima cella
                if (sideDistX < sideDistY)
                {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                }
                else
                {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }
                //Controlla se il raggio colpisce una parete
                //System.out.println(mapX + ", " + mapY + ", " + map[mapX][mapY]);
                if(map[mapX][mapY] > 0) hit = true;
            }

            //Calculate distance to the point of impact
            if(side==0)
                perpWallDist = Math.abs((mapX - camera.xPos + (1 - stepX) / 2) / rayDirX);
            else
                perpWallDist = Math.abs((mapY - camera.yPos + (1 - stepY) / 2) / rayDirY);

            //Calcola altezza della striscia verticale del muro in funzione della distanza muro-telecamera giocatore
            int lineHeight;
            if(perpWallDist > 0) lineHeight = Math.abs((int)(height / perpWallDist));
            else lineHeight = height;

            //calcola pixel più alto e basso della striscia e interpola, disegnando una riga tra i due punti
            int drawStart = -lineHeight/2+ height/2;
            if(drawStart < 0)
                drawStart = 0;
            int drawEnd = lineHeight/2 + height/2;
            if(drawEnd >= height)
                drawEnd = height - 1;

            //aggiunge la texture basato sull'indice (CFR Texture.java)
            int texNum = map[mapX][mapY] - 1;
            double wallX;//Posizione esatta dove ha colpito la parete
            if(side==1) {//Se è orizzontale o verticale
                wallX = (camera.xPos + ((mapY - camera.yPos + (1 - stepY) / 2) / rayDirY) * rayDirX);
            } else {
                wallX = (camera.yPos + ((mapX - camera.xPos + (1 - stepX) / 2) / rayDirX) * rayDirY);
            }
            wallX-=Math.floor(wallX);

            //coordinata X della texture
            int texX = (int)(wallX * (textures.get(texNum).SIZE));
            if(side == 0 && rayDirX > 0) texX = textures.get(texNum).SIZE - texX - 1;
            if(side == 1 && rayDirY < 0) texX = textures.get(texNum).SIZE - texX - 1;
            //coordinata Y della texture
            for(int y=drawStart; y<drawEnd; y++) {
                int texY = (((y*2 - height + lineHeight) << 6) / lineHeight) / 2;
                int color;
                if(side==0) color = textures.get(texNum).pixels[texX + (texY * textures.get(texNum).SIZE)];
                else color = (textures.get(texNum).pixels[texX + (texY * textures.get(texNum).SIZE)]>>1) & 8355711;//Make y sides darker
                pixels[x + y*(width)] = color;
            }

            zBuffer[x] = perpWallDist;

        }
        return pixels;
    }
}