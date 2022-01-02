package com.laben.islands.items;

import com.badlogic.gdx.math.GridPoint2;
import com.laben.islands.IslandGame;
import com.laben.islands.Region;
import com.laben.islands.Tile;

import java.util.*;

/**All items extend from this class**/
public class Item implements Comparable<Item>{

    public static final List<Item> masterItemSet; //Set of all items in the game

    private enum ItemType {Heal, HealPercent, BuffStamina, Valuable, Warp}

    private static final Map<ItemType, Integer> itemSortOrder;

    static {
        itemSortOrder = new HashMap<>();
        itemSortOrder.put(ItemType.Heal, 0);
        itemSortOrder.put(ItemType.HealPercent, 1);
        itemSortOrder.put(ItemType.BuffStamina, 2);
        itemSortOrder.put(ItemType.Warp, 3);
        itemSortOrder.put(ItemType.Valuable, 4);

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
        masterItemSet.add(new Item("SeaGlass", 75, new NoEffect(), ItemType.Valuable));
        masterItemSet.add(new Item("Ruby", 150, new NoEffect(), ItemType.Valuable));
        masterItemSet.add(new Item("Diamond", 250, new NoEffect(), ItemType.Valuable));
        masterItemSet.add(new Item("BlackDiamond", 500, new NoEffect(), ItemType.Valuable));
        masterItemSet.add(new Item("WarpStone", 11, new WarpAnywhere(), ItemType.Warp));
        masterItemSet.add(new Item("WarpScroll", 15, new WarpAnywhereInRegion(), ItemType.Warp));



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

    //Check if the item is currently usable
    public boolean usable(IslandGame game) {
        return script.usable(game);
    }

    public String getNameKey() {
        return nameKey;
    }

    public int getValue() {
        return value;
    }

    public String getDescKey() {
        return nameKey + "Desc";
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
        //Whether or not the item is usable
        boolean usable(IslandGame game);
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
            game.displayTextBox("StaminaRegen");
        }

        @Override
        public boolean usable(IslandGame game) {
            return game.getPlayer().getStamina() != game.getPlayer().getMaxStamina();
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
            game.displayTextBox("StaminaRegen");
        }

        @Override
        public boolean usable(IslandGame game) {
            return game.getPlayer().getStamina() != game.getPlayer().getMaxStamina();
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
            game.displayTextBox("MaxStaminaBuff");
        }

        @Override
        public boolean usable(IslandGame game) {
            return true;
        }
    }

    private static class NoEffect implements ItemScript {

        @Override
        public void execute(IslandGame game) {}

        @Override
        public boolean usable(IslandGame game) {
            return false;
        }

    }

    private static class WarpAnywhere implements ItemScript {

        @Override
        public void execute(IslandGame game) {
            GridPoint2 currentPos = game.getCurrentTile().getCoordinates();
            Random random = new Random();
            GridPoint2 newPosition;
            do {
                newPosition = new GridPoint2(random.nextInt(game.getCurrentIsland().getWidth()),
                        random.nextInt(game.getCurrentIsland().getHeight()));
            } while(newPosition == currentPos);

            game.setCurrentTile(game.getCurrentIsland().tileAtPoint(newPosition));
            game.displayTextBox("Warped");
        }

        @Override
        public boolean usable(IslandGame game) {
            return true;
        }
    }

    private static class WarpAnywhereInRegion implements ItemScript {

        @Override
        public void execute(IslandGame game) {
            Region currentRegion = game.getCurrentTile().getRegion();
            //Create list of all tiles in region
            List<Tile> tilesInRegion = new ArrayList<>(40);
            Iterator<Tile> iterator= game.getCurrentIsland().getIterableRegionMap().tileIterator();
            while (iterator.hasNext()) {
                Tile currentTile = iterator.next();
                if (currentTile.getRegion().equals(currentRegion))
                    tilesInRegion.add(currentTile);
            }
            tilesInRegion.remove(game.getCurrentTile());
            if (tilesInRegion.size() > 0)
                game.setCurrentTile(tilesInRegion.get(new Random().nextInt(tilesInRegion.size())));
            game.displayTextBox("Warped");
        }

        @Override
        public boolean usable(IslandGame game) {
            return true;
        }
    }



}
