package organisms.plants;

import organisms.Plant;
import world.World;

public class Dandelion extends Plant {

    public static final char SYMBOL = 'D';

    public Dandelion(int x, int y, World world) {
        super(x, y, 0, world);
    }

    @Override
    protected Dandelion clone(int newX, int newY) {
        return new Dandelion(newX, newY, world);
    }

    @Override public char   getSymbol() { return SYMBOL;     }
    @Override public String getName()   { return "Dandelion"; }

    /** Three spread attempts per turn. */
    @Override
    public void action() {
        super.action();
        super.action();
        super.action();
    }
}
