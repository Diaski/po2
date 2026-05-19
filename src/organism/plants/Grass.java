package organisms.plants;

import organisms.Plant;
import world.World;

public class Grass extends Plant {

    public static final char SYMBOL = 'G';

    public Grass(int x, int y, World world) {
        super(x, y, 0, world);
        spreadChance = 1;
    }

    @Override
    protected Grass clone(int newX, int newY) {
        return new Grass(newX, newY, world);
    }

    @Override public char   getSymbol() { return SYMBOL; }
    @Override public String getName()   { return "Grass"; }
}
