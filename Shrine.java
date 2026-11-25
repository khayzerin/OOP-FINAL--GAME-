import java.awt.Color;
import java.awt.Graphics2D;

/** The lantern shrine (saving will be added later). */
public class Shrine extends Entity {
    private boolean lit = true;

    public Shrine() { super(3); }

    public boolean isLit() { return lit; }
    public void setLit(boolean lit) { this.lit = lit; }

    @Override public boolean isSolid() { return true; }
    @Override public String getEntityType() { return "shrine"; }
    @Override public String getDisplayName() { return "Lantern Shrine"; }

    @Override
    public void draw(Graphics2D g, int camX, int camY) {
        int px = (int) getX() - camX, py = (int) getY() - camY;
        drawShadow(g, camX, camY, 4);
        GraveyardAdventure.fill(g, GraveyardAdventure.BROWN, px - 1, py - 12, 2, 12);       // post
        GraveyardAdventure.fill(g, GraveyardAdventure.BROWN, px - 4, py - 19, 8, 8);        // lantern frame
        if (lit) {
            boolean flicker = (int) (getAnimTime() * 6) % 3 == 0;
            GraveyardAdventure.fill(g, flicker ? new Color(0xFFD878) : GraveyardAdventure.GOLD, px - 3, py - 18, 6, 6);   // flickering glow
        } else {
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE_DARK, px - 3, py - 18, 6, 6);
        }
    }
}
