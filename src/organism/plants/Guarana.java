package organisms.plants;

import organisms.Plant;
import organisms.Organism;
import world.World;

public class Guarana extends Plant {

    public static final char SYMBOL = 'g';

    public Guarana(int x, int y, World world) {
        super(x, y, 0, world);
    }

    @Override
    protected Guarana clone(int newX, int newY) {
        return new Guarana(newX, newY, world);
    }

    @Override public char   getSymbol() { return SYMBOL;    }
    @Override public String getName()   { return "Guarana"; }

    @Override
    public void collision(Organism attacker) {
        if (attacker == null || !this.getIsAlive() || !attacker.getIsAlive()) return;
        attacker.changeStrength(3);
        world.addLog("Guarana boosted " + attacker.getName() + "'s strength");
        int targetX = this.getX();
        int targetY = this.getY();
        world.removeOrganism(this);
        world.moveOrganism(attacker, targetX, targetY);
    }
}
