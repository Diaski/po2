package organisms.animals;

import organisms.Animal;
import organisms.Organism;
import world.World;
import java.util.*;

public class Fox extends Animal {

    public static final char SYMBOL = 'F';

    public Fox(int x, int y, World world) {
        super(x, y, 3, 7, world);
    }

    @Override
    protected Fox clone(int newX, int newY) {
        return new Fox(newX, newY, world);
    }

    @Override public char   getSymbol() { return SYMBOL; }
    @Override public String getName()   { return "Fox";  }

    /** Fox never moves onto a cell occupied by a stronger organism. */
    @Override
    public void action() {
        List<int[]> allNeighbours  = world.getNeighbours(x, y, getMovementDirections(), false);
        List<int[]> safeNeighbours = new ArrayList<>();

        for (int[] pos : allNeighbours) {
            Organism occupant = world.getOrganismAt(pos[0], pos[1]);
            if (occupant == null || occupant.getStrength() <= this.getStrength()) {
                safeNeighbours.add(pos);
            }
        }

        if (safeNeighbours.isEmpty()) return;

        int randomIndex = (int)(Math.random() * safeNeighbours.size());
        int newX = safeNeighbours.get(randomIndex)[0];
        int newY = safeNeighbours.get(randomIndex)[1];

        if (world.isPositionFree(newX, newY)) {
            world.moveOrganism(this, newX, newY);
        } else {
            Organism defender = world.getOrganismAt(newX, newY);
            if (defender != null) defender.collision(this);
        }
    }
}
