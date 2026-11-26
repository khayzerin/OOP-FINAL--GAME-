import java.awt.Color;

/** A soft pool of light (position and radius in screen pixels) used by the night lighting pass. */
public class Light {
    private final int x, y, r;
    private final Color tint;

    public Light(int x, int y, int r, Color tint) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.tint = tint;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getRadius() { return r; }
    public Color getTint() { return tint; }
}
