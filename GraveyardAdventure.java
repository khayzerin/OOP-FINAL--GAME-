import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class GraveyardAdventure extends JPanel implements KeyListener {

    // ------------------------------------------------------------------ settings
    static final int VIEW_W = 256, VIEW_H = 192;   // internal pixel resolution
    static final int START_SCALE = 3;               // window starts at 768 x 576
    static final int TILE = 16, MW = 64, MH = 48;   // tile size and map size (tiles)
    static final int GRASS = 0, WATER = 1, DIRT = 2, COBBLE = 3, CLIFF = 4, FENCE = 5, LEAF = 6, GRAVEL = 7;
    static final double ATTACK_TIME = 0.28;
    static final int[] DX = {0, 0, -1, 1};          // facing: 0 down, 1 up, 2 left, 3 right
    static final int[] DY = {1, -1, 0, 0};
    static final File SAVE_FILE =
            new File(System.getProperty("user.home"), "graveyard_adventure_save.properties");

    // ------------------------------------------------------------------- palette
    static final Color GRASS_C = new Color(0x3A6B3E), GRASS_DARK = new Color(0x2B5430),
            GRASS_LIGHT = new Color(0x4F8A4A);
    static final Color WATER_C = new Color(0x264E5A), WATER_DARK = new Color(0x1B3A45),
            WATER_LIGHT = new Color(0x4F8C94);
    static final Color EDGE = new Color(0x4A3A2A), EDGE_DARK = new Color(0x2E2418);
    static final Color DIRT_C = new Color(0x8A6A4A), DIRT_DARK = new Color(0x6E5238), DIRT_LIGHT = new Color(0xA3815C);
    static final Color COBBLE_C = new Color(0x6E6288), COBBLE_LINE = new Color(0x463E5E), COBBLE_LIGHT = new Color(0x8678A2);
    static final Color LEAF_C = new Color(0x9A7234), LEAF_O = new Color(0xC8872E), LEAF_D = new Color(0x7E5524),
            LEAF_R = new Color(0xB5532A);
    static final Color GRAVEL_C = new Color(0x85828C), GRAVEL_L = new Color(0xB0ADB8), GRAVEL_D = new Color(0x625F6C);
    static final Color WALL_C = new Color(0x5B5870), WALL_L = new Color(0x7A7795), WALL_D = new Color(0x37354A);
    static final Color FENCE_BG = new Color(0x1C1A28), FENCE_BAR = new Color(0x3A3850), FENCE_TIP = new Color(0x7270A0);
    static final Color OUTLINE = new Color(0x120E1C);
    static final Color S_MID = new Color(0x80849C), S_LIGHT = new Color(0xA8ACC4), S_DARK = new Color(0x4C4F66),
            S_OUT = new Color(0x20202E);
    static final Color STEEL = new Color(0xE4ECF6), GOLD = new Color(0xE8B84A), BROWN = new Color(0x8A5A32);
    static final Color SHADOW = new Color(0, 0, 0, 70);
    static final Color NIGHT = new Color(10, 12, 44, 125);
    static final Color HEART_FULL = new Color(0xE63946), HEART_EMPTY = new Color(0x3A3A4C);

    // ------------------------------------------------------------ hero sprite (12x16)
    static final String[] SPR_DOWN = {
            "....hhhh....",
            "...hHHHHh...",
            "..hHHHHHHh..",
            "..hHKKKKHh..",
            "..hKFFFFKh..",
            "..hKFKKFKh..",
            "..CKFFFFKC..",
            ".WWwCCCCCCC.",
            ".WwGGGGGGCC.",
            ".CGGGBBGGGC.",
            ".CCGGGGGGCC.",
            ".CCCGGGGCCC.",
            ".CCCCGGCCCC.",
            ".CcCCCCCCcC.",
            ".CCCCCCCCCC.",
            "..CCCC.CCCC."};
    static final String[] SPR_UP = {
            "....hhhh....",
            "...hHHHHh...",
            "..hHHHHHHh..",
            "..hHHHHHHh..",
            "..hHHHHHHh..",
            "..hHHHHHHh..",
            "..ChhhhhhC..",
            ".WWwCCCCCCC.",
            ".WwCCCCCCCC.",
            ".CCCCCCCCCC.",
            ".CCCCccCCCC.",
            ".CCCCccCCCC.",
            ".CCCCCCCCCC.",
            ".CcCCCCCCcC.",
            ".CCCCCCCCCC.",
            "..CCCC.CCCC."};
    static final String[] SPR_LEFT = {
            "....hhhh....",
            "...hHHHHh...",
            "..hHHHHHHh..",
            "..hKKKHHHh..",
            "..KFFKHHHh..",
            "..KFKKHHHh..",
            "..hKFFKHCC..",
            ".WWwCCCCCCC.",
            ".WwGGGGGCCC.",
            ".CGGGBBGGCC.",
            ".CCGGGGGCCC.",
            ".CCCGGGCCCC.",
            ".CCCCGGCCCC.",
            ".CcCCCCCCcC.",
            ".CCCCCCCCCC.",
            "..CCCC.CCCC."};
    static final String[] HEART = {
            ".RR.RR.",
            "RRRRRRR",
            "RRRRRRR",
            ".RRRRR.",
            "..RRR..",
            "...R..."};

    // ---------------------------------------------------------- monster sprites (10x12)
    static final String[] MON_SKELETON = {
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
    static final String[] MON_GHOST = {
            "...llll...",
            "..llllll..",
            ".llllllll.",
            ".llkllkll.",
            ".llkllkll.",
            ".llllllll.",
            ".lllkklll.",
            ".llllllll.",
            ".Lllllllll",
            "LllllllllL",
            "Lll.ll.llL",
            "L..l..l..L"};
    static final String[] MON_ZOMBIE = {
            "..dddddd..",
            ".dGGGGGGd.",
            ".GGyGGyGG.",
            ".GGGGGGGG.",
            ".GGkkkkGG.",
            "..GGGGGG..",
            ".GGnnnnGG.",
            "GGGnnnnGGG",
            "GG.nnnn.GG",
            "...nnnn...",
            "...nn.nn..",
            "..kk..kk.."};
    static final String[] MON_PUMPKIN = {
            ".....n....",
            "..OOOOOO..",
            ".OooooooO.",
            "OooyooyooO",
            "OooooooooO",
            "OoyoyyoyoO",
            ".OoyyyyoO.",
            "..OOOOOO..",
            "...eeee...",
            "..eeeeee..",
            "..ee..ee..",
            "..ee..ee.."};
    static final String[] MON_WRAITH = {
            "...BBBB...",
            "..BbbbbB..",
            ".BbbbbbbB.",
            ".BbkkkkbB.",
            ".BbkccKbB.",
            ".BbkkkkbB.",
            "..BbbbbB..",
            ".BBbbbbBB.",
            "BBbbbbbbBB",
            "BbbbbbbbbB",
            "BBbbbbbbBB",
            ".BBBBBBBB."};
    static final String[][] MON_SPR = {MON_SKELETON, MON_GHOST, MON_ZOMBIE, MON_PUMPKIN, MON_WRAITH};
    /** Monster stats by kind: 0 skeleton, 1 ghost, 2 zombie, 3 pumpkin head, 4 wraith. */
    static final int[] MON_HP = {2, 2, 3, 2, 2};
    static final double[] MON_SPEED = {24, 22, 15, 48, 26};     // px/s (pumpkin hops, so it is faster)
    static final double[] MON_CHASE = {80, 95, 100, 80, 100};   // sight range in px
    static final boolean[] MON_FLY = {false, true, false, false, true};   // ghosts & wraiths glide over anything

    static Color pal(char ch) {
        switch (ch) {
            case 'H': return new Color(0xBDBBC8);
            case 'h': return new Color(0x7C7A8C);
            case 'K': return new Color(0x14121A);
            case 'F': return new Color(0xE8935A);
            case 'C': return new Color(0x2E2038);
            case 'c': return new Color(0x4B3350);
            case 'G': return new Color(0x6E6A74);
            case 'B': return new Color(0x8A5A32);
            case 'W': return new Color(0xF0EEF4);
            case 'w': return new Color(0xA8A6B6);
            case 'R': return Color.WHITE;
            default:  return null;
        }
    }

    static Color mpal(char ch) {
        switch (ch) {
            case 'w': return new Color(0xE8E4D4);
            case 'g': return new Color(0xA8A494);
            case 'k': return new Color(0x14121C);
            case 'K': return new Color(0x14121C);
            case 'r': return new Color(0xFF3B3B);
            case 'G': return new Color(0x6BAA55);
            case 'd': return new Color(0x3E6B3A);
            case 'y': return new Color(0xFFD84A);
            case 'o': return new Color(0xF08A24);
            case 'O': return new Color(0xB4531A);
            case 'n': return new Color(0x7A4A2A);
            case 'l': return new Color(0xE4F2FF);
            case 'L': return new Color(0x9CBCE0);
            case 'b': return new Color(0x4A6FD0);
            case 'B': return new Color(0x283C86);
            case 'c': return new Color(0x6CF0FF);
            case 'e': return new Color(0x3B2F4A);
            default:  return null;
        }
    }

    // ------------------------------------------------------------------ entities
    // Entity, Player, Monster, Prop, Shrine and Light each live in their own .java file.
    static final int P_TREE = 0, P_TOMB = 1, P_CROSS = 2, P_OBELISK = 3, P_PUMPKIN = 4, P_LAMP = 5, P_CRYPT = 6;

    // ------------------------------------------------------------ prop pictures
    static final BufferedImage[] TREE_IMG = {makeDeadTree(1), makeDeadTree(2), makeDeadTree(3), makeDeadTree(4)};
    static final BufferedImage[] TOMB_IMG = {makeTomb(0), makeTomb(1), makeTomb(2), makeTomb(3)};
    static final BufferedImage[] CROSS_IMG = {makeCross(0), makeCross(1), makeCross(2)};
    static final BufferedImage[] OBELISK_IMG = {makeObelisk()};
    static final BufferedImage[] PUMPKIN_IMG = {makePumpkin(0), makePumpkin(1), makePumpkin(2)};
    static final BufferedImage[] LAMP_IMG = {makeLamp()};
    static final BufferedImage[] CRYPT_IMG = {makeCrypt(0), makeCrypt(1)};

    // --------------------------------------------------------------------- state
    private final int[][] map = new int[MW][MH];
    private final BufferedImage world = new BufferedImage(MW * TILE, MH * TILE, BufferedImage.TYPE_INT_RGB);
    private final BufferedImage frame = new BufferedImage(VIEW_W, VIEW_H, BufferedImage.TYPE_INT_RGB);
    private final BufferedImage lightMap = new BufferedImage(VIEW_W, VIEW_H, BufferedImage.TYPE_INT_ARGB);
    private final List<Prop> props = new ArrayList<Prop>();
    private final List<Monster> monsters = new ArrayList<Monster>();
    private final Player player = new Player();
    private final Shrine shrine = new Shrine();
    private final Random rnd = new Random();
    private final boolean[] keys = new boolean[256];
    private final double[] leafX = new double[34], leafY = new double[34];
    private final int[] hubX = new int[12], hubY = new int[12];          // 4 x 3 grid of crossroads
    private double spawnX, spawnY;

    private double time, msgT;
    private String msg = "";
    private int kills, best, savedKills;                 // score of this run / best ever / score at last save
    private double savedX, savedY;                       // last saved position
    private boolean hasSave;
    private boolean title = true;                        // true while the start page is showing
    private long lastNanos;
    // -------------------------------------------------------------- construction
    public GraveyardAdventure() {
        setPreferredSize(new Dimension(VIEW_W * START_SCALE, VIEW_H * START_SCALE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        buildMap();
        buildWorldImage();
        shrine.setX((hubX[4] + 2) * TILE + 8);
        shrine.setY((hubY[4] + 1) * TILE + 12);
        placeProps();
        placeMonsters();
        for (int i = 0; i < leafX.length; i++) {
            leafX[i] = rnd.nextInt(VIEW_W);
            leafY[i] = rnd.nextInt(VIEW_H);
        }
        savedX = spawnX;
        savedY = spawnY;
        player.setX(spawnX);
        player.setY(spawnY);
        load();
        applySave();
    }

    /** Starts the game loop (~60 FPS). */
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

    // --------------------------------------------------------------- map & world
    static final int[] SX = {1, 0, -1, 0};
    static final int[] SY = {0, 1, 0, -1};

    void buildMap() {
        Random r = new Random(7);
        for (int x = 0; x < MW; x++)
            for (int y = 0; y < MH; y++)
                map[x][y] = (x < 2 || y < 2 || x >= MW - 2 || y >= MH - 2) ? FENCE : GRASS;

        // crossroads on a jittered 4 x 3 grid
        for (int cx = 0; cx < 4; cx++) {
            for (int cy = 0; cy < 3; cy++) {
                int i = cx * 3 + cy;
                hubX[i] = 9 + cx * (MW - 18) / 3 + r.nextInt(5) - 2;
                hubY[i] = 8 + cy * (MH - 16) / 2 + r.nextInt(5) - 2;
            }
        }
        spawnX = hubX[4] * TILE + 8;
        spawnY = hubY[4] * TILE + 8;

        // swamp ponds, kept away from the crossroads
        int ponds = 0, tries = 0;
        while (ponds < 5 && tries++ < 500) {
            int cx = 6 + r.nextInt(MW - 12), cy = 6 + r.nextInt(MH - 12);
            double rx = 3 + r.nextInt(4), ry = 2 + r.nextInt(3);
            boolean ok = true;
            for (int i = 0; i < 12; i++) if (dist(cx, cy, hubX[i], hubY[i]) < rx + 8) ok = false;
            if (!ok) continue;
            for (int x = 2; x < MW - 2; x++) {
                for (int y = 2; y < MH - 2; y++) {
                    double dx = (x - cx) / rx, dy = (y - cy) / ry;
                    if (dx * dx + dy * dy < 1) map[x][y] = WATER;
                }
            }
            ponds++;
        }

        // crumbling stone walls, each with a gap
        for (int i = 0; i < 9; i++) {
            boolean horiz = r.nextBoolean();
            int len = 7 + r.nextInt(7);
            int sx = 4 + r.nextInt(MW - len - 8), sy = 4 + r.nextInt(MH - len - 8);
            int gap = 2 + r.nextInt(len - 4);
            for (int k = 0; k < len; k++) {
                if (k == gap || k == gap + 1) continue;
                int x = horiz ? sx + k : sx, y = horiz ? sy : sy + k;
                if (map[x][y] == GRASS) map[x][y] = CLIFF;
            }
        }

        // ---- the path network ----
        int[] mats = {DIRT, COBBLE, LEAF, GRAVEL};
        List<int[]> cells = new ArrayList<int[]>();
        for (int cx = 0; cx < 4; cx++) {
            for (int cy = 0; cy < 3; cy++) {
                int i = cx * 3 + cy;
                if (cx < 3) {                               // east-west roads
                    int j = (cx + 1) * 3 + cy;
                    boolean main = cy == 1;
                    carve(hubX[i], hubY[i], hubX[j], hubY[j], main ? COBBLE : mats[r.nextInt(4)],
                            main || r.nextInt(3) == 0 ? 2 : 1, r, cells);
                }
                if (cy < 2) {                               // north-south roads
                    int j = cx * 3 + cy + 1;
                    boolean main = cx == 1;
                    carve(hubX[i], hubY[i], hubX[j], hubY[j], main ? COBBLE : mats[r.nextInt(4)],
                            main || r.nextInt(3) == 0 ? 2 : 1, r, cells);
                }
            }
        }
        for (int k = 0; k < 6; k++) {                       // diagonal shortcuts
            int cx = r.nextInt(3), cy = r.nextInt(2);
            carve(hubX[cx * 3 + cy], hubY[cx * 3 + cy], hubX[(cx + 1) * 3 + cy + 1], hubY[(cx + 1) * 3 + cy + 1],
                    mats[r.nextInt(4)], 1, r, cells);
        }
        for (int i = 0; i < 12; i++)                        // plazas at the crossroads
            plaza(hubX[i], hubY[i], i == 4 ? 3 : 2, i == 4 ? COBBLE : (r.nextBoolean() ? COBBLE : GRAVEL));
        for (int k = 0; k < 26; k++) {                      // side trails and dead ends
            int[] c = cells.get(r.nextInt(cells.size()));
            spur(c[0], c[1], r, mats[r.nextInt(4)]);
        }
    }

    void paint(int x, int y, int mat, int w) {
        for (int i = 0; i < w; i++)
            for (int j = 0; j < w; j++) {
                int tx = x + i, ty = y + j;
                if (tx >= 2 && ty >= 2 && tx < MW - 2 && ty < MH - 2) map[tx][ty] = mat;
            }
    }

    void plaza(int cx, int cy, int rad, int mat) {
        for (int dx = -rad; dx <= rad; dx++)
            for (int dy = -rad; dy <= rad; dy++)
                if (dx * dx + dy * dy <= rad * rad + 1) paint(cx + dx, cy + dy, mat, 1);
    }

    /** Drunken-walk road from one crossroads to another. */
    void carve(int x0, int y0, int x1, int y1, int mat, int width, Random r, List<int[]> cells) {
        int x = x0, y = y0, guard = 0;
        while ((x != x1 || y != y1) && guard++ < 3000) {
            paint(x, y, mat, width);
            cells.add(new int[]{x, y, mat});
            int dx = Integer.signum(x1 - x), dy = Integer.signum(y1 - y);
            double roll = r.nextDouble();
            if (roll < 0.60) {
                if (Math.abs(x1 - x) >= Math.abs(y1 - y)) x += dx; else y += dy;
            } else if (roll < 0.80) {
                if (dx != 0 && dy != 0) { if (r.nextBoolean()) x += dx; else y += dy; }
                else if (dx != 0) x += dx; else y += dy;
            } else {
                if (Math.abs(x1 - x) > Math.abs(y1 - y)) y += r.nextBoolean() ? 1 : -1;
                else x += r.nextBoolean() ? 1 : -1;
            }
            x = Math.max(3, Math.min(MW - 4, x));
            y = Math.max(3, Math.min(MH - 4, y));
        }
        paint(x, y, mat, width);
    }

    /** A meandering side trail that ends in a little clearing. */
    void spur(int x, int y, Random r, int mat) {
        int dir = r.nextInt(4), len = 6 + r.nextInt(10);
        for (int k = 0; k < len; k++) {
            paint(x, y, mat, 1);
            if (r.nextInt(100) < 18) dir = (dir + (r.nextBoolean() ? 1 : 3)) % 4;
            x = Math.max(3, Math.min(MW - 4, x + SX[dir]));
            y = Math.max(3, Math.min(MH - 4, y + SY[dir]));
        }
        plaza(x, y, 1, mat);
    }

    boolean isWater(int x, int y) {
        return x >= 0 && y >= 0 && x < MW && y < MH && map[x][y] == WATER;
    }

    boolean isGrass(int x, int y) {
        return x >= 0 && y >= 0 && x < MW && y < MH && map[x][y] == GRASS;
    }

    static boolean isPath(int t) {
        return t == DIRT || t == COBBLE || t == LEAF || t == GRAVEL;
    }

    static void fill(Graphics2D g, Color c, int x, int y, int w, int h) {
        g.setColor(c);
        g.fillRect(x, y, w, h);
    }

    /** Pre-renders the whole ground layer once; it is just blitted every frame. */
    void buildWorldImage() {
        Graphics2D g = world.createGraphics();
        Random r = new Random(3);
        for (int x = 0; x < MW; x++) {
            for (int y = 0; y < MH; y++) {
                int px = x * TILE, py = y * TILE;
                int t = map[x][y];
                switch (t) {
                    case WATER:
                        fill(g, WATER_C, px, py, TILE, TILE);
                        for (int i = 0; i < 3; i++)
                            fill(g, WATER_DARK, px + r.nextInt(12), py + r.nextInt(14), 3, 1);
                        if (r.nextInt(9) == 0) fill(g, new Color(0x3E7A4A), px + r.nextInt(12), py + r.nextInt(12), 3, 2);
                        break;
                    case DIRT:
                        fill(g, DIRT_C, px, py, TILE, TILE);
                        for (int i = 0; i < 7; i++) fill(g, DIRT_DARK, px + r.nextInt(15), py + r.nextInt(15), 1, 1);
                        for (int i = 0; i < 2; i++) fill(g, DIRT_LIGHT, px + r.nextInt(14), py + r.nextInt(15), 2, 1);
                        break;
                    case LEAF:
                        fill(g, LEAF_C, px, py, TILE, TILE);
                        for (int i = 0; i < 9; i++) {
                            Color c = i % 3 == 0 ? LEAF_O : (i % 3 == 1 ? LEAF_D : LEAF_R);
                            fill(g, c, px + r.nextInt(14), py + r.nextInt(15), 2, 1);
                        }
                        break;
                    case GRAVEL:
                        fill(g, GRAVEL_C, px, py, TILE, TILE);
                        for (int i = 0; i < 8; i++) fill(g, GRAVEL_L, px + r.nextInt(15), py + r.nextInt(15), 1, 1);
                        for (int i = 0; i < 8; i++) fill(g, GRAVEL_D, px + r.nextInt(14), py + r.nextInt(15), 2, 1);
                        if (r.nextInt(7) == 0) fill(g, new Color(0xE8E4D4), px + r.nextInt(12), py + r.nextInt(14), 3, 1);  // bone bit
                        break;
                    case COBBLE:
                        fill(g, COBBLE_C, px, py, TILE, TILE);
                        for (int row = 0; row < 3; row++) {
                            int y0 = row * 5, h = row == 2 ? 6 : 5, off = r.nextInt(6);
                            fill(g, COBBLE_LINE, px, py + y0 + h - 1, TILE, 1);
                            for (int k = 0; k < 3; k++) {
                                int vx = (off + k * 6) % 16;
                                fill(g, COBBLE_LINE, px + vx, py + y0, 1, h - 1);
                                fill(g, COBBLE_LIGHT, px + (vx + 2) % 15, py + y0 + 1, 2, 1);
                            }
                        }
                        break;
                    case CLIFF:
                        fill(g, WALL_C, px, py, TILE, TILE);
                        fill(g, WALL_L, px, py, TILE, 2);
                        fill(g, WALL_D, px, py + 5, TILE, 1);
                        fill(g, WALL_D, px, py + 10, TILE, 1);
                        fill(g, WALL_D, px, py + 15, TILE, 1);
                        fill(g, WALL_D, px + 5, py + 6, 1, 4);
                        fill(g, WALL_D, px + 11, py + 11, 1, 4);
                        fill(g, WALL_D, px + 9, py + 1, 1, 4);
                        if (r.nextInt(4) == 0) fill(g, new Color(0x3F6B3A), px + r.nextInt(12), py + 2, 3, 2);   // moss
                        break;
                    case FENCE:
                        fill(g, FENCE_BG, px, py, TILE, TILE);
                        fill(g, FENCE_BAR, px, py + 6, TILE, 1);
                        fill(g, FENCE_BAR, px, py + 12, TILE, 1);
                        for (int bx = 1; bx < 16; bx += 4) {
                            fill(g, FENCE_BAR, px + bx, py + 3, 2, 13);
                            fill(g, FENCE_TIP, px + bx, py + 1, 2, 2);
                        }
                        break;
                    default: // GRASS
                        fill(g, GRASS_C, px, py, TILE, TILE);
                        for (int i = 0; i < 4; i++) {
                            int tx = px + 1 + r.nextInt(14), ty = py + 2 + r.nextInt(13);
                            fill(g, GRASS_DARK, tx, ty, 1, 1);
                            if (r.nextInt(3) == 0) fill(g, GRASS_DARK, tx + 1, ty - 1, 1, 1);
                        }
                        if (r.nextInt(3) == 0)
                            fill(g, GRASS_LIGHT, px + r.nextInt(10), py + r.nextInt(14), 4, 1);
                        if (r.nextInt(7) == 0)          // fallen autumn leaf
                            fill(g, r.nextBoolean() ? LEAF_O : LEAF_R, px + r.nextInt(14), py + r.nextInt(14), 2, 1);
                        if (r.nextInt(16) == 0) {       // tiny pale-blue graveyard flower
                            int fx = px + 2 + r.nextInt(12), fy = py + 2 + r.nextInt(12);
                            fill(g, new Color(0x9CC8FF), fx, fy, 1, 1);
                            fill(g, Color.WHITE, fx + 1, fy, 1, 1);
                        }
                        // muddy shoreline where grass meets the swamp
                        if (isWater(x, y - 1)) { fill(g, EDGE, px, py, TILE, 3); fill(g, EDGE_DARK, px, py, TILE, 1); }
                        if (isWater(x, y + 1)) { fill(g, EDGE, px, py + 13, TILE, 3); fill(g, EDGE_DARK, px, py + 15, TILE, 1); }
                        if (isWater(x - 1, y)) { fill(g, EDGE, px, py, 3, TILE); fill(g, EDGE_DARK, px, py, 1, TILE); }
                        if (isWater(x + 1, y)) { fill(g, EDGE, px + 13, py, 3, TILE); fill(g, EDGE_DARK, px + 15, py, 1, TILE); }
                        break;
                }
                if (isPath(t)) {                       // ragged grass fringe along path edges
                    Color dark = t == DIRT ? DIRT_DARK : t == LEAF ? LEAF_D : t == GRAVEL ? GRAVEL_D : COBBLE_LINE;
                    for (int side = 0; side < 4; side++)
                        if (isGrass(x + SX[side == 0 ? 3 : side == 1 ? 1 : side == 2 ? 2 : 0],
                                y + SY[side == 0 ? 3 : side == 1 ? 1 : side == 2 ? 2 : 0]))
                            fringe(g, px, py, side, dark, r);
                }
            }
        }
        g.dispose();
    }

    /** side: 0 top, 1 bottom, 2 left, 3 right. */
    static void fringe(Graphics2D g, int px, int py, int side, Color dark, Random r) {
        for (int i = 0; i < TILE; i++) {
            int d = 1 + (r.nextInt(3) == 0 ? 1 : 0) + (r.nextInt(8) == 0 ? 1 : 0);
            switch (side) {
                case 0: fill(g, GRASS_C, px + i, py, 1, d); fill(g, dark, px + i, py + d, 1, 1); break;
                case 1: fill(g, GRASS_C, px + i, py + TILE - d, 1, d); fill(g, dark, px + i, py + TILE - d - 1, 1, 1); break;
                case 2: fill(g, GRASS_C, px, py + i, d, 1); fill(g, dark, px + d, py + i, 1, 1); break;
                default: fill(g, GRASS_C, px + TILE - d, py + i, d, 1); fill(g, dark, px + TILE - d - 1, py + i, 1, 1); break;
            }
        }
    }

    // ----------------------------------------------------------- prop picture makers
    static void branch(Graphics2D g, Random r, double x, double y, double ang, double len, int depth, int pass,
                       Color dark, Color mid, Color light) {
        double nx = x + Math.cos(ang) * len, ny = y + Math.sin(ang) * len;
        int w = depth + 1;
        int x0 = (int) Math.round(x), y0 = (int) Math.round(y), x1 = (int) Math.round(nx), y1 = (int) Math.round(ny);
        g.setStroke(new BasicStroke(pass == 0 ? w + 2 : w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(pass == 0 ? dark : mid);
        g.drawLine(x0, y0, x1, y1);
        if (pass == 1 && w >= 3) {
            g.setStroke(new BasicStroke(1f));
            g.setColor(light);
            g.drawLine(x0 - 1, y0, x1 - 1, y1);
        }
        if (depth > 0) {
            int n = 2 + (r.nextInt(3) == 0 ? 1 : 0);
            for (int i = 0; i < n; i++) {
                double a = ang + (i - (n - 1) / 2.0) * (0.55 + r.nextDouble() * 0.5) + (r.nextDouble() - 0.5) * 0.3;
                branch(g, r, nx, ny, a, len * (0.66 + r.nextDouble() * 0.12), depth - 1, pass, dark, mid, light);
            }
        }
    }

    /** Bare, twisted dead tree: 40x46 image, feet at (20,44). */
    static BufferedImage makeDeadTree(long seed) {
        BufferedImage im = new BufferedImage(40, 46, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        Color dark = new Color(0x1E1410), mid = new Color(0x5A4030), light = new Color(0x7E5E42);
        fill(g, dark, 14, 42, 12, 3);
        for (int pass = 0; pass < 2; pass++)
            branch(g, new Random(seed * 31 + 5), 20, 44, -Math.PI / 2, 13, 3, pass, dark, mid, light);
        fill(g, mid, 17, 41, 6, 3);
        g.dispose();
        return im;
    }

    /** Gravestone: 16x24, feet at (8,23). */
    static BufferedImage makeTomb(int style) {
        BufferedImage im = new BufferedImage(16, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        fill(g, S_DARK, 0, 21, 16, 3);                           // base slab
        fill(g, S_OUT, 0, 23, 16, 1);
        if (style == 1) {                                        // square stone with RIP
            fill(g, S_OUT, 1, 2, 14, 20);
            fill(g, S_MID, 2, 3, 12, 18);
            fill(g, S_LIGHT, 2, 3, 12, 1);
            fill(g, S_LIGHT, 2, 3, 1, 18);
            fill(g, S_DARK, 13, 4, 1, 17);
            String[] R = {"##.", "#.#", "##.", "#.#", "#.#"}, I = {"###", ".#.", ".#.", ".#.", "###"},
                    P = {"##.", "#.#", "##.", "#..", "#.."};
            String[][] word = {R, I, P};
            g.setColor(S_DARK);
            for (int k = 0; k < 3; k++)
                for (int j = 0; j < 5; j++)
                    for (int i = 0; i < 3; i++)
                        if (word[k][j].charAt(i) == '#') g.fillRect(3 + k * 4 + i, 6 + j, 1, 1);
            fill(g, S_DARK, 4, 14, 8, 1);
        } else {                                                 // rounded stones
            g.setColor(S_OUT);
            g.fillRect(2, 8, 12, 14);
            g.fillOval(2, 2, 12, 12);
            g.setColor(S_MID);
            g.fillRect(3, 8, 10, 13);
            g.fillOval(3, 3, 10, 10);
            fill(g, S_LIGHT, 4, 5, 2, 15);
            fill(g, S_DARK, 11, 8, 2, 13);
            if (style == 3) {                                    // skull carving
                fill(g, S_DARK, 5, 7, 6, 4);
                fill(g, S_OUT, 6, 8, 1, 2);
                fill(g, S_OUT, 9, 8, 1, 2);
                fill(g, S_DARK, 6, 11, 4, 2);
                fill(g, S_OUT, 7, 12, 1, 1);
                fill(g, S_OUT, 9, 12, 1, 1);
            } else {                                             // engraved cross
                fill(g, S_DARK, 7, 7, 2, 8);
                fill(g, S_DARK, 5, 9, 6, 2);
            }
            if (style == 2) {                                    // cracked & mossy
                fill(g, S_OUT, 9, 4, 1, 3);
                fill(g, S_OUT, 8, 6, 1, 3);
                fill(g, new Color(0x4C7A3C), 3, 18, 6, 3);
                fill(g, new Color(0x3A5E30), 4, 17, 3, 1);
            }
        }
        g.dispose();
        return im;
    }

    /** Grave cross: 14x24, feet at (7,23). */
    static BufferedImage makeCross(int style) {
        BufferedImage im = new BufferedImage(14, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        if (style == 0) {                                        // wooden cross on a mound
            fill(g, new Color(0x2A2018), 1, 19, 12, 5);
            fill(g, new Color(0x3A2E22), 2, 19, 10, 4);
            fill(g, new Color(0x2A1C10), 5, 2, 4, 20);
            fill(g, new Color(0x2A1C10), 1, 6, 12, 4);
            fill(g, new Color(0x6B4A2F), 6, 3, 2, 18);
            fill(g, new Color(0x6B4A2F), 2, 7, 10, 2);
            fill(g, new Color(0x8A6440), 6, 3, 1, 17);
        } else {                                                 // stone cross (style 2: celtic ring)
            fill(g, S_OUT, 1, 19, 12, 5);
            fill(g, S_DARK, 2, 20, 10, 3);
            fill(g, S_OUT, 4, 1, 6, 20);
            fill(g, S_OUT, 0, 5, 14, 6);
            fill(g, S_MID, 5, 2, 4, 18);
            fill(g, S_MID, 1, 6, 12, 4);
            fill(g, S_LIGHT, 5, 2, 1, 17);
            fill(g, S_LIGHT, 1, 6, 12, 1);
            if (style == 2) {
                g.setColor(S_OUT);
                g.fillOval(1, 2, 12, 12);
                g.setColor(S_MID);
                g.fillOval(2, 3, 10, 10);
                g.setComposite(AlphaComposite.Clear);
                g.fillOval(4, 5, 6, 6);
                g.setComposite(AlphaComposite.SrcOver);
                fill(g, S_MID, 5, 2, 4, 18);
                fill(g, S_MID, 1, 6, 12, 4);
                fill(g, S_LIGHT, 5, 2, 1, 17);
            }
        }
        g.dispose();
        return im;
    }

    /** Stone obelisk: 12x30, feet at (6,29). */
    static BufferedImage makeObelisk() {
        BufferedImage im = new BufferedImage(12, 30, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        fill(g, S_OUT, 0, 25, 12, 5);
        fill(g, S_DARK, 1, 26, 10, 3);
        for (int y = 0; y < 25; y++) {
            int w = 2 + y * 6 / 25;                 // tapering shaft
            int x0 = 6 - w - 1;
            fill(g, S_OUT, x0, y, w * 2 + 2, 1);
            fill(g, S_MID, x0 + 1, y, w * 2, 1);
            fill(g, S_LIGHT, x0 + 1, y, Math.max(1, w / 2), 1);
            fill(g, S_DARK, x0 + w * 2 - 1, y, 1, 1);
        }
        fill(g, S_DARK, 4, 12, 4, 1);
        fill(g, S_DARK, 4, 16, 4, 1);
        g.dispose();
        return im;
    }

    /** Glowing jack-o'-lantern: 14x12, feet at (7,11). */
    static BufferedImage makePumpkin(int v) {
        BufferedImage im = new BufferedImage(14, 12, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        Color body = v == 2 ? new Color(0xD8601A) : new Color(0xE87A1E);
        g.setColor(new Color(0x5A2A0E));
        g.fillOval(0, 3, 14, 9);
        g.setColor(body);
        g.fillOval(1, 3, 12, 8);
        fill(g, new Color(0xB4531A), 5, 4, 1, 7);
        fill(g, new Color(0xB4531A), 9, 4, 1, 7);
        fill(g, new Color(0x4A6B2A), 6, 0, 2, 3);
        fill(g, new Color(0x6E9A3A), 7, 0, 1, 2);
        Color glow = new Color(0xFFE070);
        fill(g, glow, 3, 5, 3, 1);
        fill(g, glow, 4, 6, 1, 1);
        fill(g, glow, 8, 5, 3, 1);
        fill(g, glow, 9, 6, 1, 1);
        if (v == 1) {                                  // toothy grin
            fill(g, glow, 3, 8, 8, 2);
            fill(g, body, 5, 8, 1, 1);
            fill(g, body, 8, 9, 1, 1);
        } else {                                       // zig-zag grin
            fill(g, glow, 3, 8, 1, 1);
            fill(g, glow, 4, 9, 2, 1);
            fill(g, glow, 6, 8, 2, 1);
            fill(g, glow, 8, 9, 2, 1);
            fill(g, glow, 10, 8, 1, 1);
        }
        g.dispose();
        return im;
    }

    /** Iron lamp post with a glowing lantern: 10x30, feet at (5,29). */
    static BufferedImage makeLamp() {
        BufferedImage im = new BufferedImage(10, 30, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        fill(g, new Color(0x1A1A26), 4, 9, 2, 20);
        fill(g, new Color(0x1A1A26), 2, 26, 6, 4);
        fill(g, new Color(0x3A3A52), 3, 27, 4, 2);
        fill(g, new Color(0x1A1A26), 0, 0, 10, 10);
        fill(g, new Color(0x4A78B0), 1, 1, 8, 8);
        fill(g, new Color(0xFFD060), 2, 2, 6, 6);
        fill(g, new Color(0xFFF2B0), 3, 3, 3, 3);
        fill(g, new Color(0x2A4A78), 0, 0, 10, 2);
        g.dispose();
        return im;
    }

    /** Stone mausoleum: 48x48, feet at (24,47). */
    static BufferedImage makeCrypt(int style) {
        BufferedImage im = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = im.createGraphics();
        Random r = new Random(40 + style);
        fill(g, S_OUT, 2, 0, 44, 25);                       // roof outline + body outline
        fill(g, S_OUT, 4, 21, 40, 27);
        fill(g, new Color(0x6A6A7E), 4, 2, 20, 20);         // roof, lit left half
        fill(g, new Color(0x55556A), 24, 2, 20, 20);        // roof, shaded right half
        fill(g, S_OUT, 23, 2, 2, 20);                       // ridge
        fill(g, new Color(0x7E7E94), 4, 2, 20, 2);
        for (int i = 0; i < 14; i++)                        // roof seams, moss and fallen leaves
            fill(g, i % 3 == 0 ? new Color(0x3F6B3A) : (i % 3 == 1 ? LEAF_O : new Color(0x47475C)),
                    5 + r.nextInt(38), 4 + r.nextInt(16), 2, 1);
        fill(g, new Color(0x4A4A5C), 6, 22, 36, 26);        // front wall
        fill(g, new Color(0x33333F), 6, 22, 5, 26);         // pillars
        fill(g, new Color(0x33333F), 37, 22, 5, 26);
        fill(g, new Color(0x5E5E74), 6, 22, 36, 2);         // lintel
        fill(g, new Color(0xE8E4D4), 22, 24, 4, 3);         // skull over the door
        fill(g, S_OUT, 23, 25, 1, 1);
        fill(g, S_OUT, 25, 25, 1, 1);
        fill(g, new Color(0x15151D), 14, 28, 20, 20);       // doorway
        Color bar = style == 0 ? new Color(0x5A8AC0) : new Color(0x8A4A2A);
        for (int x = 16; x <= 31; x += 3) fill(g, bar, x, 29, 1, 19);
        fill(g, bar, 15, 33, 18, 1);
        g.dispose();
        return im;
    }

    // ------------------------------------------------------------------ placement
    static double dist(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }

    Prop makeProp(int kind, int variant, double x, double y) {
        Prop p = new Prop();
        p.setKind(kind);
        p.setX(x);
        p.setY(y);
        switch (kind) {
            case P_TREE:    p.setSprite(TREE_IMG[variant % TREE_IMG.length], 20, 44); p.setHitbox(3, 4); break;
            case P_TOMB:    p.setSprite(TOMB_IMG[variant % TOMB_IMG.length], 8, 23); p.setHitbox(6, 4); break;
            case P_CROSS:   p.setSprite(CROSS_IMG[variant % CROSS_IMG.length], 7, 23); p.setHitbox(4, 3); break;
            case P_OBELISK: p.setSprite(OBELISK_IMG[0], 6, 29); p.setHitbox(4, 3); break;
            case P_PUMPKIN: p.setSprite(PUMPKIN_IMG[variant % PUMPKIN_IMG.length], 7, 11); p.setHitbox(5, 2);
                p.setLight(new Color(255, 150, 40, 26), 24); break;
            case P_LAMP:    p.setSprite(LAMP_IMG[0], 5, 29); p.setHitbox(3, 3);
                p.setLight(new Color(255, 200, 80, 28), 46); break;
            default:        p.setSprite(CRYPT_IMG[variant % CRYPT_IMG.length], 24, 47); p.setHitbox(20, 18); break;
        }
        return p;
    }

    boolean grassAt(double px, double py) {
        return isGrass((int) Math.floor(px / TILE), (int) Math.floor(py / TILE));
    }

    boolean freeSpot(double px, double py, double minD) {
        if (dist(px, py, spawnX, spawnY) < 56 || dist(px, py, shrine.getX(), shrine.getY()) < 36) return false;
        for (Prop o : props) if (dist(px, py, o.getX(), o.getY()) < minD) return false;
        return true;
    }

    void placeProps() {
        Random r = new Random(11);
        // crypts near a few crossroads
        int[] cryptHubs = {0, 2, 6, 9, 11};
        for (int h : cryptHubs) {
            for (int attempt = 0; attempt < 60; attempt++) {
                int tx = hubX[h] + r.nextInt(19) - 9, ty = hubY[h] - 4 - r.nextInt(5);
                boolean ok = true;
                for (int dx = -2; dx <= 2 && ok; dx++)
                    for (int dy = -3; dy <= 0; dy++)
                        if (!isGrass(tx + dx, ty + dy)) { ok = false; break; }
                double px = tx * TILE + 8, py = ty * TILE + 14;
                if (!ok || !freeSpot(px, py, 56)) continue;
                props.add(makeProp(P_CRYPT, r.nextInt(2), px, py));
                break;
            }
        }
        // lamp posts around the plazas
        for (int i = 0; i < 12; i++) {
            props.add(makeProp(P_LAMP, 0, (hubX[i] - 3) * TILE + 8, (hubY[i] - 3) * TILE + 14));
            props.add(makeProp(P_LAMP, 0, (hubX[i] + 3) * TILE + 8, (hubY[i] - 3) * TILE + 14));
        }
        // dead trees
        int tries = 0, trees = 0;
        while (trees < 70 && tries++ < 6000) {
            int tx = 3 + r.nextInt(MW - 6), ty = 3 + r.nextInt(MH - 6);
            boolean ok = true;
            for (int dx = -1; dx <= 1 && ok; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    if (!isGrass(tx + dx, ty + dy)) { ok = false; break; }
            double px = tx * TILE + 8, py = ty * TILE + 14;
            if (!ok || !freeSpot(px, py, 34)) continue;
            props.add(makeProp(P_TREE, r.nextInt(4), px, py));
            trees++;
        }
        // grave plots: clusters of stones, crosses and the odd obelisk
        for (int c = 0; c < 26; c++) {
            double cx = (4 + r.nextInt(MW - 8)) * TILE, cy = (4 + r.nextInt(MH - 8)) * TILE;
            int n = 5 + r.nextInt(6);
            for (int k = 0; k < n; k++) {
                double px = cx + r.nextInt(81) - 40, py = cy + r.nextInt(61) - 30;
                if (!grassAt(px - 8, py) || !grassAt(px + 8, py) || !grassAt(px, py - 10) || !freeSpot(px, py, 20)) continue;
                int roll = r.nextInt(100);
                if (roll < 58) props.add(makeProp(P_TOMB, r.nextInt(4), px, py));
                else if (roll < 88) props.add(makeProp(P_CROSS, r.nextInt(3), px, py));
                else props.add(makeProp(P_OBELISK, 0, px, py));
            }
        }
        // pumpkins lurking near the graves
        int pumps = 0;
        tries = 0;
        List<Prop> snapshot = new ArrayList<Prop>(props);
        while (pumps < 34 && tries++ < 3000 && !snapshot.isEmpty()) {
            Prop base = snapshot.get(r.nextInt(snapshot.size()));
            if (base.getKind() == P_LAMP || base.getKind() == P_CRYPT) continue;
            double px = base.getX() + r.nextInt(41) - 20, py = base.getY() + r.nextInt(25) - 8;
            if (!grassAt(px - 6, py) || !grassAt(px + 6, py) || !grassAt(px, py - 6) || !freeSpot(px, py, 14)) continue;
            props.add(makeProp(P_PUMPKIN, r.nextInt(3), px, py));
            pumps++;
        }
    }

    void placeMonsters() {
        Random r = new Random(5);
        int tries = 0;
        while (monsters.size() < 18 && tries++ < 3000) {
            double x = (3 + r.nextInt(MW - 6)) * TILE + 8, y = (3 + r.nextInt(MH - 6)) * TILE + 12;
            if (dist(x, y, spawnX, spawnY) < 110 || blocked(x, y)) continue;
            Monster m = new Monster();
            m.setPosition(x, y);
            m.setHome(x, y);
            m.setPhase(r.nextDouble() * 3);
            m.setKind(monsters.size() % MON_SPR.length);
            m.setHp(MON_HP[m.getKind()]);
            monsters.add(m);
        }
    }

    // ----------------------------------------------------------------- collision
    boolean solidTile(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= MW || ty >= MH) return true;
        int t = map[tx][ty];
        return t == WATER || t == CLIFF || t == FENCE;
    }

    /** Feet box: 8 wide, 4 tall, with (x, y) at the bottom centre. */
    boolean blocked(double x, double y) {
        double[] xs = {x - 4, x + 3.99};
        double[] ys = {y - 4, y};
        for (double cx : xs)
            for (double cy : ys)
                if (solidTile((int) Math.floor(cx / TILE), (int) Math.floor(cy / TILE))) return true;
        for (Prop t : props)
            if (x + 4 > t.getX() - t.getHw() && x - 4 < t.getX() + t.getHw() && y > t.getY() - t.getHd() && y - 4 < t.getY()) return true;
        return false;
    }

    void tryMove(Entity e, double dx, double dy) {
        if (!blocked(e.getX() + dx, e.getY())) e.setX(e.getX() + dx);
        if (!blocked(e.getX(), e.getY() + dy)) e.setY(e.getY() + dy);
    }

    /** Ghosts and wraiths drift over graves, walls and ponds; only the map edge stops them. */
    void tryFly(Entity e, double dx, double dy) {
        double nx = e.getX() + dx, ny = e.getY() + dy;
        if (nx > 36 && nx < MW * TILE - 36) e.setX(nx);
        if (ny > 36 && ny < MH * TILE - 36) e.setY(ny);
    }

    // -------------------------------------------------------------------- update
    boolean down(int code) {
        return code < keys.length && keys[code];
    }

    void update(double dt) {
        time += dt;
        if (msgT > 0) msgT -= dt;
        updateLeaves(dt);
        if (title) return;
        updateMonsters(dt);
        if (player.getHealth() <= 0) {
            if (down(KeyEvent.VK_ENTER)) respawn();
            return;
        }
        updatePlayer(dt);
    }

    void updatePlayer(double dt) {
        Player p = player;
        double ax = 0, ay = 0;
        if (down(KeyEvent.VK_LEFT) || down(KeyEvent.VK_A)) ax -= 1;
        if (down(KeyEvent.VK_RIGHT) || down(KeyEvent.VK_D)) ax += 1;
        if (down(KeyEvent.VK_UP) || down(KeyEvent.VK_W)) ay -= 1;
        if (down(KeyEvent.VK_DOWN) || down(KeyEvent.VK_S)) ay += 1;
        p.setMoving(ax != 0 || ay != 0);

        if (p.getAtk() <= 0) {                  // can't turn mid-attack
            if (ay != 0) p.setDir(ay > 0 ? 0 : 1);
            if (ax != 0) p.setDir(ax < 0 ? 2 : 3);
        }
        if (p.isMoving()) {
            double len = Math.sqrt(ax * ax + ay * ay);
            double speed = (p.getAtk() > 0 ? 28 : 60) * dt;
            tryMove(p, ax / len * speed, ay / len * speed);
            p.setAnim(p.getAnim() + dt);
        }
        if (Math.abs(p.getKx()) + Math.abs(p.getKy()) > 1) {   // knockback
            tryMove(p, p.getKx() * dt, p.getKy() * dt);
            double f = Math.max(0, 1 - 8 * dt);
            p.setKx(p.getKx() * f);
            p.setKy(p.getKy() * f);
        }
        if (p.getInv() > 0) p.setInv(p.getInv() - dt);
        if (p.getCool() > 0) p.setCool(p.getCool() - dt);
        if (p.getAtk() > 0) {
            p.setAtk(p.getAtk() - dt);
            if (!p.isHitDone() && p.getAtk() < 0.2) {
                p.setHitDone(true);
                swordHit();
            }
        } else if (down(KeyEvent.VK_SPACE) && p.getCool() <= 0) {
            p.setAtk(ATTACK_TIME);
            p.setCool(0.45);
            p.setHitDone(false);
        }
    }

    void swordHit() {
        double hx = player.getX() + DX[player.getDir()] * 15, hy = player.getY() - 6 + DY[player.getDir()] * 15;
        for (Monster s : monsters) {
            if (s.getDead() > 0) continue;
            if (Math.abs(s.getX() - hx) < 14 && Math.abs(s.getY() - 4 - hy) < 14) {
                s.setHp(s.getHp() - 1);
                s.setHurt(0.2);
                s.setVx(DX[player.getDir()] * 110);
                s.setVy(DY[player.getDir()] * 110);
                if (s.getHp() <= 0) {
                    s.setDead(8);      // respawns after 8 seconds
                    kills++;
                    if (kills > best) {
                        best = kills;
                        persist();
                    }
                }
            }
        }
    }

    void moveMonster(Monster m, double dx, double dy) {
        if (MON_FLY[m.getKind()]) tryFly(m, dx, dy); else tryMove(m, dx, dy);
    }

    void updateMonsters(double dt) {
        for (Monster s : monsters) {
            if (s.getDead() > 0) {
                s.setDead(s.getDead() - dt);
                if (s.getDead() <= 0) {
                    s.setDead(0);
                    s.setHp(MON_HP[s.getKind()]);
                    s.setX(s.getHomeX());
                    s.setY(s.getHomeY());
                    s.setVx(0);
                    s.setVy(0);
                }
                continue;
            }
            if (s.getHurt() > 0) s.setHurt(s.getHurt() - dt);
            int before = (int) (s.getPhase() * 2);
            s.setPhase(s.getPhase() + dt);
            if ((int) (s.getPhase() * 2) != before) chooseDirection(s);
            double cyc = s.getPhase() - Math.floor(s.getPhase());
            double sp = MON_SPEED[s.getKind()] * dt;
            if (s.getKind() == 3) {                                   // pumpkin heads hop
                if (cyc < 0.4) moveMonster(s, s.getDirx() * sp, s.getDiry() * sp);
            } else {
                moveMonster(s, s.getDirx() * sp, s.getDiry() * sp);
            }
            if (s.getDirx() > 0.2) s.setFlip(true); else if (s.getDirx() < -0.2) s.setFlip(false);
            if (Math.abs(s.getVx()) + Math.abs(s.getVy()) > 1) {
                moveMonster(s, s.getVx() * dt, s.getVy() * dt);
                double f = Math.max(0, 1 - 8 * dt);
                s.setVx(s.getVx() * f);
                s.setVy(s.getVy() * f);
            }
            if (player.getHealth() > 0 && player.getInv() <= 0 && dist(s.getX(), s.getY(), player.getX(), player.getY()) < 9) hurtPlayer(s);
        }
    }

    void chooseDirection(Monster s) {
        double dx = player.getX() - s.getX(), dy = player.getY() - s.getY(), d = Math.hypot(dx, dy);
        if (player.getHealth() > 0 && d < MON_CHASE[s.getKind()] && d > 0.001) {
            s.setDirx(dx / d);
            s.setDiry(dy / d);
        } else if (rnd.nextInt(3) == 0) {
            s.setDirx(0);
            s.setDiry(0);
        } else {
            double a = rnd.nextDouble() * Math.PI * 2;
            s.setDirx(Math.cos(a));
            s.setDiry(Math.sin(a));
        }
    }

    void hurtPlayer(Monster s) {
        player.setHealth(player.getHealth() - 1);
        player.setInv(1.0);
        double dx = player.getX() - s.getX(), dy = player.getY() - s.getY(), d = Math.hypot(dx, dy);
        if (d < 0.001) { dx = 0; dy = 1; d = 1; }
        player.setKx(dx / d * 130);
        player.setKy(dy / d * 130);
    }

    void updateLeaves(double dt) {
        for (int i = 0; i < leafX.length; i++) {
            leafX[i] += (7 + i % 5 * 2) * dt;
            leafY[i] += (9 + i % 7 * 2) * dt;
            if (leafX[i] > VIEW_W) leafX[i] = -2;
            if (leafY[i] > VIEW_H) leafY[i] = -2;
        }
    }

    boolean nearShrine() {
        return dist(player.getX(), player.getY(), shrine.getX(), shrine.getY()) < 20;
    }

    void respawn() {
        player.setHealth(3);
        player.setInv(1.5);
        player.setKx(0);
        player.setKy(0);
        applySave();
    }

    /** Puts the player back at the last save point (or the spawn if there is none). */
    void applySave() {
        player.setX(savedX);
        player.setY(savedY);
        kills = savedKills;
    }

    void newGame() {
        hasSave = false;
        savedX = spawnX;
        savedY = spawnY;
        savedKills = 0;
        player.setHealth(3);
        player.setInv(0);
        player.setAtk(0);
        player.setKx(0);
        player.setKy(0);
        applySave();
        for (Monster s : monsters) {
            s.setHp(MON_HP[s.getKind()]);
            s.setDead(0);
            s.setX(s.getHomeX());
            s.setY(s.getHomeY());
            s.setVx(0);
            s.setVy(0);
        }
        persist();
        title = false;
    }

    // ---------------------------------------------------------------- save / load
    void save() {
        savedX = player.getX();
        savedY = player.getY();
        savedKills = kills;
        hasSave = true;
        msg = persist() ? "GAME SAVED" : "SAVE FAILED";
        msgT = 2.0;
    }

    /** Writes the save point and the best score to disk. */
    boolean persist() {
        Properties p = new Properties();
        p.setProperty("saved", String.valueOf(hasSave));
        p.setProperty("x", String.valueOf(savedX));
        p.setProperty("y", String.valueOf(savedY));
        p.setProperty("kills", String.valueOf(savedKills));
        p.setProperty("best", String.valueOf(best));
        try (FileOutputStream out = new FileOutputStream(SAVE_FILE)) {
            p.store(out, "Graveyard Adventure save");
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    void load() {
        if (!SAVE_FILE.exists()) return;
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(SAVE_FILE)) {
            p.load(in);
            best = Integer.parseInt(p.getProperty("best", "0"));
            if (Boolean.parseBoolean(p.getProperty("saved", "false"))) {
                double x = Double.parseDouble(p.getProperty("x"));
                double y = Double.parseDouble(p.getProperty("y"));
                if (!blocked(x, y)) {
                    savedX = x;
                    savedY = y;
                    savedKills = Integer.parseInt(p.getProperty("kills", "0"));
                    hasSave = true;
                }
            }
        } catch (Exception ex) {
            // corrupt or missing save: just start from the default spawn
        }
    }

    // ------------------------------------------------------------------ rendering
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D fg = frame.createGraphics();
        fg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        fg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        fg.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        render(fg);
        fg.dispose();

        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int s = Math.max(1, Math.min(getWidth() / VIEW_W, getHeight() / VIEW_H));   // whole-number scale
        int w = VIEW_W * s, h = VIEW_H * s;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(frame, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
    }

    void render(Graphics2D g) {
        int camX = (int) Math.max(0, Math.min(MW * TILE - VIEW_W, player.getX() - VIEW_W / 2));
        int camY = (int) Math.max(0, Math.min(MH * TILE - VIEW_H, player.getY() - 8 - VIEW_H / 2));

        g.drawImage(world, -camX, -camY, null);
        drawWaterRipples(g, camX, camY);

        // collect everything visible, then sort by Y so things lower on screen draw on top
        List<Entity> list = new ArrayList<Entity>();
        List<Light> lights = new ArrayList<Light>();
        for (Prop t : props) {
            if (t.getX() > camX - 30 && t.getX() < camX + VIEW_W + 30 && t.getY() > camY - 8 && t.getY() < camY + VIEW_H + 54) {
                list.add(t);
                if (t.getTint() != null) {
                    int fl = (int) Math.round(Math.sin(time * 7 + t.getX()) * 1.5);
                    lights.add(new Light((int) t.getX() - camX, (int) t.getY() - 8 - camY, t.getLightR() + fl, t.getTint()));
                }
            }
        }
        for (Monster s : monsters) {
            if (s.getDead() > 0) continue;
            list.add(s);
            if (s.getKind() == 3) lights.add(new Light((int) s.getX() - camX, (int) s.getY() - 8 - camY, 22, new Color(255, 150, 40, 26)));
            if (s.getKind() == 1 || s.getKind() == 4) lights.add(new Light((int) s.getX() - camX, (int) s.getY() - 12 - camY, 20, new Color(100, 220, 255, 22)));
        }
        list.add(shrine);
        list.add(player);
        lights.add(new Light((int) shrine.getX() - camX, (int) shrine.getY() - 14 - camY, 52, new Color(255, 190, 70, 28)));
        if (player.getHealth() > 0)
            lights.add(new Light((int) Math.round(player.getX()) - camX, (int) Math.round(player.getY()) - 8 - camY, 40,
                    new Color(255, 200, 90, 24)));

        // ground shadows first, so no shadow ever covers a sprite
        g.setColor(SHADOW);
        for (Entity e : list) {
            int sx = (int) Math.round(e.getX()) - camX, sy = (int) Math.round(e.getY()) - camY;
            if (e instanceof Prop) {
                int hw = (int) ((Prop) e).getHw();
                g.fillOval(sx - hw - 3, sy - 3, hw * 2 + 6, 6);
            } else if (e instanceof Shrine) g.fillOval(sx - 5, sy - 2, 10, 4);
            else g.fillOval(sx - 6, sy - 3, 12, 5);
        }

        Collections.sort(list, (a, b) -> Double.compare(a.getY(), b.getY()));
        for (Entity e : list) {
            if (e instanceof Prop) {
                Prop t = (Prop) e;
                g.drawImage(t.getImg(), (int) t.getX() - t.getOx() - camX, (int) t.getY() - t.getOy() - camY, null);
            } else if (e instanceof Monster) {
                drawMonster(g, (Monster) e, camX, camY);
            } else if (e instanceof Shrine) {
                drawShrine(g, camX, camY);
            } else {
                drawPlayer(g, camX, camY);
            }
        }

        // night: moonlit darkness with warm pools of light, then drifting fog and leaves
        drawLighting(g, lights);
        drawFog(g);
        drawLeaves(g);
        drawHud(g);

        if (title) {
            // the title page draws its own text
        } else if (msgT > 0) {
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            drawCentered(g, msg, VIEW_W / 2, VIEW_H - 22, Color.WHITE);
        } else if (player.getHealth() > 0 && nearShrine()) {
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 8));
            drawCentered(g, "E: SAVE", (int) shrine.getX() - camX, (int) shrine.getY() - camY - 28, Color.WHITE);
        }

        if (player.getHealth() <= 0 && !title) {
            g.setColor(new Color(20, 0, 10, 160));
            g.fillRect(0, 0, VIEW_W, VIEW_H);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
            drawCentered(g, "YOU DIED", VIEW_W / 2, VIEW_H / 2, new Color(0xFF5A6E));
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 8));
            drawCentered(g, "PRESS ENTER TO RESPAWN", VIEW_W / 2, VIEW_H / 2 + 16, Color.WHITE);
            drawCentered(g, "SCORE " + kills + "   BEST " + best, VIEW_W / 2, VIEW_H / 2 + 30, new Color(0xFFD866));
        }

        if (title) drawTitle(g);
    }

    void drawLighting(Graphics2D g, List<Light> lights) {
        Graphics2D lg = lightMap.createGraphics();
        lg.setComposite(AlphaComposite.Src);
        lg.setColor(NIGHT);
        lg.fillRect(0, 0, VIEW_W, VIEW_H);
        lg.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT, 0.30f));
        lg.setColor(Color.WHITE);
        for (Light l : lights) {
            for (int k = 0; k < 4; k++) {               // stepped rings keep the glow pixelated
                int rr = (int) (l.getRadius() * (1 - k * 0.24));
                lg.fillOval(l.getX() - rr, l.getY() - rr, rr * 2, rr * 2);
            }
        }
        lg.dispose();
        g.drawImage(lightMap, 0, 0, null);
        for (Light l : lights) {
            g.setColor(l.getTint());
            for (int k = 0; k < 2; k++) {
                int rr = (int) (l.getRadius() * (0.9 - k * 0.4));
                g.fillOval(l.getX() - rr, l.getY() - rr, rr * 2, rr * 2);
            }
        }
    }

    void drawFog(Graphics2D g) {
        g.setColor(new Color(190, 205, 255, 13));
        for (int i = 0; i < 10; i++) {
            int fx = (int) ((i * 97 + time * (5 + i % 3 * 3)) % (VIEW_W + 100)) - 50;
            int fy = (i * 53) % VIEW_H;
            g.fillOval(fx, fy, 80, 16);
            g.fillOval(fx + 14, fy - 5, 50, 12);
        }
    }

    void drawLeaves(Graphics2D g) {
        for (int i = 0; i < leafX.length; i++) {
            int x = (int) leafX[i] + (int) Math.round(Math.sin(time * 2 + i) * 2), y = (int) leafY[i];
            if (i % 6 == 0) {                            // wandering wisps
                if (((int) (time * 3 + i)) % 2 == 0) fill(g, new Color(0x9FFFEF), x, y, 1, 1);
                continue;
            }
            Color c = i % 3 == 0 ? LEAF_O : (i % 3 == 1 ? LEAF_R : LEAF_D);
            fill(g, c, x, y, 1, 1);
            if (i % 3 == 0) fill(g, c, x + 1, y, 1, 1);
        }
    }

    // ----------------------------------------------------------------- title page
    void drawTitle(Graphics2D g) {
        // night sky
        g.setPaint(new GradientPaint(0, 0, new Color(0x161432), 0, 110, new Color(0x3C3C5C)));
        g.fillRect(0, 0, VIEW_W, VIEW_H);

        // the big moon, with a halo and craters
        int mx = 196, my = 56, mr = 30;
        for (int k = 3; k >= 1; k--) {
            g.setColor(new Color(220, 225, 255, 16));
            g.fillOval(mx - mr - k * 5, my - mr - k * 5, (mr + k * 5) * 2, (mr + k * 5) * 2);
        }
        g.setColor(new Color(0xF6F6FF));
        g.fillOval(mx - mr, my - mr, mr * 2, mr * 2);
        g.setColor(new Color(0xC8CCE4));
        g.fillOval(mx - 18, my - 14, 10, 10);
        g.fillOval(mx + 4, my + 2, 14, 14);
        g.fillOval(mx - 8, my + 14, 8, 8);
        g.fillOval(mx + 12, my - 18, 7, 7);

        // drifting clouds (some pass in front of the moon)
        for (int i = 0; i < 14; i++) {
            int cx = (int) ((i * 61 + time * (3 + i % 3)) % (VIEW_W + 90)) - 45;
            int cy = 6 + (i * 37) % 70, cw = 50 + (i * 13) % 40;
            g.setColor(i % 2 == 0 ? new Color(0x4A4C6E, false) : new Color(40, 40, 72, 190));
            g.fillOval(cx, cy, cw, 14);
            g.fillOval(cx + 8, cy - 6, cw - 22, 12);
        }

        // bats
        for (int i = 0; i < 4; i++) {
            int bx = (int) ((time * 22 + i * 70) % (VIEW_W + 20)) - 10;
            int by = 14 + i * 13 + (int) Math.round(Math.sin(time * 3 + i) * 4);
            boolean up = ((int) (time * 8 + i)) % 2 == 0;
            fill(g, OUTLINE, bx, by, 2, 2);
            fill(g, OUTLINE, bx - 3, by + (up ? -2 : 1), 3, 1);
            fill(g, OUTLINE, bx + 2, by + (up ? -2 : 1), 3, 1);
        }

        // far tombstones, ground, and the dead trees framing the scene
        g.setColor(new Color(0x2A2A44));
        g.fillRect(0, 108, VIEW_W, 6);
        for (int x = 8; x < VIEW_W; x += 26) g.drawImage(TOMB_IMG[(x / 26) % 4], x, 94, null);
        g.drawImage(TREE_IMG[0], -14, 22, 80, 92, null);
        g.drawImage(TREE_IMG[2], 196, 18, 80, 92, null);
        g.setColor(new Color(0x1B3326));
        g.fillRect(0, 112, VIEW_W, VIEW_H - 112);
        for (int x = 0; x < VIEW_W; x += 4) {              // grass blades
            int h = 2 + ((x * 7919) % 5);
            fill(g, GRASS_DARK, x, 112 - h + 3, 1, h);
            fill(g, GRASS_C, x + 2, 112 - h + 5, 1, h - 1);
        }
        g.setColor(new Color(0x2F5238));
        for (int x = 0; x < VIEW_W; x += 3) g.fillRect(x, 113 + ((x * 31) % 4), 2, 1);
        for (int i = 0; i < 4; i++) {                       // glowing pumpkins
            int px = new int[]{14, 78, 176, 238}[i], py = 132;
            g.setColor(new Color(255, 150, 40, 30));
            g.fillOval(px - 22, py - 20, 44, 34);
            g.setColor(new Color(255, 150, 40, 30));
            g.fillOval(px - 13, py - 13, 26, 20);
            g.drawImage(PUMPKIN_IMG[i % 3], px - 7, py - 11, null);
        }

        // title text
        int bob = (int) Math.round(Math.sin(time * 3) * 3);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        drawCentered(g, "GRAVEYARD", 112, 36 + bob, new Color(0xFF9A3C));
        drawCentered(g, "ADVENTURE", 112, 58 + bob, new Color(0xE8E4F4));

        // the hero with his monsters
        int hx = VIEW_W / 2 - 6, hy = 112 + (int) Math.round(Math.sin(time * 4));
        drawSpr(g, SPR_DOWN, hx, hy, false, 16, false, false);
        drawSword(g, hx - 1, hy + 9, Math.PI / 2 + 0.35, 9);
        double[] sx = {34, 62, 92, 164, 194, 222};
        int[] kinds = {0, 1, 2, 3, 4, 0};
        for (int i = 0; i < sx.length; i++) {
            Monster d = new Monster();
            d.setX(sx[i]);
            d.setY(128);
            d.setKind(kinds[i]);
            d.setPhase(time + i * 0.35);
            d.setFlip(sx[i] < 128);
            drawMonster(g, d, 0, 0);
        }

        // menu panel
        g.setColor(new Color(6, 4, 16, 170));
        g.fillRect(0, 138, VIEW_W, VIEW_H - 138);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 9));
        drawCentered(g, "BEST SCORE: " + best, VIEW_W / 2, 148, new Color(0xFFD866));
        if (((int) (time * 2)) % 2 == 0)
            drawCentered(g, hasSave ? "ENTER: CONTINUE" : "ENTER: START", VIEW_W / 2, 159, Color.WHITE);
        if (hasSave) drawCentered(g, "N: NEW GAME", VIEW_W / 2, 169, new Color(0xBBBBBB));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 8));
        drawCentered(g, "ARROWS/WASD MOVE  SPACE SLASH", VIEW_W / 2, 178, new Color(0xBBBBBB));
        drawCentered(g, "E SAVE   ESC MENU", VIEW_W / 2, 187, new Color(0xBBBBBB));
    }

    void drawWaterRipples(Graphics2D g, int camX, int camY) {
        for (int tx = camX / TILE; tx <= (camX + VIEW_W) / TILE; tx++) {
            for (int ty = camY / TILE; ty <= (camY + VIEW_H) / TILE; ty++) {
                if (tx < 0 || ty < 0 || tx >= MW || ty >= MH || map[tx][ty] != WATER) continue;
                int h = ((tx * 73856093) ^ (ty * 19349663)) & 0xFF;
                if (h % 3 != 0) continue;
                int rx = tx * TILE + (h + (int) (time * 6)) % 11;
                fill(g, WATER_LIGHT, rx - camX, ty * TILE + h % 12 - camY, 4, 1);
            }
        }
    }

    // ------------------------------------------------------------------ sprites
    static Color spx(String[] rows, int i, int j, int w, int nRows, boolean flip, boolean monster) {
        if (i < 0 || j < 0 || i >= w || j >= nRows) return null;
        char ch = rows[j].charAt(flip ? w - 1 - i : i);
        return monster ? mpal(ch) : pal(ch);
    }

    /** Draws a character-grid sprite with an automatic dark outline. */
    void drawSpr(Graphics2D g, String[] rows, int x, int y, boolean flip, int nRows, boolean monster, boolean flash) {
        int w = rows[0].length();
        g.setColor(OUTLINE);
        for (int j = -1; j <= nRows; j++) {
            for (int i = -1; i <= w; i++) {
                if (spx(rows, i, j, w, nRows, flip, monster) != null) continue;
                if (spx(rows, i - 1, j, w, nRows, flip, monster) != null || spx(rows, i + 1, j, w, nRows, flip, monster) != null
                        || spx(rows, i, j - 1, w, nRows, flip, monster) != null || spx(rows, i, j + 1, w, nRows, flip, monster) != null)
                    g.fillRect(x + i, y + j, 1, 1);
            }
        }
        for (int j = 0; j < nRows; j++) {
            for (int i = 0; i < w; i++) {
                Color c = spx(rows, i, j, w, nRows, flip, monster);
                if (c != null) {
                    g.setColor(flash ? Color.WHITE : c);
                    g.fillRect(x + i, y + j, 1, 1);
                }
            }
        }
    }

    void drawMono(Graphics2D g, String[] rows, int x, int y, Color c) {
        g.setColor(c);
        for (int j = 0; j < rows.length; j++)
            for (int i = 0; i < rows[j].length(); i++)
                if (rows[j].charAt(i) != '.') g.fillRect(x + i, y + j, 1, 1);
    }

    /** A short steel sword: blade, golden cross-guard and a leather grip, starting at (hx,hy). */
    void drawSword(Graphics2D g, int hx, int hy, double ang, int len) {
        double c = Math.cos(ang), s = Math.sin(ang);
        int ex = hx + (int) Math.round(c * len), ey = hy + (int) Math.round(s * len);
        g.setStroke(new BasicStroke(3f));
        g.setColor(OUTLINE);
        g.drawLine(hx, hy, ex, ey);
        g.setStroke(new BasicStroke(1f));
        g.setColor(STEEL);
        g.drawLine(hx, hy, ex, ey);
        int gx = hx + (int) Math.round(c * 2), gy = hy + (int) Math.round(s * 2);
        int px = (int) Math.round(-s * 2), py = (int) Math.round(c * 2);
        g.setColor(GOLD);
        g.drawLine(gx - px, gy - py, gx + px, gy + py);
        g.setColor(BROWN);
        g.drawLine(hx, hy, hx - (int) Math.round(c * 2), hy - (int) Math.round(s * 2));
    }

    void drawPlayer(Graphics2D g, int camX, int camY) {
        Player p = player;
        if (p.getInv() > 0 && ((int) (p.getInv() * 20)) % 2 == 0) return;           // hit flicker
        int px = (int) Math.round(p.getX()) - camX, py = (int) Math.round(p.getY()) - camY;
        boolean step = p.isMoving() && ((int) (p.getAnim() * 8)) % 2 == 1;
        String[] spr = p.getDir() == 1 ? SPR_UP : (p.getDir() == 0 ? SPR_DOWN : SPR_LEFT);
        drawSpr(g, spr, px - 6, py - 16 + (step ? -1 : 0), p.getDir() == 3, 16, false, false);

        // lantern in the off hand (flickering flame)
        int lx = px + (p.getDir() == 3 ? -7 : 7), ly = py - 8;
        fill(g, OUTLINE, lx, ly - 3, 1, 3);
        fill(g, new Color(0x4A78B0), lx - 1, ly, 3, 4);
        fill(g, (((int) (time * 10)) % 3 == 0) ? new Color(0xFFF2B0) : new Color(0xFFC040), lx, ly + 1, 1, 2);

        if (p.getAtk() > 0) {                                  // slash sweeps across the facing direction
            double t = 1 - p.getAtk() / ATTACK_TIME;
            double base = p.getDir() == 0 ? Math.PI / 2 : p.getDir() == 1 ? -Math.PI / 2 : p.getDir() == 2 ? Math.PI : 0;
            double ang = base + (t * 2 - 1) * 1.0;
            int ox = px, oy = py - 8;
            g.setColor(new Color(225, 238, 255, 170));
            for (int k = 1; k <= 6; k++) {                // fading slash trail
                double a = ang - k * 0.16;
                g.fillRect(ox + (int) Math.round(Math.cos(a) * (14 + k % 2)), oy + (int) Math.round(Math.sin(a) * (14 + k % 2)), 2, 1);
            }
            drawSword(g, ox, oy, ang, 15);
        } else {                                          // sword held low at the side
            if (p.getDir() == 3) drawSword(g, px + 6, py - 7, Math.PI / 2 - 0.35, 9);
            else drawSword(g, px - 6, py - 7, Math.PI / 2 + 0.35, 9);
        }
    }

    void drawMonster(Graphics2D g, Monster m, int camX, int camY) {
        double cyc = m.getPhase() - Math.floor(m.getPhase());
        int lift;
        if (m.getKind() == 3) lift = cyc < 0.4 ? (int) (Math.sin(cyc / 0.4 * Math.PI) * 5) : 0;
        else if (m.getKind() == 1 || m.getKind() == 4) lift = 3 + (int) Math.round(Math.sin(time * 3 + m.getPhase() * 2) * 2);
        else lift = ((int) (m.getPhase() * 4)) % 2 == 1 && (m.getDirx() != 0 || m.getDiry() != 0) ? 1 : 0;
        int bx = (int) Math.round(m.getX()) - 5 - camX, by = (int) Math.round(m.getY()) - 12 - lift - camY;
        Composite old = g.getComposite();
        if (m.getKind() == 1) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.82f));
        drawSpr(g, MON_SPR[m.getKind()], bx, by, m.isFlip(), 12, true, m.getHurt() > 0);
        g.setComposite(old);
    }

    /** The save point: a lantern shrine on a stone pedestal. */
    void drawShrine(Graphics2D g, int camX, int camY) {
        int x = (int) shrine.getX() - camX, y = (int) shrine.getY() - camY;
        fill(g, OUTLINE, x - 5, y - 4, 10, 5);
        fill(g, S_MID, x - 4, y - 3, 8, 3);
        fill(g, S_DARK, x - 4, y - 1, 8, 1);
        fill(g, OUTLINE, x - 3, y - 12, 6, 9);
        fill(g, S_MID, x - 2, y - 11, 4, 8);
        fill(g, OUTLINE, x - 5, y - 21, 10, 10);
        fill(g, new Color(0x4A78B0), x - 4, y - 20, 8, 8);
        boolean bright = ((int) (time * 8)) % 3 == 0;
        fill(g, bright ? new Color(0xFFF2B0) : new Color(0xFFD060), x - 3, y - 19, 6, 6);
        fill(g, new Color(0xFFF8D8), x - 1, y - 17, 2, 3);
        fill(g, new Color(0x2A4A78), x - 5, y - 21, 10, 2);
        if (nearShrine()) fill(g, Color.WHITE, x - 1, y - 27 + (int) (Math.sin(time * 6) * 1.5), 3, 1);
    }

    /**
     * POLYMORPHISM: Every object is stored as an Entity reference.
     * The overridden getEntityType() method is selected at runtime for
     * Player, Monster, Prop, and Shrine objects.
     */
    int countEntitiesOfType(String requestedType) {
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(player);
        entities.add(shrine);
        entities.addAll(monsters);
        entities.addAll(props);

        int count = 0;
        for (Entity entity : entities) {
            if (entity.getEntityType().equals(requestedType)) {
                count++;
            }
        }
        return count;
    }

    void drawHud(Graphics2D g) {
        for (int i = 0; i < 3; i++)
            drawMono(g, HEART, 5 + i * 10, 5, i < player.getHealth() ? HEART_FULL : HEART_EMPTY);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 8));
        drawText(g, "SCORE " + kills, VIEW_W - 62, 12, Color.WHITE);
        drawText(g, "BEST " + best, VIEW_W - 62, 22, new Color(0xFFD866));
        drawText(g, "FOES " + countEntitiesOfType("monster"), 5, 22, new Color(0xD9D5E8));
    }

    void drawText(Graphics2D g, String s, int x, int y, Color c) {
        g.setColor(new Color(0, 0, 0, 200));
        g.drawString(s, x + 1, y + 1);
        g.setColor(c);
        g.drawString(s, x, y);
    }

    void drawCentered(Graphics2D g, String s, int cx, int y, Color c) {
        FontMetrics fm = g.getFontMetrics();
        drawText(g, s, cx - fm.stringWidth(s) / 2, y, c);
    }

    // ---------------------------------------------------------------------- input
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k < keys.length) keys[k] = true;
        if (title) {                                   // start page controls
            if (k == KeyEvent.VK_ENTER) {
                if (player.getHealth() <= 0) respawn();
                title = false;
            } else if (k == KeyEvent.VK_N) {
                newGame();
            }
            return;
        }
        if (k == KeyEvent.VK_ESCAPE) {                 // back to the start page
            java.util.Arrays.fill(keys, false);
            title = true;
            return;
        }
        if (k == KeyEvent.VK_E && player.getHealth() > 0 && nearShrine()) save();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k < keys.length) keys[k] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    // ----------------------------------------------------------------------- main
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
