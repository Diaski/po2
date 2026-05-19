package game;

import organisms.Organism;
import organisms.animals.*;
import organisms.plants.*;
import world.World;
import world.GridWorld;
import world.HexWorld;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.function.BiFunction;

public class Game extends JFrame {

    // ------------------------------------------------------------------ //
    //  Grid rendering constants
    // ------------------------------------------------------------------ //
    private static final int   CELL_SIZE = 22;
    private static final Font  GRID_FONT = new Font(Font.MONOSPACED, Font.BOLD, 16);

    // ------------------------------------------------------------------ //
    //  Hex rendering constants  (pointy-top, odd-r offset)
    // ------------------------------------------------------------------ //
    private static final double HEX_R         = 13.0;
    private static final double HEX_COL_SPACE = Math.sqrt(3) * HEX_R;   // ≈ 22.5
    private static final double HEX_ROW_SPACE = 1.5 * HEX_R;            // = 19.5
    private static final Font   HEX_FONT      = new Font(Font.MONOSPACED, Font.BOLD, 11);

    // ------------------------------------------------------------------ //
    //  Common constants
    // ------------------------------------------------------------------ //
    private static final Color  BG_COLOR  = new Color(18, 18, 18);
    private static final String SAVE_FILE = "save.txt";

    private static final String[] ORGANISM_NAMES = {
            "Wolf","Sheep","Fox","Antelope","Turtle","CyberSheep",
            "Grass","Dandelion","Guarana","Wolfberries","Sosnowski's Hogweed"
    };

    // ------------------------------------------------------------------ //
    //  State
    // ------------------------------------------------------------------ //
    private World world;       // abstract – either GridWorld or HexWorld
    private int   turnCount;

    // ------------------------------------------------------------------ //
    //  UI components
    // ------------------------------------------------------------------ //
    private WorldPanel worldPanel;
    private JTextArea  logArea;
    private JLabel     statusLabel;
    private JLabel     abilityLabel;
    private JPanel     centrePanel;

    // ======================================================================
    //  Constructor
    // ======================================================================

