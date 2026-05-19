package organisms;

import world.World;
import java.util.*;

public abstract class Animal extends Organism {

    public Animal(int x, int y, int strength, int initiative, World world) {
        super(x, y, strength, initiative, world);
    }

    protected List<int[]> getMovementDirections() {
        return Arrays.asList(
            new int[]{0, -1}, new int[]{0, 1},
            new int[]{-1, 0}, new int[]{1, 0}
        );
    }

    @Override
    protected abstract Animal clone(int newX, int newY);

    @Override
    public void action() {
        List<int[]> neighbours = world.getNeighbours(x, y, getMovementDirections(), false);
        if (neighbours.isEmpty()) return;

        int randomIndex = (int)(Math.random() * neighbours.size());
        int newX = neighbours.get(randomIndex)[0];
        int newY = neighbours.get(randomIndex)[1];

        if (world.isPositionFree(newX, newY)) {
            world.moveOrganism(this, newX, newY);
        } else {
            Organism defender = world.getOrganismAt(newX, newY);
            if (defender != this && defender.getIsAlive()) {
                defender.collision(this);
            }
        }
    }

    @Override
    public void collision(Organism attacker) {
        if (attacker == null || !this.getIsAlive() || !attacker.getIsAlive()) return;

        if (this.getSymbol() == attacker.getSymbol()) {
            List<int[]> freeSpaces = world.getNeighbours(x, y, getMovementDirections(), true);
            if (!freeSpaces.isEmpty()) {
                int randomIndex = (int)(Math.random() * freeSpaces.size());
                int newX = freeSpaces.get(randomIndex)[0];
                int newY = freeSpaces.get(randomIndex)[1];
                Animal child = this.clone(newX, newY);
                world.addOrganism(child);
                world.addLog(this.getName() + " spawned at (" + newX + "," + newY + ")");
            }
            return;
        }

        if (attacker.getStrength() >= this.getStrength()) {
            int targetX = this.getX();
            int targetY = this.getY();
            world.addLog(attacker.getName() + " killed " + this.getName());
            world.removeOrganism(this);
            world.moveOrganism(attacker, targetX, targetY);
        } else {
            world.addLog(this.getName() + " repelled " + attacker.getName());
            world.removeOrganism(attacker);
        }
    }
}
