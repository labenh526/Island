package com.laben.islands.Screens;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.laben.islands.IslandGame;

import java.util.HashMap;
import java.util.Map;

public class InventoryScreen extends InfoScreen{

    private TextureAtlas inventoryAtlas;

    private static Map<String, Class> inventoryScreenAssets;

    static {
        inventoryScreenAssets = new HashMap<>();
        inventoryScreenAssets.put("InventoryScreenTextures.atlas", TextureAtlas.class);
    }

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
    }


}
