import java.awt.Graphics2D;

/** The character controlled by the player. */
public class Player extends Entity {
    static final int MAX_HP = 3;
    private int hp = MAX_HP;
    private final double speed = 70;     // pixels per second
    private double inputX, inputY;       // set by the game from the keyboard
    private boolean facingLeft;

    public Player() { super(4); }

    public int getHealth() { return hp; }
    public void setHealth(int health) { hp = Math.max(0, Math.min(MAX_HP, health)); }
    public double getSpeed() { return speed; }

    /** Direction the player wants to walk this frame (each between -1 and 1). */
    public void setInput(double ix, double iy) {
        inputX = ix;
        inputY = iy;
        if (ix < 0) facingLeft = true;
        else if (ix > 0) facingLeft = false;
    }

    public boolean isMoving() { return inputX != 0 || inputY != 0; }

    @Override
    public void update(double dt, GraveyardAdventure game) {
        super.update(dt, game);
        if (isMoving()) game.tryMove(this, inputX * speed * dt, inputY * speed * dt);
    }

    @Override public String getEntityType() { return "player"; }
    @Override public String getDisplayName() { return "Graveyard Hero"; }

    @Override
    public void draw(Graphics2D g, int camX, int camY) {
        boolean step = isMoving() && ((int) (getAnimTime() * 8) % 2 == 1);   // alternate legs while walking
        drawShadow(g, camX, camY, 5);
        GraveyardAdventure.drawSprite(g, step ? GraveyardAdventure.SPR_HERO_WALK : GraveyardAdventure.SPR_HERO,
                (int) getX() - 5 - camX, (int) getY() - 14 - camY, facingLeft);
    }
}
