package com.laben.islands.items;

import com.laben.islands.IslandGame;

import java.util.*;

/**All items extend from this class**/
public class Item implements Comparable<Item>{

    public static final List<Item> masterItemSet; //Set of all items in the game

    private enum ItemType {Heal, HealPercent, BuffStamina}

    private static final Map<ItemType, Integer> itemSortOrder;

    static {
        itemSortOrder = new HashMap<>();
        itemSortOrder.put(ItemType.Heal, 0);
        itemSortOrder.put(ItemType.HealPercent, 1);
        itemSortOrder.put(ItemType.BuffStamina, 2);

        masterItemSet = new ArrayList<>(50);
        masterItemSet.add(new Item("BreadCrumbs", 1, new HealStamina(1), ItemType.Heal));
        masterItemSet.add(new Item("Bread", 5, new HealStamina(5), ItemType.Heal));
        masterItemSet.add(new Item("MagicBread", 35, new HealStamina(20), ItemType.Heal));
        masterItemSet.add(new Item("StaminaSerum", 12, new HealStaminaPercent(.25), ItemType.HealPercent));
        masterItemSet.add(new Item("SuperSerum", 50, new HealStaminaPercent(.5), ItemType.HealPercent));
        masterItemSet.add(new Item("GoldenSerum", 91, new HealStaminaPercent(.75), ItemType.HealPercent));
        masterItemSet.add(new Item("MythicSerum", 124, new HealStaminaPercent(1.0), ItemType.HealPercent));
        masterItemSet.add(new Item("Nectar", 127, new BuffMaxStamina(1), ItemType.BuffStamina));
        masterItemSet.add(new Item("GoldenNectar", 178, new BuffMaxStamina(2), ItemType.BuffStamina));
        masterItemSet.add(new Item("HeavensNectar", 300, new BuffMaxStamina(5), ItemType.BuffStamina));



    }


    private final String nameKey;
    private final ItemScript script;
    private final int value;
    private ItemType type;

    public Item(String nameKey, int value, ItemScript script, ItemType type) {
        this.nameKey = nameKey;
        this.script = script;
        this.value = value;
        this.type = type;
    }

    //Use the item. If it is an equip, then this equips the item
    public void use(IslandGame game) {
        script.execute(game);
    }

    public String getNameKey() {
        return nameKey;
    }


    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return value == item.value &&
                nameKey.equals(item.nameKey);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (nameKey == null ? 0 : nameKey.hashCode());
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
        //Checks for item type
        if (!type.equals(o.getItemType()))
            return itemSortOrder.get(type) - itemSortOrder.get(o.getItemType());
        //Sort by value next
        if (value != o.getValue())
            return value - o.getValue();
        //Then sorts by alphabetical
        return nameKey.compareTo(o.getNameKey());
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

    //Heals stamina by a % of max stamina
    private static class HealStaminaPercent implements ItemScript {
        double percent;

        HealStaminaPercent(double percent) {
            this.percent = percent;
        }

        @Override
        public void execute(IslandGame game) {
            int maxStamina = game.getPlayer().getMaxStamina();
            int currentStamina = game.getPlayer().getStamina();
            int percentStamina = (int)((double)maxStamina * percent);
            currentStamina += percentStamina;
            game.getPlayer().setStamina(currentStamina > maxStamina ? maxStamina : currentStamina);
        }
    }

    //Increases max stamina by a given amount
    private static class BuffMaxStamina implements ItemScript {
        int amount;

        BuffMaxStamina(int amount) {
            this.amount = amount;
        }

        @Override
        public void execute(IslandGame game) {
            int maxStamina = game.getPlayer().getMaxStamina();
            int currentStamina = game.getPlayer().getStamina();
            game.getPlayer().setMaxStamina(maxStamina + amount);
            game.getPlayer().setStamina(currentStamina + amount);
        }
    }



}
