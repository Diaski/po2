package organisms.animals;

import organisms.Animal;
import organisms.Organism;
import world.World;
import java.util.*;

public class Antylop extends Animal {

    public static final char SYMBOL = 'A';

    public Antylop(int x, int y, World world) {
        super(x, y, 4, 4, world);
    }

    /** Antelope can move up to 2 cells in any direction. */
    @Override
    protected List<int[]> getMovementDirections() {
        return Arrays.asList(
            new int[]{0,  -1}, new int[]{0,   1},
            new int[]{-1,  0}, new int[]{1,   0},
            new int[]{-1, -1}, new int[]{-1,  1},
            new int[]{1,  -1}, new int[]{1,   1},
            new int[]{-2,  0}, new int[]{2,   0},
            new int[]{0,  -2}, new int[]{0,   2},
            new int[]{-2, -2}, new int[]{-2,  2},
            new int[]{2,  -2}, new int[]{2,   2}
        );
    }

    @Override
    protected Antylop clone(int newX, int newY) {
        return new Antylop(newX, newY, world);
    }

    @Override public char   getSymbol() { return SYMBOL;     }
    @Override public String getName()   { return "Antelope"; }

    /**
     * Same species → breed.
     * 50 % chance to flee before fighting.
     * Fight: attacker (other) wins if strength >= this strength.
     */
    @Override
    public void collision(Organism other) {
        if (other == null || !this.getIsAlive() || !other.getIsAlive()) return;

        // Same species – breed
        if (this.getSymbol() == other.getSymbol()) {
            List<int[]> freeSpaces = world.getNeighbours(x, y, getMovementDirections(), true);
            if (!freeSpaces.isEmpty()) {
                int ri = (int)(Math.random() * freeSpaces.size());
                int nx = freeSpaces.get(ri)[0];
                int ny = freeSpaces.get(ri)[1];
                Animal child = this.clone(nx, ny);
                world.addOrganism(child);
                world.addLog(this.getName() + " spawned at (" + nx + "," + ny + ")");
            }
            return;
        }

        // 50 % chance to flee
        if (Math.random() < 0.5) {
            List<int[]> freeNeighbours = world.getNeighbours(x, y, getMovementDirections(), true);
            if (!freeNeighbours.isEmpty()) {
                int ri = (int)(Math.random() * freeNeighbours.size());
                int nx = freeNeighbours.get(ri)[0];
                int ny = freeNeighbours.get(ri)[1];
                if (world.isPositionFree(nx, ny)) {
                    world.moveOrganism(this, nx, ny);
                    return;
                }
            }
        }

        // Fight: attacker is `other`, defender is `this`
        if (other.getStrength() >= this.getStrength()) {
            int targetX = this.getX();
            int targetY = this.getY();
            world.addLog(other.getName() + " killed " + this.getName());
            world.removeOrganism(this);
            world.moveOrganism(other, targetX, targetY);
        } else {
            world.addLog(this.getName() + " repelled " + other.getName());
            world.removeOrganism(other);
        }
    }
}
