package organisms;

import world.World;
import java.util.*;

public abstract class Plant extends Organism {

    protected int spreadChance = 1;

    public Plant(int x, int y, int strength, World world) {
        super(x, y, strength, 0, world);
    }

    protected List<int[]> getSpreadDirections() {
        return Arrays.asList(
            new int[]{0, -1}, new int[]{0, 1},
            new int[]{-1, 0}, new int[]{1, 0}
        );
    }

    @Override
    protected abstract Plant clone(int newX, int newY);

    @Override
    public void action() {
        int spreadRoll = (int)(Math.random() * 100);
        if (spreadRoll < spreadChance) {
            List<int[]> freeSpaces = world.getNeighbours(x, y, getSpreadDirections(), true);
            if (!freeSpaces.isEmpty()) {
                int randomIndex = (int)(Math.random() * freeSpaces.size());
                int newX = freeSpaces.get(randomIndex)[0];
                int newY = freeSpaces.get(randomIndex)[1];
                Plant newPlant = this.clone(newX, newY);
                world.addOrganism(newPlant);
                world.addLog(this.getName() + " spread to (" + newX + "," + newY + ")");
            }
        }
    }

    @Override
    public void collision(Organism attacker) {
        if (attacker == null || !this.getIsAlive() || !attacker.getIsAlive()) return;
        world.addLog(attacker.getName() + " eat " + this.getName());
        int targetX = this.getX();
        int targetY = this.getY();
        world.removeOrganism(this);
        world.moveOrganism(attacker, targetX, targetY);
    }
}
