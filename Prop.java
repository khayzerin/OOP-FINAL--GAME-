import java.awt.Color;
import java.awt.Graphics2D;

/** A gravestone decoration. Three different shapes, chosen by "variant". */
public class Prop extends Entity {
    private final int variant;

    public Prop(int variant) {
        super(5);
        this.variant = Math.floorMod(variant, 3);
    }

    @Override public boolean isSolid() { return true; }
    @Override public String getEntityType() { return "prop"; }
    @Override public String getDisplayName() { return "Gravestone"; }

    @Override
    public void draw(Graphics2D g, int camX, int camY) {
        int px = (int) getX() - 5 - camX, py = (int) getY() - 12 - camY;
        drawShadow(g, camX, camY, 6);
        if (variant == 0) {                                  // cross gravestone
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE_DARK, px, py + 1, 10, 11);
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE, px + 1, py + 1, 8, 10);
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE, px + 2, py, 6, 1);
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE_DARK, px + 4, py + 3, 2, 6);
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE_DARK, px + 2, py + 5, 6, 2);
        } else if (variant == 1) {                           // rounded stone with carved lines
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE_DARK, px + 1, py + 2, 8, 10);
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE, px + 2, py + 2, 6, 9);
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE, px + 3, py + 1, 4, 1);
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE_DARK, px + 3, py + 4, 4, 1);
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE_DARK, px + 3, py + 6, 4, 1);
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE_DARK, px + 3, py + 8, 4, 1);
        } else {                                             // low, wide slab
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE_DARK, px, py + 6, 10, 6);
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE, px + 1, py + 6, 8, 5);
            GraveyardAdventure.fill(g, GraveyardAdventure.STONE_DARK, px + 2, py + 8, 6, 1);
        }
    }
}
