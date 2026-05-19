package organisms.animals;

import organisms.Animal;
import organisms.Organism;
import world.World;

public class Human extends Animal {

    public static final char SYMBOL = 'H';

    private static Human instance = null;

    private int     dx, dy;
    private boolean abilityActive;
    private int     abilityCooldown;
    private int     abilityDuration;
    private int     baseStrength;

    private Human(int x, int y, World world) {
        super(x, y, 5, 4, world);
        this.dx              = 0;
        this.dy              = 0;
        this.abilityActive   = false;
        this.abilityCooldown = 0;
        this.abilityDuration = 0;
        this.baseStrength    = 5;
    }

    // ------------------------------------------------------------------ //
    //  Singleton
    // ------------------------------------------------------------------ //

    public static Human getInstance(int x, int y, World world) {
        if (instance == null) instance = new Human(x, y, world);
        return instance;
    }

    /** Returns current instance without creating a new one. */
    public static Human getInstance() { return instance; }

    public static void resetInstance() { instance = null; }

    /** Clone is never used for Human (no reproduction). */
    @Override
    protected Human clone(int newX, int newY) { return instance; }

    // ------------------------------------------------------------------ //
    //  Identity
    // ------------------------------------------------------------------ //

    @Override public char   getSymbol() { return SYMBOL;  }
    @Override public String getName()   { return "Human"; }

    // ------------------------------------------------------------------ //
    //  Input
    // ------------------------------------------------------------------ //

    public void setDirection(int dx, int dy) { this.dx = dx; this.dy = dy; }

    // ------------------------------------------------------------------ //
    //  Magic Elixir ability
    // ------------------------------------------------------------------ //

    public void activateAbility() {
        if (abilityCooldown == 0 && !abilityActive) {
            abilityActive   = true;
            abilityDuration = 5;
            strength        = 10;
            world.addLog("Human activated Magic Elixir!");
        }
    }

    // ------------------------------------------------------------------ //
    //  Action  (1:1 with Human.cpp)
    // ------------------------------------------------------------------ //

    @Override
    public void action() {
        int newX = x + dx;
        int newY = y + dy;
        dx = 0;
        dy = 0;

        if (newX >= 0 && newX < world.getWidth() && newY >= 0 && newY < world.getHeight()) {
            if (world.isPositionFree(newX, newY)) {
                world.moveOrganism(this, newX, newY);
            } else {
                Organism defender = world.getOrganismAt(newX, newY);
                if (defender != this && defender.getIsAlive()) {
                    defender.collision(this);
                }
            }
        }

        if (abilityActive) {
            abilityDuration--;
            if (abilityDuration > 0) {
                strength--;
            } else {
                strength        = baseStrength;
                abilityActive   = false;
                abilityCooldown = 5;
                world.addLog("Magic Elixir ended. Strength back to normal.");
            }
        } else if (abilityCooldown > 0) {
            abilityCooldown--;
        }
    }

    // ------------------------------------------------------------------ //
    //  Collision
    // ------------------------------------------------------------------ //

    @Override
    public void collision(Organism other) {
        if (other == null || !this.getIsAlive() || !other.getIsAlive()) return;

        if (this.getStrength() >= other.getStrength()) {
            // Human is the DEFENDER – just kill the attacker, stay in place.
            // (Moving Human to the attacker's old position would be wrong.)
            world.addLog("Human killed " + other.getName());
            world.removeOrganism(other);
        } else {
            world.addLog("Human was killed by " + other.getName());
            Human.resetInstance();
            world.removeOrganism(this);
        }
    }

    // ------------------------------------------------------------------ //
    //  Save / Load getters & setters
    // ------------------------------------------------------------------ //

    public boolean isAbilityActive()              { return abilityActive;   }
    public int     getAbilityCooldown()           { return abilityCooldown; }
    public int     getAbilityDuration()           { return abilityDuration; }
    public int     getBaseStrength()              { return baseStrength;    }
    public void    setAbilityActive(boolean v)    { abilityActive   = v;   }
    public void    setAbilityCooldown(int v)      { abilityCooldown = v;   }
    public void    setAbilityDuration(int v)      { abilityDuration = v;   }
    public void    setBaseStrength(int v)         { baseStrength    = v;   }
}