package com.laben.islands.items;

import com.laben.islands.IslandGame;

import java.util.*;

/**All items extend from this class**/
public class Item implements Comparable<Item>{

    public static final List<Item> masterItemSet; //Set of all items in the game

    private enum ItemType {Heal}

    private static final Map<ItemType, Integer> itemSortOrder;

    static {
        itemSortOrder = new HashMap<>();
        itemSortOrder.put(ItemType.Heal, 0);

        masterItemSet = new ArrayList<>(50);
        masterItemSet.add(new Item("Bread Crumbs", false, 1, new HealStamina(1), ItemType.Heal));
        masterItemSet.add(new Item("Bread", false, 5, new HealStamina(5), ItemType.Heal));
        masterItemSet.add(new Item("Magic Bread", false, 35, new HealStamina(20), ItemType.Heal));

    }


    private final String name;
    private final boolean isEquippable;
    private final ItemScript script;
    private final int value;
    private ItemType type;

    public Item(String name, boolean isEquippable, int value, ItemScript script, ItemType type) {
        this.name = name;
        this.isEquippable = isEquippable;
        this.script = script;
        this.value = value;
        this.type = type;
    }

    //Use the item. If it is an equip, then this equips the item
    public void use(IslandGame game) {
        script.execute(game);
    }

    public String getName() {
        return name;
    }

    public boolean isEquippable() {
        return isEquippable;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return isEquippable == item.isEquippable &&
                value == item.value &&
                name.equals(item.name);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (name == null ? 0 : name.hashCode());
        hash = 31 * hash + (isEquippable ? 0 : 1);
        hash = 31 * hash + value;
        return hash;
    }

    public ItemType getItemType() {
        return type;
    }

    @Override
    public int compareTo(Item o) {
        //Check if items are equal
        if (equals(o))
            return 0;
        //Checks if equippable. Equippable items are sorted last
        if (isEquippable != o.isEquippable())
            return isEquippable ? 1 : -1;
        //Checks for item type
        if (!type.equals(o.getItemType()))
            return itemSortOrder.get(type) - itemSortOrder.get(o.getItemType());
        //Sort by value next
        if (value != o.getValue())
            return value - o.getValue();
        //Then sorts by alphabetical
        return name.compareTo(o.getName());
    }

    private interface ItemScript {
        //Executes the script
        void execute(IslandGame game);
    }

    /* Scripts for all items: */

    //Heals a specified amount of stamina
    private static class HealStamina implements ItemScript {

        int staminaAmount;

        HealStamina(int staminaAmount) {
            this.staminaAmount = staminaAmount;
        }

        @Override
        public void execute(IslandGame game) {
            int maxStamina = game.getPlayer().getMaxStamina();
            int currentStamina = game.getPlayer().getStamina();
            currentStamina += staminaAmount;
            game.getPlayer().setStamina(currentStamina > maxStamina ? maxStamina : currentStamina);
        }
    }



}
