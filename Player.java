/** The character controlled by the player. */
public class Player extends Entity {
    private int dir, hp = 3;                       // dir: 0 down, 1 up, 2 left, 3 right
    private boolean moving, hitDone;
    private double anim, atk, cool, inv, kx, ky;   // animation, attack timer, cooldown, invincibility, knockback

    public int getHealth() { return hp; }
    public void setHealth(int health) { hp = Math.max(0, Math.min(3, health)); }

    public int getDir() { return dir; }
    public void setDir(int dir) { this.dir = dir; }

    public boolean isMoving() { return moving; }
    public void setMoving(boolean moving) { this.moving = moving; }

    public boolean isHitDone() { return hitDone; }
    public void setHitDone(boolean hitDone) { this.hitDone = hitDone; }

    public double getAnim() { return anim; }
    public void setAnim(double anim) { this.anim = anim; }

    public double getAtk() { return atk; }
    public void setAtk(double atk) { this.atk = atk; }

    public double getCool() { return cool; }
    public void setCool(double cool) { this.cool = cool; }

    public double getInv() { return inv; }
    public void setInv(double inv) { this.inv = inv; }

    public double getKx() { return kx; }
    public void setKx(double kx) { this.kx = kx; }

    public double getKy() { return ky; }
    public void setKy(double ky) { this.ky = ky; }

    @Override public String getEntityType() { return "player"; }
    @Override public String getDisplayName() { return "Graveyard Hero"; }
}
