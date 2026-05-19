package organisms.plants;

import organisms.Animal;
import organisms.Plant;
import organisms.Organism;
import organisms.animals.CyberSheep;
import world.World;
import java.util.List;

public class SosnowskisBorscht extends Plant {

    public static final char SYMBOL = 'b';

    public SosnowskisBorscht(int x, int y, World world) {
        super(x, y, 10, world);
        spreadChance = 15;
    }

    @Override
    protected SosnowskisBorscht clone(int newX, int newY) {
        return new SosnowskisBorscht(newX, newY, world);
    }

    @Override public char   getSymbol() { return SYMBOL;                    }
    @Override public String getName()   { return "Sosnowski's Hogweed";     }

    /** CyberSheep eats it; every other animal is killed. */
    @Override
    public void collision(Organism attacker) {
        if (attacker == null || !this.getIsAlive() || !attacker.getIsAlive()) return;

        if (attacker.getSymbol() == CyberSheep.SYMBOL) {
            int targetX = this.getX();
            int targetY = this.getY();
            world.addLog("Cyber Sheep ate Sosnowski's Hogweed");
            world.removeOrganism(this);
            world.moveOrganism(attacker, targetX, targetY);
        } else {
            world.addLog(attacker.getName() + " was killed by Sosnowski's Hogweed");
            world.removeOrganism(attacker);
            world.removeOrganism(this);
        }
    }

    /** Kills all adjacent animals (except CyberSheep), then attempts to spread. */
    @Override
    public void action() {
        List<int[]> neighbours = world.getNeighbours(x, y, getSpreadDirections(), false);

        for (int[] pos : neighbours) {
            Organism victim = world.getOrganismAt(pos[0], pos[1]);
            if (victim != null && victim.getIsAlive()) {
                if (victim instanceof Animal && victim.getSymbol() != CyberSheep.SYMBOL) {
                    world.addLog("Sosnowski's Hogweed killed " + victim.getName());
                    world.removeOrganism(victim);
                }
            }
        }
        super.action();
    }
}
