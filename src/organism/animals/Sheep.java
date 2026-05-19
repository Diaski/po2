package organisms.animals;

import organisms.Animal;
import world.World;

public class Sheep extends Animal {

    public static final char SYMBOL = 'S';

    public Sheep(int x, int y, World world) {
        super(x, y, 4, 4, world);
    }

    public Sheep(int x, int y, int strength, int initiative, World world) {
        super(x, y, strength, initiative, world);
    }

    @Override
    protected Sheep clone(int newX, int newY) {
        return new Sheep(newX, newY, world);
    }

    @Override public char   getSymbol() { return SYMBOL;  }
    @Override public String getName()   { return "Sheep"; }
}
