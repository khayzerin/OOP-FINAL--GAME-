import java.awt.Graphics2D;
import java.util.Random;

/** A graveyard enemy: a skeleton that wanders around the spot where it was placed. */
public class Monster extends Entity {
    private int hp = 2;
    private final double speed = 22;
    private final double wanderRadius;
    private final Random rng = new Random();
    private double homeX, homeY;
    private boolean homeSet;
    private double dirX, dirY, decideTimer;
    private boolean facingLeft;

    public Monster(double wanderRadius) {
        super(4);
        this.wanderRadius = wanderRadius;
    }

    public int getHealth() { return hp; }
    public boolean isAlive() { return hp > 0; }
    public void takeDamage(int amount) { hp = Math.max(0, hp - Math.max(0, amount)); }

    @Override
    public void update(double dt, GraveyardAdventure game) {
        super.update(dt, game);
        if (!homeSet) { homeX = getX(); homeY = getY(); homeSet = true; }

        decideTimer -= dt;
        if (decideTimer <= 0) chooseDirection();

        if (dirX != 0 || dirY != 0) {
            boolean moved = game.tryMove(this, dirX * speed * dt, dirY * speed * dt);
            if (!moved) decideTimer = 0;                 // bumped into something: pick a new way
            if (dirX < 0) facingLeft = true;
            else if (dirX > 0) facingLeft = false;
        }
    }

    private void chooseDirection() {
        decideTimer = 1.0 + rng.nextDouble() * 1.5;
        double fromHome = Math.hypot(getX() - homeX, getY() - homeY);
        if (fromHome > wanderRadius) {                   // too far away: walk back toward home
            dirX = Integer.signum((int) Math.round(homeX - getX()));
            dirY = Integer.signum((int) Math.round(homeY - getY()));
        } else if (rng.nextInt(4) == 0) {                // sometimes just stand still
            dirX = 0;
            dirY = 0;
        } else {
            dirX = rng.nextInt(3) - 1;
            dirY = rng.nextInt(3) - 1;
        }
        if (dirX != 0 && dirY != 0) { dirX *= 0.7071; dirY *= 0.7071; }
    }

    @Override public String getEntityType() { return "monster"; }
    @Override public String getDisplayName() { return "Skeleton"; }

    @Override
    public void draw(Graphics2D g, int camX, int camY) {
        boolean walking = dirX != 0 || dirY != 0;
        boolean step = walking && ((int) (getAnimTime() * 5) % 2 == 1);
        drawShadow(g, camX, camY, 5);
        GraveyardAdventure.drawSprite(g, step ? GraveyardAdventure.SPR_SKELETON_WALK : GraveyardAdventure.SPR_SKELETON,
                (int) getX() - 5 - camX, (int) getY() - 12 - camY, facingLeft);
    }
}
