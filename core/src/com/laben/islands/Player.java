package com.laben.islands;

import com.laben.islands.items.Item;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** This class represents the player and all things to do with the player such as Stamina, equips, etc.
 */
public class Player {
    private static final int DEFAULT_MAX_STAMINA = 40;

    private int maxStamina;
    private int stamina;

    private Map<Item, Integer> inventory; //A mapping of every item in the game to the amount you have in your bag
    private List<Item> inventoryInBag; //Contains all items in bag in their currently sorted order

    public Player() {
        maxStamina = DEFAULT_MAX_STAMINA;
        stamina = maxStamina;
        //Initialize inventory
        inventoryInBag = new LinkedList<>();
        inventory = new HashMap<>();
        for (Item item : Item.masterItemSet)
            inventory.put(item, 0);
    }

    public List<Item> getInventoryInBag() {
        return inventoryInBag;
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

    public Map<Item, Integer> getInventory() {
        return inventory;
    }
}
