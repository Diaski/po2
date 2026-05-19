package organisms;

import world.World;

public abstract class Organism {
    protected int x, y;
    protected int strength;
    protected int initiative;
    protected World world;
    protected int age;
    protected boolean isAlive = true;

    public Organism(int x, int y, int strength, int initiative, World world) {
        this.x = x;
        this.y = y;
        this.strength = strength;
        this.initiative = initiative;
        this.world = world;
        this.age = 0;
    }

    public abstract void action();
    public abstract void collision(Organism other);
    public abstract char getSymbol();
    public abstract String getName();
    protected abstract Organism clone(int newX, int newY);

    public void setPosition(int newX, int newY) { x = newX; y = newY; }
    public void incrementAge()                  { age++; }
    public void setAge(int newAge)              { age = newAge; }
    public int  getAge()                        { return age; }
    public int  getStrength()                   { return strength; }
    public int  getInitiative()                 { return initiative; }
    public int  getX()                          { return x; }
    public int  getY()                          { return y; }
    public int  changeStrength(int delta)       { strength += delta; return strength; }
    public boolean getIsAlive()                 { return isAlive; }
    public void setDead()                       { isAlive = false; }
    public World getWorld()                     { return world; }
    public void setWorld(World w)               { world = w; }
}
