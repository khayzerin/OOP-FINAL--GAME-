/**
 * ABSTRACTION: Entity defines the common data and behavior of every
 * object that can exist in the graveyard. Concrete entities must identify
 * themselves and provide a display name.
 *
 * ENCAPSULATION: Position is private and can be accessed through methods.
 */
public abstract class Entity {
    private double x, y; // x,y = feet position

    public double getX() { return x; }
    public double getY() { return y; }

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

    public abstract String getEntityType();
    public abstract String getDisplayName();
}
