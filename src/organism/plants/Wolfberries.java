package organisms.plants;

import organisms.Plant;
import organisms.Organism;
import world.World;

public class Wolfberries extends Plant {

    public static final char SYMBOL = 'w';

    public Wolfberries(int x, int y, World world) {
        super(x, y, 99, world);
    }

    @Override
    protected Wolfberries clone(int newX, int newY) {
        return new Wolfberries(newX, newY, world);
    }

    @Override public char   getSymbol() { return SYMBOL;         }
    @Override public String getName()   { return "Wolfberries";  }

    @Override
    public void collision(Organism attacker) {
        if (attacker == null || !this.getIsAlive() || !attacker.getIsAlive()) return;
        world.addLog("Wolfberries killed " + attacker.getName());
        world.removeOrganism(this);
        world.removeOrganism(attacker);
    }
}