    public Game(int width, int height, boolean useHex) {
        // Polymorphic world creation: GridWorld or HexWorld
        world     = useHex ? new HexWorld(width, height)
                : new GridWorld(width, height);
        turnCount = 0;
        initOrganisms();
        buildUI();
        bindKeys();
        setTitle("Virtual World - Damian Stawski 208186");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    // ======================================================================
    //  Organism spawning  (1:1 with Game.cpp)
    // ======================================================================

    private void initOrganisms() {
        int cx = world.getWidth()  / 2;
        int cy = world.getHeight() / 2;
        world.addOrganism(Human.getInstance(cx, cy, world));
        spawnN(3, (x,y) -> new Wolf(x, y, world));
        spawnN(4, (x,y) -> new Sheep(x, y, world));
        spawnN(3, (x,y) -> new Fox(x, y, world));
        spawnN(3, (x,y) -> new Antylop(x, y, world));
        spawnN(3, (x,y) -> new Turtle(x, y, world));
        spawnN(2, (x,y) -> new CyberSheep(x, y, world));
        spawnN(8, (x,y) -> new Grass(x, y, world));
        spawnN(4, (x,y) -> new Dandelion(x, y, world));
        spawnN(3, (x,y) -> new Guarana(x, y, world));
        spawnN(3, (x,y) -> new Wolfberries(x, y, world));
        spawnN(3, (x,y) -> new SosnowskisBorscht(x, y, world));
    }

    private void spawnN(int count, BiFunction<Integer,Integer,Organism> factory) {
        int placed=0, attempts=0;
        while (placed<count && attempts<10000) {
            attempts++;
            int rx = (int)(Math.random() * world.getWidth());
            int ry = (int)(Math.random() * world.getHeight());
            if (world.isPositionFree(rx,ry)) { world.addOrganism(factory.apply(rx,ry)); placed++; }
        }
    }

    // ======================================================================
    //  UI construction
    // ======================================================================

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8,8));
        root.setBackground(BG_COLOR);
        root.setBorder(new EmptyBorder(8,8,8,8));
        setContentPane(root);
        setJMenuBar(buildMenuBar());

        statusLabel = new JLabel();
        statusLabel.setForeground(Color.CYAN);
        statusLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        updateStatusLabel();
        root.add(statusLabel, BorderLayout.NORTH);

        centrePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        centrePanel.setBackground(BG_COLOR);
        worldPanel = new WorldPanel();
        centrePanel.add(worldPanel);
        centrePanel.add(buildLegendPanel());
        root.add(centrePanel, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(4,4));
        south.setBackground(BG_COLOR);
        logArea = new JTextArea(6,60);
        logArea.setEditable(false);
        logArea.setBackground(new Color(30,30,30));
        logArea.setForeground(Color.LIGHT_GRAY);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        south.add(new JScrollPane(logArea), BorderLayout.CENTER);
        south.add(buildButtonPanel(), BorderLayout.SOUTH);
        root.add(south, BorderLayout.SOUTH);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar  = new JMenuBar();
        JMenu    file = new JMenu("File");
        JMenu    game = new JMenu("Game");
        addMI(file, "Save  [S]",             e -> saveWorld());
        addMI(file, "Load  [L]",             e -> loadWorld());
        file.addSeparator();
        addMI(file, "Quit  [Q]",             e -> System.exit(0));
        addMI(game, "Next Turn  [N]",        e -> nextTurn());
        addMI(game, "Activate Ability  [E]", e -> activateAbility());
        bar.add(file); bar.add(game);
        return bar;
    }

    private void addMI(JMenu m, String t, ActionListener al) {
        JMenuItem i = new JMenuItem(t); i.addActionListener(al); m.add(i);
    }

    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        p.setBackground(BG_COLOR);
        p.add(mkBtn("Next Turn [N]", e -> nextTurn()));
        p.add(mkBtn("Ability   [E]", e -> activateAbility()));
        p.add(mkBtn("Save      [S]", e -> saveWorld()));
        p.add(mkBtn("Load      [L]", e -> loadWorld()));
        p.add(mkBtn("Quit      [Q]", e -> System.exit(0)));
        abilityLabel = new JLabel("  Elixir: READY");
        abilityLabel.setForeground(Color.GREEN);
        abilityLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        p.add(abilityLabel);
        return p;
    }

    private JButton mkBtn(String t, ActionListener al) {
        JButton b = new JButton(t);
        b.setBackground(new Color(50,50,50)); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        b.addActionListener(al); return b;
    }

    private JPanel buildLegendPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_COLOR);
        p.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        JLabel title = new JLabel("  === LEGEND ===");
        title.setForeground(Color.YELLOW);
        title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        p.add(title);

        Object[][] entries = {
                {Wolf.SYMBOL,"Wolf"}, {Sheep.SYMBOL,"Sheep"}, {Fox.SYMBOL,"Fox"},
                {Antylop.SYMBOL,"Antelope"}, {Turtle.SYMBOL,"Turtle"},
                {CyberSheep.SYMBOL,"CyberSheep"}, {Human.SYMBOL,"Human"},
                {Grass.SYMBOL,"Grass"}, {SosnowskisBorscht.SYMBOL,"Hogweed"},
                {Wolfberries.SYMBOL,"Wolfberries"}, {Guarana.SYMBOL,"Guarana"},
                {Dandelion.SYMBOL,"Dandelion"},
        };
        for (Object[] e : entries) {
            char sym = (Character)e[0];
            JLabel l = new JLabel("  " + sym + " - " + e[1]);
            l.setForeground(symColor(sym));
            l.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            p.add(l);
        }
        p.add(Box.createVerticalStrut(8));
        for (String h : new String[]{"  Click empty cell","  to add organism"}) {
            JLabel hl = new JLabel(h);
            hl.setForeground(Color.GRAY);
            hl.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 11));
            p.add(hl);
        }
        return p;
    }

    // ======================================================================
    //  Key bindings
    // ======================================================================

    private void bindKeys() {
        InputMap  im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();
        addKey(im,am,KeyEvent.VK_UP,    "up",      () -> moveHuman(0,-1));
        addKey(im,am,KeyEvent.VK_DOWN,  "down",    () -> moveHuman(0, 1));
        addKey(im,am,KeyEvent.VK_LEFT,  "left",    () -> moveHuman(-1,0));
        addKey(im,am,KeyEvent.VK_RIGHT, "right",   () -> moveHuman( 1,0));
        addKey(im,am,KeyEvent.VK_N,     "next",    this::nextTurn);
        addKey(im,am,KeyEvent.VK_Q,     "quit",    () -> System.exit(0));
        addKey(im,am,KeyEvent.VK_S,     "save",    this::saveWorld);
        addKey(im,am,KeyEvent.VK_L,     "load",    this::loadWorld);
        addKey(im,am,KeyEvent.VK_E,     "ability", this::activateAbility);
    }

    private void addKey(InputMap im, ActionMap am, int vk, String name, Runnable r) {
        im.put(KeyStroke.getKeyStroke(vk, 0), name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { r.run(); }
        });
    }

    // ======================================================================
    //  Game logic
    // ======================================================================

    private void moveHuman(int dx, int dy) {
        Human p = Human.getInstance();
        if (p != null) { p.setDirection(dx,dy); nextTurn(); }
    }

    private void activateAbility() {
        Human p = Human.getInstance();
        if (p != null) p.activateAbility();
        refresh();
    }

    private void nextTurn() { world.executeTurn(); turnCount++; refresh(); }

    // ======================================================================
    //  Click-to-add organism  (Java 4-point requirement)
    // ======================================================================

    private void handleCellClick(int cx, int cy) {
        if (cx < 0 || cx >= world.getWidth() || cy < 0 || cy >= world.getHeight()) return;
        if (!world.isPositionFree(cx, cy)) {
            logArea.setText("Cell (" + cx + "," + cy + ") is occupied.");
            return;
        }
        String choice = (String) JOptionPane.showInputDialog(
                this, "Place organism at (" + cx + ", " + cy + "):",
                "Add Organism", JOptionPane.PLAIN_MESSAGE, null,
                ORGANISM_NAMES, ORGANISM_NAMES[0]);
        if (choice == null) return;
        Organism org = mkOrgByName(choice, cx, cy);
        if (org != null) {
            world.addOrganism(org);
            world.addLog("Player added " + org.getName() + " at (" + cx + "," + cy + ")");
            refresh();
        }
    }

    private Organism mkOrgByName(String name, int x, int y) {
        switch (name) {
            case "Wolf":                  return new Wolf(x,y,world);
            case "Sheep":                 return new Sheep(x,y,world);
            case "Fox":                   return new Fox(x,y,world);
            case "Antelope":              return new Antylop(x,y,world);
            case "Turtle":                return new Turtle(x,y,world);
            case "CyberSheep":            return new CyberSheep(x,y,world);
            case "Grass":                 return new Grass(x,y,world);
            case "Dandelion":             return new Dandelion(x,y,world);
            case "Guarana":               return new Guarana(x,y,world);
            case "Wolfberries":           return new Wolfberries(x,y,world);
            case "Sosnowski's Hogweed":   return new SosnowskisBorscht(x,y,world);
            default:                      return null;
        }
    }

    // ======================================================================
    //  UI refresh
    // ======================================================================

    private void refresh() {
        updateStatusLabel(); updateAbilityLabel(); updateLog(); worldPanel.repaint();
    }

    private void updateStatusLabel() {
        String type = (world instanceof HexWorld) ? "HEX" : "GRID";
        statusLabel.setText(
                "  Author: Damian Stawski 208186  |  VIRTUAL WORLD [" + type + "]"
                        + "  |  Turn: " + turnCount
                        + "  |  Map: " + world.getWidth() + "x" + world.getHeight());
    }

    private void updateAbilityLabel() {
        if (abilityLabel == null) return;
        Human p = Human.getInstance();
        if (p == null) {
            abilityLabel.setText("  Human: DEAD"); abilityLabel.setForeground(Color.RED);
        } else if (p.isAbilityActive()) {
            abilityLabel.setText("  Elixir: ACTIVE  str=" + p.getStrength()
                    + "  " + p.getAbilityDuration() + "t");
            abilityLabel.setForeground(Color.ORANGE);
        } else if (p.getAbilityCooldown() > 0) {
            abilityLabel.setText("  Elixir: cooldown " + p.getAbilityCooldown());
            abilityLabel.setForeground(Color.YELLOW);
        } else {
            abilityLabel.setText("  Elixir: READY"); abilityLabel.setForeground(Color.GREEN);
        }
    }

    private void updateLog() {
        List<String> logs = world.getLogs();
        StringBuilder sb = new StringBuilder("--- EVENT LOG ---\n");
        if (logs.isEmpty()) sb.append("Nothing happened this turn.");
        else for (String l : logs) sb.append(l).append("\n");
        logArea.setText(sb.toString());
    }

    // ======================================================================
    //  Rebuild WorldPanel after map-size or type change on load
    // ======================================================================

    private void rebuildWorldPanel() {
        centrePanel.remove(worldPanel);
        worldPanel = new WorldPanel();
        centrePanel.add(worldPanel, 0);
        centrePanel.revalidate();
        pack();
        setLocationRelativeTo(null);
    }

    // ======================================================================
    //  Save / Load
    //  Header: "width height G" (GridWorld) or "width height H" (HexWorld)
    //  Backward-compatible: missing type token defaults to G.
    // ======================================================================

    private void saveWorld() {
        String typeChar = (world instanceof HexWorld) ? "H" : "G";
        try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
            pw.printf("%d %d %s%n", world.getWidth(), world.getHeight(), typeChar);
            pw.printf("%d%n", turnCount);
            for (int y = 0; y < world.getHeight(); y++) {
                for (int x = 0; x < world.getWidth(); x++) {
                    Organism org = world.getOrganismAt(x, y);
                    if (org == null) continue;
                    if (org.getSymbol() == Human.SYMBOL) {
                        Human h = (Human) org;
                        pw.printf("%c %d %d %d %d %d %d %d %d%n",
                                org.getSymbol(), x, y,
                                org.getAge(), org.getStrength(),
                                h.isAbilityActive()?1:0,
                                h.getAbilityCooldown(), h.getAbilityDuration(), h.getBaseStrength());
                    } else {
                        pw.printf("%c %d %d %d %d%n",
                                org.getSymbol(), x, y, org.getAge(), org.getStrength());
                    }
                }
            }
            logArea.setText("Saved -> " + SAVE_FILE + "  [" + typeChar + "]  "
                    + world.getWidth() + "x" + world.getHeight() + ", turn " + turnCount);
        } catch (IOException ex) { logArea.setText("Cannot save: " + ex.getMessage()); }
    }

    private void loadWorld() {
        try (BufferedReader br = new BufferedReader(new FileReader(SAVE_FILE))) {
            String[] dim = br.readLine().trim().split("\\s+");
            int loadW    = Integer.parseInt(dim[0]);
            int loadH    = Integer.parseInt(dim[1]);
            boolean hex  = dim.length >= 3 && dim[2].equalsIgnoreCase("H");
            int loadTurn = Integer.parseInt(br.readLine().trim());

            if (loadW < 1 || loadW > 500 || loadH < 1 || loadH > 500) {
                logArea.setText("Invalid dimensions: " + loadW + "x" + loadH); return;
            }

            boolean needRebuild = loadW  != world.getWidth()
                    || loadH  != world.getHeight()
                    || hex    != (world instanceof HexWorld);

            Human.resetInstance();
            // Polymorphic world creation on load
            world     = hex ? new HexWorld(loadW, loadH) : new GridWorld(loadW, loadH);
            turnCount = loadTurn;

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\s+");
                if (p.length < 3) continue;

                char sym = p[0].charAt(0);
                int  lx  = Integer.parseInt(p[1]);
                int  ly  = Integer.parseInt(p[2]);
                int  age = p.length > 3 ? Integer.parseInt(p[3]) : 0;
                int  str = p.length > 4 ? Integer.parseInt(p[4]) : -1;

                if (lx < 0 || lx >= loadW || ly < 0 || ly >= loadH) continue;
                if (!world.isPositionFree(lx, ly)) continue;

                Organism org = mkOrgBySym(sym, lx, ly);
                if (org == null) continue;
                org.setAge(age);
                if (str >= 0) org.changeStrength(str - org.getStrength());

                if (sym == Human.SYMBOL && p.length >= 9) {
                    Human hu = (Human) org;
                    hu.setAbilityActive  (Integer.parseInt(p[5]) != 0);
                    hu.setAbilityCooldown(Integer.parseInt(p[6]));
                    hu.setAbilityDuration(Integer.parseInt(p[7]));
                    hu.setBaseStrength   (Integer.parseInt(p[8]));
                }
                world.addOrganism(org);
            }

            if (needRebuild) rebuildWorldPanel();
            refresh();
            logArea.setText("Loaded <- " + SAVE_FILE + "  ["
                    + (hex?"HEX":"GRID") + "]  " + loadW + "x" + loadH + ", turn " + loadTurn);

        } catch (FileNotFoundException ex) {
            logArea.setText("Save file not found: " + SAVE_FILE);
        } catch (IOException | NumberFormatException ex) {
            logArea.setText("Cannot load: " + ex.getMessage());
        }
    }

    private Organism mkOrgBySym(char sym, int x, int y) {
        switch (sym) {
            case Wolf.SYMBOL:              return new Wolf(x,y,world);
            case Sheep.SYMBOL:             return new Sheep(x,y,world);
            case Fox.SYMBOL:               return new Fox(x,y,world);
            case Antylop.SYMBOL:           return new Antylop(x,y,world);
            case Turtle.SYMBOL:            return new Turtle(x,y,world);
            case CyberSheep.SYMBOL:        return new CyberSheep(x,y,world);
            case Human.SYMBOL:             return Human.getInstance(x,y,world);
            case Grass.SYMBOL:             return new Grass(x,y,world);
            case SosnowskisBorscht.SYMBOL: return new SosnowskisBorscht(x,y,world);
            case Wolfberries.SYMBOL:       return new Wolfberries(x,y,world);
            case Guarana.SYMBOL:           return new Guarana(x,y,world);
            case Dandelion.SYMBOL:         return new Dandelion(x,y,world);
            default:                       return null;
        }
    }

    // ======================================================================
    //  Colour mapping
    // ======================================================================

    static Color symColor(char s) {
        switch (s) {
            case Wolf.SYMBOL:              return new Color(220, 60,  60);
            case Sheep.SYMBOL:             return new Color(220,220, 220);
            case Fox.SYMBOL:               return new Color(255,140,   0);
            case Turtle.SYMBOL:            return new Color( 50,200,  50);
            case Antylop.SYMBOL:           return new Color(240,200,  80);
            case CyberSheep.SYMBOL:        return new Color( 80,200, 255);
            case Human.SYMBOL:             return new Color(100,100, 255);
            case Grass.SYMBOL:             return new Color( 30,160,  30);
            case SosnowskisBorscht.SYMBOL: return new Color(180, 30,  30);
            case Wolfberries.SYMBOL:       return new Color(180,  0, 180);
            case Guarana.SYMBOL:           return new Color(  0,255, 120);
            case Dandelion.SYMBOL:         return new Color(255,255,  80);
            default:                       return new Color( 80, 80,  80);
        }
    }

    static Color orgColor(Organism o) {
        return o == null ? new Color(45,45,45) : symColor(o.getSymbol());
    }

    // ======================================================================
    //  Hex geometry helpers
    // ======================================================================

    private static double hexCX(int col, int row) {
        double off = (row % 2 == 1) ? HEX_COL_SPACE / 2.0 : 0;
        return col * HEX_COL_SPACE + HEX_COL_SPACE / 2.0 + off + 4;
    }

    private static double hexCY(int row) {
        return row * HEX_ROW_SPACE + HEX_R + 4;
    }

    private static int[][] hexVertices(double cx, double cy) {
        int[][] v = new int[2][6];
        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(-90 + 60 * i);
            v[0][i] = (int) Math.round(cx + HEX_R * Math.cos(a));
            v[1][i] = (int) Math.round(cy + HEX_R * Math.sin(a));
        }
        return v;
    }

    private static int[] pixelToHex(int px, int py) {
        int row = Math.max(0, (int)((py - 4) / HEX_ROW_SPACE));
        double off = (row % 2 == 1) ? HEX_COL_SPACE / 2.0 : 0;
        int col = Math.max(0, (int)((px - 4 - off) / HEX_COL_SPACE));

        // Refine: find closest centre in 3x3 neighbourhood
        int bestCol = col, bestRow = row;
        double bestDist = Double.MAX_VALUE;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int r = row+dr, c = col+dc;
                if (r < 0 || c < 0) continue;
                double dx = px - hexCX(c,r), dy = py - hexCY(r);
                double d  = dx*dx + dy*dy;
                if (d < bestDist) { bestDist=d; bestCol=c; bestRow=r; }
            }
        }
        return new int[]{bestCol, bestRow};
    }

    private Dimension hexPanelSize() {
        int w = (int)(world.getWidth()  * HEX_COL_SPACE + HEX_COL_SPACE/2 + 12);
        int h = (int)(world.getHeight() * HEX_ROW_SPACE + HEX_R + 12);
        return new Dimension(w, h);
    }

    // ======================================================================
    //  WorldPanel  –  renders grid OR hex, handles mouse click
    // ======================================================================

    private class WorldPanel extends JPanel {

        WorldPanel() {
            setBackground(BG_COLOR);
            setPreferredSize((world instanceof HexWorld)
                    ? hexPanelSize()
                    : new Dimension(world.getWidth()*CELL_SIZE, world.getHeight()*CELL_SIZE));
            setToolTipText("Click empty cell to add organism");

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int cx, cy;
                    if (world instanceof HexWorld) {
                        int[] cell = pixelToHex(e.getX(), e.getY());
                        cx = cell[0]; cy = cell[1];
                    } else {
                        cx = e.getX() / CELL_SIZE;
                        cy = e.getY() / CELL_SIZE;
                    }
                    handleCellClick(cx, cy);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (world instanceof HexWorld) paintHex(g2);
            else                           paintGrid(g2);
        }

        private void paintGrid(Graphics2D g) {
            g.setFont(GRID_FONT);
            FontMetrics fm = g.getFontMetrics();
            for (int row = 0; row < world.getHeight(); row++) {
                for (int col = 0; col < world.getWidth(); col++) {
                    Organism org = world.getOrganismAt(col, row);
                    g.setColor(new Color(35,35,35));
                    g.fillRect(col*CELL_SIZE, row*CELL_SIZE, CELL_SIZE-1, CELL_SIZE-1);
                    char ch = (org!=null) ? org.getSymbol() : '.';
                    g.setColor(orgColor(org));
                    int tx = col*CELL_SIZE + (CELL_SIZE - fm.charWidth(ch))/2;
                    int ty = row*CELL_SIZE + (CELL_SIZE + fm.getAscent())/2 - 2;
                    g.drawString(String.valueOf(ch), tx, ty);
                }
            }
        }

        private void paintHex(Graphics2D g) {
            g.setFont(HEX_FONT);
            FontMetrics fm = g.getFontMetrics();
            for (int row = 0; row < world.getHeight(); row++) {
                for (int col = 0; col < world.getWidth(); col++) {
                    Organism org = world.getOrganismAt(col, row);
                    double cx = hexCX(col, row);
                    double cy = hexCY(row);
                    int[][] v = hexVertices(cx, cy);

                    g.setColor(new Color(35,35,35));
                    g.fillPolygon(v[0], v[1], 6);
                    g.setColor(new Color(60,60,60));
                    g.drawPolygon(v[0], v[1], 6);

                    char ch = (org!=null) ? org.getSymbol() : '.';
                    g.setColor(orgColor(org));
                    int tx = (int)cx - fm.charWidth(ch)/2;
                    int ty = (int)cy + fm.getAscent()/2 - 2;
                    g.drawString(String.valueOf(ch), tx, ty);
                }
            }
        }
    }
}