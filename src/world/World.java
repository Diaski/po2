package world;

import organisms.Organism;
import java.util.*;

/**
 * Abstract base class for all world implementations.
 * Holds the common board, organism list and log management.
 * Subclasses (GridWorld, HexWorld) implement getNeighbours()
 * with their own topology.
 */
public abstract class World {

    public static final int WORLD_WIDTH = 20;
    public static final int WORLD_HEIGHT = 20;

    // ------------------------------------------------------------------ //
    // Fields
    // ------------------------------------------------------------------ //

    protected final int width;
    protected final int height;

    private final List<Organism> organisms;
    private final Organism[][] board;
    private final List<String> logs;
    private int logCounter;

    // ------------------------------------------------------------------ //
    // Constructor
    // ------------------------------------------------------------------ //

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.organisms = new ArrayList<>();
        this.board = new Organism[width][height];
        this.logs = new ArrayList<>();
        this.logCounter = 1;
    }

    // ------------------------------------------------------------------ //
    // Abstract method – topology-specific neighbour lookup
    // ------------------------------------------------------------------ //

    /**
     * Returns the list of neighbouring cells reachable from (x, y).
     * The {@code directions} parameter carries the organism's movement
     * or spread pattern; subclasses may use or ignore it as appropriate.
     *
     * @param x          origin column
     * @param y          origin row
     * @param directions movement/spread offsets defined by the organism
     * @param onlyFree   if true, return only unoccupied cells
     */
    public abstract List<int[]> getNeighbours(int x, int y,
            List<int[]> directions,
            boolean onlyFree);

    // ------------------------------------------------------------------ //
    // Organism management (common to all world types)
    // ------------------------------------------------------------------ //

    public void addOrganism(Organism org) {
        organisms.add(org);
        board[org.getX()][org.getY()] = org;
    }

    public void removeOrganism(Organism org) {
        board[org.getX()][org.getY()] = null;
        org.setDead();
    }

    public void moveOrganism(Organism org, int newX, int newY) {
        board[org.getX()][org.getY()] = null;
        org.setPosition(newX, newY);
        board[newX][newY] = org;
    }

    public Organism getOrganismAt(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height)
            return board[x][y];
        return null;
    }

    public boolean isPositionFree(int x, int y) {
        return getOrganismAt(x, y) == null;
    }

    // ------------------------------------------------------------------ //
    // Turn execution (same logic for every world type)
    // ------------------------------------------------------------------ //

    public void executeTurn() {
        clearLogs();

        organisms.sort((a, b) -> {
            if (a.getInitiative() == b.getInitiative())
                return Integer.compare(b.getAge(), a.getAge());
            return Integer.compare(b.getInitiative(), a.getInitiative());
        });

        int turnSize = organisms.size();
        for (int i = 0; i < turnSize; i++) {
            Organism org = organisms.get(i);
            if (org.getIsAlive())
                org.action();
            if (org.getIsAlive())
                org.incrementAge();
        }

        organisms.removeIf(org -> !org.getIsAlive());
    }

    // ------------------------------------------------------------------ //
    // Log management
    // ------------------------------------------------------------------ //

    public void addLog(String message) {
        if (logs.size() >= 5)
            logs.remove(0);
        logs.add("[" + logCounter + "] " + message);
        logCounter++;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void clearLogs() {
        logs.clear();
        logCounter = 1;
    }

    public int getLogCounter() {
        return logCounter;
    }

    // ------------------------------------------------------------------ //
    // Getters
    // ------------------------------------------------------------------ //

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Organism> getOrganisms() {
        return organisms;
    }
}