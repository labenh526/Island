package com.laben.islands.Screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.laben.islands.IslandGame;
import com.laben.islands.items.Item;

import java.util.HashMap;
import java.util.Map;

public class InventoryScreen extends InfoScreen{

    private static Map<String, Class> inventoryScreenAssets;

    static {
        inventoryScreenAssets = new HashMap<>();
        inventoryScreenAssets.put("InventoryScreenTextures.atlas", TextureAtlas.class);
        inventoryScreenAssets.put("Fonts/InventoryItemListTitle.fnt", BitmapFont.class);
        inventoryScreenAssets.put("Fonts/InventoryItemName.fnt", BitmapFont.class);
    }

    private TextureAtlas inventoryAtlas;

    public InventoryScreen(IslandGame game) {
        super(game, ScreenType.INVENTORY, inventoryScreenAssets);

        inventoryAtlas = game.getManager().get("InventoryScreenTextures.atlas");

        getInfoBoxBg().validate();
        //Add inventory list box
        Image listBox = new Image(inventoryAtlas.findRegion("ListBox"));
        float listBoxWidth = getInfoBoxBg().getWidth() * .4f;
        float listBoxHeight = listBoxWidth * 6f / 7f;
        float listBoxX = getInfoBoxBg().getX() + getInfoBoxBg().getWidth() * .01f;
        float listBoxY = getInfoBoxBg().getY() + getInfoBoxBg().getHeight() * .14f;
        listBox.setSize(listBoxWidth, listBoxHeight);
        listBox.setPosition(listBoxX, listBoxY);
        getStage().addActor(listBox);

        //Create a table to show items in inventory
        Table inventoryTable = inventoryTable(listBoxWidth * .95f, listBoxHeight * .95f,
                listBoxX + .025f * listBoxWidth, listBoxY + .025f * listBoxHeight);
        getStage().addActor(inventoryTable);

    }

    public Table inventoryTable(float width, float height, float xpos, float ypos) {
        Table table = new Table();
        table.setSize(width, height);
        table.setPosition(xpos, ypos);
        float itemNameWidth = width * .8f;
        float itemNumWidth = width *.2f;
        //Add Item Name title
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = getGame().getManager().get("Fonts/InventoryItemListTitle.fnt");
        titleStyle.fontColor = Color.BLACK;
        Label nameTitle = new Label("Item", titleStyle);
        nameTitle.setAlignment(Align.topLeft);
        nameTitle.setWidth(itemNameWidth);
        Label numTitle = new Label("#", titleStyle);
        numTitle.setAlignment(Align.topLeft);
        numTitle.setWidth(itemNumWidth);
        nameTitle.setFontScale(.6f);
        numTitle.setFontScale(.6f);

        table.row().height(height / 8f);

        table.add(nameTitle).width(itemNameWidth).expandX().top();
        table.add(numTitle).width(itemNumWidth).expandX().top();

        Label.LabelStyle itemStyle = new Label.LabelStyle();
        itemStyle.font = getGame().getManager().get("Fonts/InventoryItemName.fnt");
        itemStyle.fontColor = Color.BLACK;

        //Add all items and their quantities
        for (Item item : getGame().getPlayer().getInventoryInBag()) {
            table.row().height(height / 8f);
            Label itemLabel = new Label(item.getName(), itemStyle);
            Label itemNumLabel = new Label(getGame().getPlayer().getInventory().get(item).toString(), itemStyle);
            itemLabel.setWidth(itemNameWidth);
            itemLabel.setFontScale(.35f);
            itemLabel.setAlignment(Align.topLeft);
            itemNumLabel.setFontScale(.35f);
            itemNumLabel.setAlignment(Align.topLeft);
            itemNumLabel.setWidth(itemNumWidth);
            table.add(itemLabel).width(itemNumWidth).expand().top().left();
            table.add(itemNumLabel).width(itemNumWidth).expand().top();
        }

        return table;
    }

    @Override
    public void dispose() {
        inventoryAtlas.dispose();
        super.dispose();
    }


}
