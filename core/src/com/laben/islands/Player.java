package com.laben.islands;

/** This class represents the player and all things to do with the player such as Stamina, equips, etc.
 */
public class Player {
    private static final int DEFAULT_MAX_STAMINA = 40;

    private int maxStamina;
    private int stamina;

    public Player() {
        maxStamina = DEFAULT_MAX_STAMINA;
        stamina = maxStamina;
    }

    public int getMaxStamina() {
        return maxStamina;
    }

    public void setMaxStamina(int maxStamina) {
        this.maxStamina = maxStamina;
    }

    public int getStamina() {
        return stamina;
    }

    public void setStamina(int stamina) {
        this.stamina = stamina;
    }
}
