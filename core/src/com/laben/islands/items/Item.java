package com.laben.islands.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
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
        masterItemSet.add(new Item("BreadCrumbs", 1, new HealStamina(1), ItemType.Heal));
        masterItemSet.add(new Item("Bread", 5, new HealStamina(5), ItemType.Heal));
        masterItemSet.add(new Item("MagicBread", 35, new HealStamina(20), ItemType.Heal));
        masterItemSet.add(new Item("StaminaSerum", 12, new HealStaminaPercent(.25), ItemType.Heal));
        masterItemSet.add(new Item("SuperSyrum", 50, new HealStaminaPercent(.5), ItemType.Heal));
        masterItemSet.add(new Item("GoldenSyrum", 91, new HealStaminaPercent(.75), ItemType.Heal));
        masterItemSet.add(new Item("MythicSyrum", 124, new HealStaminaPercent(1.0), ItemType.Heal));


    }


    private final String name;
    private final ItemScript script;
    private final int value;
    private ItemType type;

    public Item(String name, int value, ItemScript script, ItemType type) {
        this.name = name;
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


    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return value == item.value &&
                name.equals(item.name);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (name == null ? 0 : name.hashCode());
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



}
