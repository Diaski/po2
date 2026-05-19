package organisms.animals;

import organisms.Organism;
import organisms.plants.SosnowskisBorscht;
import world.World;

public class CyberSheep extends Sheep {

    public static final char SYMBOL = 'C';

    public CyberSheep(int x, int y, World world) {
        super(x, y, 11, 4, world);
    }

    @Override
    protected CyberSheep clone(int newX, int newY) {
        return new CyberSheep(newX, newY, world);
    }

    @Override public char   getSymbol() { return SYMBOL;        }
    @Override public String getName()   { return "Cyber Sheep"; }

    /**
     * Seeks the nearest Sosnowski's Hogweed (Manhattan distance).
     * If none exists, behaves like a normal sheep (Animal.action).
     */
    @Override
    public void action() {
        Organism closestBorscht = null;
        int      minDistance    = Integer.MAX_VALUE;

        for (int cy = 0; cy < world.getHeight(); cy++) {
            for (int cx = 0; cx < world.getWidth(); cx++) {
                Organism org = world.getOrganismAt(cx, cy);
                if (org != null && org.getSymbol() == SosnowskisBorscht.SYMBOL) {
                    int distance = Math.abs(x - cx) + Math.abs(y - cy);
                    if (distance < minDistance) {
                        minDistance    = distance;
                        closestBorscht = org;
                    }
                }
            }
        }

        if (closestBorscht != null) {
            int newX = x;
            int newY = y;

            if      (x < closestBorscht.getX()) newX++;
            else if (x > closestBorscht.getX()) newX--;
            else if (y < closestBorscht.getY()) newY++;
            else if (y > closestBorscht.getY()) newY--;

            if (world.isPositionFree(newX, newY)) {
                world.moveOrganism(this, newX, newY);
            } else {
                Organism defender = world.getOrganismAt(newX, newY);
                if (defender != null) defender.collision(this);
            }
        } else {
            super.action(); // behaves like Animal.action()
        }
    }
}
