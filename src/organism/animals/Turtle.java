package organisms.animals;

import organisms.Animal;
import organisms.Organism;
import world.World;

public class Turtle extends Animal {

    public static final char SYMBOL = 'T';

    public Turtle(int x, int y, World world) {
        super(x, y, 2, 1, world);
    }

    @Override
    protected Turtle clone(int newX, int newY) {
        return new Turtle(newX, newY, world);
    }

    @Override public char   getSymbol() { return SYMBOL;   }
    @Override public String getName()   { return "Turtle"; }

    /** Turtle moves only 25 % of the time (rand() % 4 == 0 equivalent). */
    @Override
    public void action() {
        if ((int)(Math.random() * 4) != 0) return;
        super.action();
    }

    /** Repels attackers weaker than 5; stronger attackers kill turtle. */
    @Override
    public void collision(Organism other) {
        if (other == null || !this.getIsAlive() || !other.getIsAlive()) return;

        // Same species → breed (delegate to Animal.collision)
        if (this.getSymbol() == other.getSymbol()) {
            super.collision(other);
            return;
        }

        if (other.getStrength() < 5) {
            world.addLog(other.getName() + " was repelled by Turtle");
        } else {
            world.addLog(other.getName() + " killed Turtle");
            int tx = this.getX();
            int ty = this.getY();
            world.removeOrganism(this);
            world.moveOrganism(other, tx, ty);
        }
    }
}