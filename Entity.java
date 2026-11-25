import java.awt.Graphics2D;

/**
 * ABSTRACTION: Entity describes what every object in the graveyard has and can do.
 * It is abstract, so only its subclasses can be created.
 *
 * ENCAPSULATION: position, size and animation clock are private and are changed
 * only through methods that reject invalid numbers.
 */
public abstract class Entity {
    private double x, y;               // x, y = position of the entity's feet
    private final int halfWidth;       // half of the footprint width (used for collision)
    private double animTime;           // seconds this entity has been alive (drives animation)

    protected Entity(int halfWidth) {
        this.halfWidth = halfWidth;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getHalfWidth() { return halfWidth; }
    public double getAnimTime() { return animTime; }

    public void setX(double x) {
        if (Double.isFinite(x)) this.x = x;
    }

    public void setY(double y) {
        if (Double.isFinite(y)) this.y = y;
    }

    public void setPosition(double x, double y) {
        if (Double.isFinite(x) && Double.isFinite(y)) {
            this.x = x;
            this.y = y;
        }
    }

    /** Distance between the feet of two entities. */
    public double distanceTo(Entity other) {
        return Math.hypot(other.x - x, other.y - y);
    }

    /** True when this entity is solid and a body with the given half width at (px, py) would hit it. */
    public boolean blocksFoot(double px, double py, int otherHalfWidth) {
        return isSolid() && Math.abs(px - x) < halfWidth + otherHalfWidth && Math.abs(py - y) < 3;
    }

    /** Solid entities (gravestones, the shrine) cannot be walked through. */
    public boolean isSolid() { return false; }

    /** Called once per frame. Subclasses extend it (POLYMORPHISM) and call super.update(). */
    public void update(double dt, GraveyardAdventure game) {
        animTime += dt;
    }

    public abstract String getEntityType();
    public abstract String getDisplayName();
    public abstract void draw(Graphics2D g, int camX, int camY);

    /** Small oval under the feet, shared by every entity (INHERITANCE). */
    protected void drawShadow(Graphics2D g, int camX, int camY, int halfWidth) {
        g.setColor(GraveyardAdventure.SHADOW);
        g.fillOval((int) getX() - halfWidth - camX, (int) getY() - 2 - camY, halfWidth * 2, 4);
    }
}
