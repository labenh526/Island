package com.laben.islands;

import com.laben.islands.items.Item;

import java.util.*;

/** This class represents the player and all things to do with the player such as Stamina, equips, etc.
 */
public class Player {
    private static final int DEFAULT_MAX_STAMINA = 40;

    private int maxStamina;
    private int stamina;

    private Map<Item, Integer> inventory; //A mapping of every item in the game to the amount you have in your bag
    private LinkedHashSet<Item> inventoryInBag; //Contains all items in bag in their currently sorted order

    public Player() {
        maxStamina = DEFAULT_MAX_STAMINA;
        stamina = maxStamina;
        //Initialize inventory
        inventoryInBag = new LinkedHashSet<>();
        inventory = new HashMap<>();
        for (Item item : Item.masterItemSet)
            inventory.put(item, 0);
    }

    public LinkedHashSet<Item> getInventoryInBag() {
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

    //Adds 1 of the specified item to the player's inventory
    public void addItemToInventory(Item item) {
        addItemToInventory(item, 1);
    }

    //Adds the given number of the specified item to the player's inventory
    public void addItemToInventory(Item item, int quantity) {
        inventoryInBag.add(item);
        inventory.put(item, inventory.get(item) + quantity);
    }

    //Sorts the inventory in the player's bag
    public void sortInventory() {
        List<Item> inventoryList = new LinkedList<>(inventoryInBag);
        Collections.sort(inventoryList);
        inventoryInBag = new LinkedHashSet<>(inventoryList);
    }

    public void removeItemFromInventory(Item item) {
        removeItemFromInventory(item, 1);
    }

    public void removeItemFromInventory(Item item, int quantity) {
        inventory.put(item, inventory.get(item) - quantity);
        if (inventory.get(item) == 0)
            inventoryInBag.remove(item);
        else if (inventory.get(item) < 0)
            throw new IllegalStateException("Removed more items than possible");
    }


}
