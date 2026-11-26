/** A moving enemy. Different monster kinds share this common superclass. */
public class Monster extends Entity {
    private int hp, kind;                          // kind: 0 skeleton, 1 ghost, 2 zombie, 3 pumpkin head, 4 wraith
    private double phase, hurt, dead, vx, vy, dirx, diry, homeX, homeY;
    private boolean flip;

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getKind() { return kind; }
    public void setKind(int kind) { this.kind = kind; }

    public double getPhase() { return phase; }
    public void setPhase(double phase) { this.phase = phase; }

    public double getHurt() { return hurt; }
    public void setHurt(double hurt) { this.hurt = hurt; }

    public double getDead() { return dead; }
    public void setDead(double dead) { this.dead = dead; }

    public double getVx() { return vx; }
    public void setVx(double vx) { this.vx = vx; }

    public double getVy() { return vy; }
    public void setVy(double vy) { this.vy = vy; }

    public double getDirx() { return dirx; }
    public void setDirx(double dirx) { this.dirx = dirx; }

    public double getDiry() { return diry; }
    public void setDiry(double diry) { this.diry = diry; }

    public double getHomeX() { return homeX; }
    public double getHomeY() { return homeY; }
    public void setHome(double x, double y) { homeX = x; homeY = y; }

    public boolean isFlip() { return flip; }
    public void setFlip(boolean flip) { this.flip = flip; }

    @Override public String getEntityType() { return "monster"; }

    @Override public String getDisplayName() {
        switch (kind) {
            case 0: return "Skeleton";
            case 1: return "Ghost";
            case 2: return "Zombie";
            case 3: return "Pumpkin Head";
            default: return "Blue Wraith";
        }
    }
}
