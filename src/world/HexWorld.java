package world;

import java.util.*;

/**
 * Hexagonal grid world using "odd-r" offset coordinates (pointy-top hexagons).
 * Odd rows are shifted right by half a hex width.
 *
 * Neighbor tables for odd-r:
 * Even row (y%2==0): E(+1,0) W(-1,0) NE(0,-1) NW(-1,-1) SE(0,+1) SW(-1,+1)
 * Odd row (y%2==1): E(+1,0) W(-1,0) NE(+1,-1) NW(0,-1) SE(+1,+1) SW(0,+1)
 *
 * getNeighbours() ignores the direction list from the organism and returns
 * the appropriate hex neighbours based on the max manhattan distance of those
 * directions:
 * dist == 1 → the 6 immediate hex neighbours
 * dist >= 2 → all hex cells within hex-distance 2 (antelope range)
 */
public class HexWorld extends World {

    // Neighbour offsets for even rows (y % 2 == 0)
    private static final int[][] EVEN_DIRS = {
            { +1, 0 }, { -1, 0 },
            { 0, -1 }, { -1, -1 },
            { 0, +1 }, { -1, +1 }
    };

    // Neighbour offsets for odd rows (y % 2 == 1)
    private static final int[][] ODD_DIRS = {
            { +1, 0 }, { -1, 0 },
            { +1, -1 }, { 0, -1 },
            { +1, +1 }, { 0, +1 }
    };

    // ------------------------------------------------------------------ //

    public HexWorld(int width, int height) {
        super(width, height);
    }

    // ------------------------------------------------------------------ //
    // Core override
    // ------------------------------------------------------------------ //

    /**
     * Returns hex neighbours.
     * The `directions` list is used only to detect max movement range
     * (antelope passes dist-2 directions; everything else is dist-1).
     */
    @Override
    public List<int[]> getNeighbours(int x, int y, List<int[]> directions, boolean onlyFree) {
        int maxDist = 1;
        for (int[] dir : directions) {
            int d = Math.abs(dir[0]) + Math.abs(dir[1]);
            if (d > 1) {
                maxDist = 2;
                break;
            }
        }
        return (maxDist == 1)
                ? hexNeighbours1(x, y, onlyFree)
                : hexNeighboursUpTo2(x, y, onlyFree);
    }

    // ------------------------------------------------------------------ //
    // Hex neighbour helpers
    // ------------------------------------------------------------------ //

    /** The 6 immediate hex neighbours of (x, y). */
    private List<int[]> hexNeighbours1(int x, int y, boolean onlyFree) {
        int[][] dirs = (y % 2 == 0) ? EVEN_DIRS : ODD_DIRS;
        List<int[]> result = new ArrayList<>();
        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (inBounds(nx, ny) && (!onlyFree || isPositionFree(nx, ny))) {
                result.add(new int[] { nx, ny });
            }
        }
        return result;
    }

    /**
     * All hex cells within hex-distance 2 of (x, y), excluding (x, y) itself.
     * Built by BFS: start with dist-1 ring, expand each by 1 more step,
     * deduplicate with a String key set.
     */
    private List<int[]> hexNeighboursUpTo2(int x, int y, boolean onlyFree) {
        Set<String> seen = new HashSet<>();
        List<int[]> result = new ArrayList<>();
        seen.add(key(x, y));

        // Distance 1 ring
        int[][] dirs1 = (y % 2 == 0) ? EVEN_DIRS : ODD_DIRS;
        for (int[] d1 : dirs1) {
            int nx = x + d1[0];
            int ny = y + d1[1];
            if (!inBounds(nx, ny))
                continue;
            if (seen.add(key(nx, ny))) {
                if (!onlyFree || isPositionFree(nx, ny))
                    result.add(new int[] { nx, ny });

                // Distance 2 ring (neighbours of this distance-1 cell)
                int[][] dirs2 = (ny % 2 == 0) ? EVEN_DIRS : ODD_DIRS;
                for (int[] d2 : dirs2) {
                    int mx = nx + d2[0];
                    int my = ny + d2[1];
                    if (!inBounds(mx, my))
                        continue;
                    if (seen.add(key(mx, my))) {
                        if (!onlyFree || isPositionFree(mx, my))
                            result.add(new int[] { mx, my });
                    }
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------ //
    // Utilities
    // ------------------------------------------------------------------ //

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
    }

    private static String key(int x, int y) {
        return x + "," + y;
    }
}