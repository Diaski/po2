package world;

import organisms.Organism;
import java.util.*;

/**
 * Square-grid (4-neighbour) implementation of {@link World}.
 * Each cell has up to 4 orthogonal neighbours; the organism's own
 * direction list is used exactly as supplied (supports arbitrary ranges,
 * e.g. the antelope's distance-2 directions).
 */
public class GridWorld extends World {

    public GridWorld(int width, int height) {
        super(width, height);
    }

    /**
     * Returns all cells reachable from (x, y) using the given direction offsets.
     * Only cells inside the board boundaries are included.
     * If {@code onlyFree} is true, occupied cells are excluded.
     */
    @Override
    public List<int[]> getNeighbours(int x, int y,
                                     List<int[]> directions,
                                     boolean onlyFree) {
        List<int[]> result = new ArrayList<>();
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && nx < getWidth() && ny >= 0 && ny < getHeight()) {
                if (!onlyFree || isPositionFree(nx, ny)) {
                    result.add(new int[]{nx, ny});
                }
            }
        }
        return result;
    }
}