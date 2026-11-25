import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GraveyardAdventure extends JPanel implements KeyListener {

    // ------------------------------------------------------------------ resolution
    static final int VIEW_W = 256, VIEW_H = 192;   // internal pixel resolution
    static final int START_SCALE = 3;               // window starts at 768 x 576
    static final int TILE = 16, MW = 64, MH = 48;   // tile size and map size (in tiles)

    // ------------------------------------------------------------------ tile types
    static final int GRASS = 0, WATER = 1, DIRT = 2, COBBLE = 3, FENCE = 5;

    // ------------------------------------------------------------------ palette
    static final Color GRASS_C = new Color(0x3A6B3E), GRASS_DARK = new Color(0x2B5430),
            GRASS_LIGHT = new Color(0x4F8A4A);
    static final Color WATER_C = new Color(0x264E5A), WATER_DARK = new Color(0x1B3A45),
            WATER_LIGHT = new Color(0x4F8C94);
    static final Color DIRT_C = new Color(0x8A6A4A), DIRT_DARK = new Color(0x6E5238),
            DIRT_LIGHT = new Color(0xA3815C);
    static final Color COBBLE_C = new Color(0x6E6288), COBBLE_LINE = new Color(0x463E5E),
            COBBLE_LIGHT = new Color(0x8678A2);
    static final Color FENCE_BG = new Color(0x1C1A28), FENCE_BAR = new Color(0x3A3850),
            FENCE_TIP = new Color(0x7270A0);
    static final Color STONE = new Color(0x80849C), STONE_DARK = new Color(0x4C4F66);
    static final Color GOLD = new Color(0xE8B84A), BROWN = new Color(0x8A5A32);
    static final Color SHADOW = new Color(0, 0, 0, 70);
    static final Color NIGHT = new Color(10, 12, 44, 110);

    // ------------------------------------------------------------------ sprites
    // Each character in a sprite row is one pixel. '.' = transparent. See pal().
    static final String[] SPR_HERO = {          // hooded swordsman, 10 x 14
            "...hhhh...",
            "..hHHHHh..",
            ".hHHHHHHh.",
            ".hHkkkkHh.",
            ".hHkFFkHh.",
            ".hhkFFkhh.",
            "..CCCCCC..",
            ".CCCCCCCC.",
            "CCCCGGCCCC",
            "CCCCCCCCCC",
            ".CCCCCCCC.",
            ".CCCCCCCC.",
            "..CC..CC..",
            "..kk..kk.."};
    static final String[] SPR_SKELETON = {      // graveyard monster, 10 x 12
            "...wwww...",
            "..wwwwww..",
            "..wrwwrw..",
            "..wwwwww..",
            "...wkkw...",
            "....ww....",
            "..wwwwww..",
            "..wgwwgw..",
            "..wwwwww..",
            "...w..w...",
            "...w..w...",
            "..ww..ww.."};

    static final String[] SPR_HERO_WALK = {     // same hero, legs apart (walking frame)
            "...hhhh...",
            "..hHHHHh..",
            ".hHHHHHHh.",
            ".hHkkkkHh.",
            ".hHkFFkHh.",
            ".hhkFFkhh.",
            "..CCCCCC..",
            ".CCCCCCCC.",
            "CCCCGGCCCC",
            "CCCCCCCCCC",
            ".CCCCCCCC.",
            ".CCCCCCCC.",
            ".CC....CC.",
            ".kk....kk."};
    static final String[] SPR_SKELETON_WALK = { // same skeleton, legs apart (walking frame)
            "...wwww...",
            "..wwwwww..",
            "..wrwwrw..",
            "..wwwwww..",
            "...wkkw...",
            "....ww....",
            "..wwwwww..",
            "..wgwwgw..",
            "..wwwwww..",
            "..w....w..",
            ".w......w.",
            ".ww....ww."};

    static Color pal(char ch) {
        switch (ch) {
            case 'h': return new Color(0x3A2F5A);
            case 'H': return new Color(0x5A4A86);
            case 'k': return new Color(0x14121C);
            case 'F': return new Color(0xE8C9A0);
            case 'C': return new Color(0x2E2B45);
            case 'G': return new Color(0xE8B84A);
            case 'w': return new Color(0xE8E4D4);
            case 'g': return new Color(0xA8A494);
            case 'r': return new Color(0xFF3B3B);
            default:  return null;
        }
    }

    static void drawSprite(Graphics2D g, String[] rows, int x, int y) {
        drawSprite(g, rows, x, y, false);
    }

    /** Draws a sprite; flip = true mirrors it left-to-right (used when facing left). */
    static void drawSprite(Graphics2D g, String[] rows, int x, int y, boolean flip) {
        for (int j = 0; j < rows.length; j++) {
            int w = rows[j].length();
            for (int i = 0; i < w; i++) {
                Color c = pal(rows[j].charAt(flip ? w - 1 - i : i));
                if (c != null) {
                    g.setColor(c);
                    g.fillRect(x + i, y + j, 1, 1);
                }
            }
        }
    }

    static void fill(Graphics2D g, Color c, int x, int y, int w, int h) {
        g.setColor(c);
        g.fillRect(x, y, w, h);
    }

    // ------------------------------------------------------------------ entities
    /**
     * ABSTRACTION: Entity describes what every object in the graveyard has and can do.
     * It is abstract, so only its subclasses can be created.
     *
     * ENCAPSULATION: position, size and animation clock are private and are changed
     * only through methods that reject invalid numbers.
     */
    static abstract class Entity {
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
            g.setColor(SHADOW);
            g.fillOval((int) getX() - halfWidth - camX, (int) getY() - 2 - camY, halfWidth * 2, 4);
        }
    }

    /** The character controlled by the player. */
    static class Player extends Entity {
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
            drawSprite(g, step ? SPR_HERO_WALK : SPR_HERO,
                    (int) getX() - 5 - camX, (int) getY() - 14 - camY, facingLeft);
        }
    }

    /** A graveyard enemy: a skeleton that wanders around the spot where it was placed. */
    static class Monster extends Entity {
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
            drawSprite(g, step ? SPR_SKELETON_WALK : SPR_SKELETON,
                    (int) getX() - 5 - camX, (int) getY() - 12 - camY, facingLeft);
        }
    }

    /** A gravestone decoration. Three different shapes, chosen by "variant". */
    static class Prop extends Entity {
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
                fill(g, STONE_DARK, px, py + 1, 10, 11);
                fill(g, STONE, px + 1, py + 1, 8, 10);
                fill(g, STONE, px + 2, py, 6, 1);
                fill(g, STONE_DARK, px + 4, py + 3, 2, 6);
                fill(g, STONE_DARK, px + 2, py + 5, 6, 2);
            } else if (variant == 1) {                           // rounded stone with carved lines
                fill(g, STONE_DARK, px + 1, py + 2, 8, 10);
                fill(g, STONE, px + 2, py + 2, 6, 9);
                fill(g, STONE, px + 3, py + 1, 4, 1);
                fill(g, STONE_DARK, px + 3, py + 4, 4, 1);
                fill(g, STONE_DARK, px + 3, py + 6, 4, 1);
                fill(g, STONE_DARK, px + 3, py + 8, 4, 1);
            } else {                                             // low, wide slab
                fill(g, STONE_DARK, px, py + 6, 10, 6);
                fill(g, STONE, px + 1, py + 6, 8, 5);
                fill(g, STONE_DARK, px + 2, py + 8, 6, 1);
            }
        }
    }

    /** The lantern shrine (saving will be added later). */
    static class Shrine extends Entity {
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
            fill(g, BROWN, px - 1, py - 12, 2, 12);       // post
            fill(g, BROWN, px - 4, py - 19, 8, 8);        // lantern frame
            if (lit) {
                boolean flicker = (int) (getAnimTime() * 6) % 3 == 0;
                fill(g, flicker ? new Color(0xFFD878) : GOLD, px - 3, py - 18, 6, 6);   // flickering glow
            } else {
                fill(g, STONE_DARK, px - 3, py - 18, 6, 6);
            }
        }
    }

    // ------------------------------------------------------------------ state
    enum Screen { TITLE, PLAYING }

    private final int[][] map = new int[MW][MH];
    private final BufferedImage world = new BufferedImage(MW * TILE, MH * TILE, BufferedImage.TYPE_INT_RGB);
    private final BufferedImage frame = new BufferedImage(VIEW_W, VIEW_H, BufferedImage.TYPE_INT_RGB);
    private final List<Entity> entities = new ArrayList<Entity>();   // POLYMORPHISM: one list, many kinds
    private final Player player = new Player();
    private final boolean[] keys = new boolean[256];
    private Screen screen = Screen.TITLE;
    private double time;
    private long lastNanos;

    // ------------------------------------------------------------------ construction
    public GraveyardAdventure() {
        setPreferredSize(new Dimension(VIEW_W * START_SCALE, VIEW_H * START_SCALE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        buildMap();
        buildWorldImage();
        placeEntities();
    }

    /** Starts the game loop (about 60 FPS). */
    void start() {
        lastNanos = System.nanoTime();
        new javax.swing.Timer(16, e -> {
            long now = System.nanoTime();
            double dt = Math.min(0.05, (now - lastNanos) / 1e9);
            lastNanos = now;
            update(dt);
            repaint();
        }).start();
    }

    // ------------------------------------------------------------------ map
    /** Basic graveyard: fence border, grass, two swamp ponds, dirt crossroads, cobble plaza. */
    void buildMap() {
        for (int x = 0; x < MW; x++)
            for (int y = 0; y < MH; y++)
                map[x][y] = (x < 2 || y < 2 || x >= MW - 2 || y >= MH - 2) ? FENCE : GRASS;

        pond(50, 12, 5, 3);
        pond(12, 36, 4, 3);

        int cx = MW / 2, cy = MH / 2;
        for (int x = 2; x < MW - 2; x++) {          // east-west road (2 tiles wide)
            map[x][cy] = DIRT;
            map[x][cy + 1] = DIRT;
        }
        for (int y = 2; y < MH - 2; y++) {          // north-south road (2 tiles wide)
            map[cx][y] = DIRT;
            map[cx + 1][y] = DIRT;
        }
        for (int dx = -3; dx <= 3; dx++)            // cobblestone plaza at the crossroads
            for (int dy = -3; dy <= 3; dy++)
                if (dx * dx + dy * dy <= 10) map[cx + dx][cy + dy] = COBBLE;
    }

    void pond(int cx, int cy, double rx, double ry) {
        for (int x = 2; x < MW - 2; x++) {
            for (int y = 2; y < MH - 2; y++) {
                double dx = (x - cx) / rx, dy = (y - cy) / ry;
                if (dx * dx + dy * dy < 1) map[x][y] = WATER;
            }
        }
    }

    /** Paints every tile once into the big "world" picture. */
    void buildWorldImage() {
        Graphics2D g = world.createGraphics();
        Random r = new Random(3);
        for (int x = 0; x < MW; x++) {
            for (int y = 0; y < MH; y++) {
                int px = x * TILE, py = y * TILE;
                switch (map[x][y]) {
                    case WATER:
                        fill(g, WATER_C, px, py, TILE, TILE);
                        for (int i = 0; i < 3; i++)
                            fill(g, WATER_DARK, px + r.nextInt(12), py + r.nextInt(14), 3, 1);
                        if (r.nextInt(6) == 0)
                            fill(g, WATER_LIGHT, px + r.nextInt(12), py + r.nextInt(14), 2, 1);
                        break;
                    case DIRT:
                        fill(g, DIRT_C, px, py, TILE, TILE);
                        for (int i = 0; i < 5; i++)
                            fill(g, r.nextBoolean() ? DIRT_DARK : DIRT_LIGHT,
                                    px + r.nextInt(14), py + r.nextInt(14), 2, 1);
                        break;
                    case COBBLE:
                        fill(g, COBBLE_C, px, py, TILE, TILE);
                        fill(g, COBBLE_LINE, px, py + 7, TILE, 1);
                        fill(g, COBBLE_LINE, px + 7, py, 1, 8);
                        fill(g, COBBLE_LINE, px + 3, py + 8, 1, 8);
                        fill(g, COBBLE_LIGHT, px + 1, py + 1, 3, 1);
                        fill(g, COBBLE_LIGHT, px + 9, py + 9, 3, 1);
                        break;
                    case FENCE:
                        fill(g, FENCE_BG, px, py, TILE, TILE);
                        for (int b = 2; b < TILE; b += 5) {
                            fill(g, FENCE_BAR, px + b, py, 2, TILE);
                            fill(g, FENCE_TIP, px + b, py, 2, 1);
                        }
                        break;
                    default:   // GRASS
                        fill(g, GRASS_C, px, py, TILE, TILE);
                        for (int i = 0; i < 4; i++)
                            fill(g, r.nextBoolean() ? GRASS_DARK : GRASS_LIGHT,
                                    px + r.nextInt(14), py + r.nextInt(14), 1, 2);
                }
            }
        }
        g.dispose();
    }

    // ------------------------------------------------------------------ entities
    void placeEntities() {
        int cx = MW / 2 * TILE, cy = MH / 2 * TILE;
        player.setPosition(cx + 8, cy + 8);          // start on the plaza
        entities.add(player);

        Shrine shrine = new Shrine();
        shrine.setPosition(cx + 56, cy - 24);
        entities.add(shrine);

        double[][] spawns = {{cx - 80, cy + 70}, {200, 200}, {820, 300}, {700, 600}, {250, 420}};
        for (double[] p : spawns) {
            Monster skeleton = new Monster(60);
            skeleton.setPosition(p[0], p[1]);
            entities.add(skeleton);
        }

        double[][] graves = {{400, 300}, {432, 300}, {600, 300}, {624, 322}, {300, 470}, {330, 480}, {700, 480}};
        int variant = 0;
        for (double[] p : graves) {
            Prop grave = new Prop(variant++);
            grave.setPosition(p[0], p[1]);
            entities.add(grave);
        }
    }

    /** POLYMORPHISM: counts entities through Entity references using getEntityType(). */
    int countEntitiesOfType(String type) {
        int n = 0;
        for (Entity e : entities)
            if (e.getEntityType().equals(type)) n++;
        return n;
    }

    // ------------------------------------------------------------------ collision
    boolean solidAt(double px, double py) {
        int tx = (int) Math.floor(px / TILE), ty = (int) Math.floor(py / TILE);
        if (tx < 0 || ty < 0 || tx >= MW || ty >= MH) return true;
        int t = map[tx][ty];
        return t == FENCE || t == WATER;
    }

    boolean blocked(double x, double y) {
        return solidAt(x - 4, y) || solidAt(x + 4, y);
    }

    /** True if a solid entity (gravestone, shrine) is in the way of body e at (x, y). */
    boolean blockedByEntity(Entity self, double x, double y) {
        for (Entity o : entities)
            if (o != self && o.blocksFoot(x, y, self.getHalfWidth())) return true;
        return false;
    }

    /** Moves an entity one axis at a time so it can slide along walls. Returns true if it moved. */
    boolean tryMove(Entity e, double dx, double dy) {
        boolean moved = false;
        double nx = e.getX() + dx;
        if (!blocked(nx, e.getY()) && !blockedByEntity(e, nx, e.getY())) {
            e.setX(nx);
            moved |= dx != 0;
        }
        double ny = e.getY() + dy;
        if (!blocked(e.getX(), ny) && !blockedByEntity(e, e.getX(), ny)) {
            e.setY(ny);
            moved |= dy != 0;
        }
        return moved;
    }

    // ------------------------------------------------------------------ update
    boolean down(int code) {
        return code < keys.length && keys[code];
    }

    void update(double dt) {
        time += dt;
        if (screen != Screen.PLAYING) return;

        double dx = 0, dy = 0;
        if (down(KeyEvent.VK_LEFT) || down(KeyEvent.VK_A)) dx -= 1;
        if (down(KeyEvent.VK_RIGHT) || down(KeyEvent.VK_D)) dx += 1;
        if (down(KeyEvent.VK_UP) || down(KeyEvent.VK_W)) dy -= 1;
        if (down(KeyEvent.VK_DOWN) || down(KeyEvent.VK_S)) dy += 1;
        if (dx != 0 && dy != 0) { dx *= 0.7071; dy *= 0.7071; }
        player.setInput(dx, dy);

        // POLYMORPHISM: every entity updates itself (the player walks, skeletons wander, the lantern flickers)
        for (Entity e : new ArrayList<Entity>(entities)) e.update(dt, this);
    }

    // ------------------------------------------------------------------ rendering
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D fg = frame.createGraphics();
        fg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        fg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        fg.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        if (screen == Screen.TITLE) drawTitle(fg);
        else render(fg);
        fg.dispose();

        // scale the 256 x 192 frame up with crisp pixels (whole-number scale, centered)
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int s = Math.max(1, Math.min(getWidth() / VIEW_W, getHeight() / VIEW_H));
        int w = VIEW_W * s, h = VIEW_H * s;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(frame, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
    }

    void render(Graphics2D g) {
        int camX = (int) Math.max(0, Math.min(MW * TILE - VIEW_W, player.getX() - VIEW_W / 2));
        int camY = (int) Math.max(0, Math.min(MH * TILE - VIEW_H, player.getY() - 8 - VIEW_H / 2));

        g.drawImage(world, -camX, -camY, null);

        // draw everything sorted by Y so things lower on screen appear in front
        List<Entity> sorted = new ArrayList<Entity>(entities);
        Collections.sort(sorted, (a, b) -> Double.compare(a.getY(), b.getY()));
        for (Entity e : sorted) e.draw(g, camX, camY);   // POLYMORPHISM: each class draws itself

        fill(g, NIGHT, 0, 0, VIEW_W, VIEW_H);            // night tint

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        drawText(g, "HP: " + player.getHealth() + "/" + Player.MAX_HP, 4, 12, new Color(0xFF6B6B));
        drawText(g, "Monsters: " + countEntitiesOfType("monster"), 4, 24, Color.WHITE);
        drawText(g, "ESC = Main Menu", 4, VIEW_H - 5, new Color(0xB0ADB8));
    }

    void drawTitle(Graphics2D g) {
        fill(g, new Color(0x120E1C), 0, 0, VIEW_W, VIEW_H);
        fill(g, new Color(0x1C1A28), 0, 140, VIEW_W, 52);        // ground
        g.setColor(new Color(0xE4ECF6));                          // moon
        g.fillOval(190, 22, 28, 28);
        g.setColor(new Color(0x120E1C));
        g.fillOval(198, 18, 26, 26);
        drawSprite(g, SPR_HERO, 123, 118);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        drawCentered(g, "GRAVEYARD ADVENTURE", VIEW_W / 2, 70, new Color(0xE8B84A));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        if ((int) (time * 2) % 2 == 0)
            drawCentered(g, "PRESS ENTER TO START", VIEW_W / 2, 95, Color.WHITE);
        drawCentered(g, "Arrows / WASD = move", VIEW_W / 2, 172, new Color(0xB0ADB8));
    }

    void drawText(Graphics2D g, String s, int x, int y, Color c) {
        g.setColor(Color.BLACK);
        g.drawString(s, x + 1, y + 1);
        g.setColor(c);
        g.drawString(s, x, y);
    }

    void drawCentered(Graphics2D g, String s, int cx, int y, Color c) {
        FontMetrics fm = g.getFontMetrics();
        drawText(g, s, cx - fm.stringWidth(s) / 2, y, c);
    }

    // ------------------------------------------------------------------ input
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k < keys.length) keys[k] = true;
        if (screen == Screen.TITLE && k == KeyEvent.VK_ENTER) {
            screen = Screen.PLAYING;
        } else if (screen == Screen.PLAYING && k == KeyEvent.VK_ESCAPE) {
            java.util.Arrays.fill(keys, false);
            screen = Screen.TITLE;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k < keys.length) keys[k] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    // ------------------------------------------------------------------ main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Graveyard Adventure");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GraveyardAdventure game = new GraveyardAdventure();
            f.add(game);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            game.requestFocusInWindow();
            game.start();
        });
    }
}