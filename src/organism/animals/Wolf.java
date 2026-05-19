package organisms.animals;

import organisms.Animal;
import world.World;

public class Wolf extends Animal {

    public static final char SYMBOL = 'W';

    public Wolf(int x, int y, World world) {
        super(x, y, 9, 5, world);
    }

    @Override
    protected Wolf clone(int newX, int newY) {
        return new Wolf(newX, newY, world);
    }

    @Override public char   getSymbol() { return SYMBOL;  }
    @Override public String getName()   { return "Wolf";  }
}
