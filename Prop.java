import java.awt.Color;
import java.awt.image.BufferedImage;

public class Prop extends Entity {
    private int kind, ox, oy;      // kind (P_* in PixelAdventure), image origin offset
    private double hw, hd;         // collision half-width and depth
    private BufferedImage img;
    private Color tint;            // glow colour (null = no light)
    private int lightR;            // glow radius

    public int getKind() { return kind; }
    public void setKind(int kind) { this.kind = kind; }

    public BufferedImage getImg() { return img; }
    public int getOx() { return ox; }
    public int getOy() { return oy; }
    /** Sets the picture and the point on it that sits on the prop's feet position. */
    public void setSprite(BufferedImage img, int ox, int oy) {
        this.img = img;
        this.ox = ox;
        this.oy = oy;
    }

    public double getHw() { return hw; }
    public double getHd() { return hd; }
    public void setHitbox(double hw, double hd) {
        this.hw = hw;
        this.hd = hd;
    }

    public Color getTint() { return tint; }
    public int getLightR() { return lightR; }
    public void setLight(Color tint, int lightR) {
        this.tint = tint;
        this.lightR = lightR;
    }

    @Override public String getEntityType() { return "prop"; }
    @Override public String getDisplayName() { return "Graveyard decoration"; }
}
