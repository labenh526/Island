package com.laben.islands.Screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.laben.islands.IslandGame;
import com.laben.islands.items.Item;

import java.util.*;

public class InventoryScreen extends InfoScreen{

    private static final int PAGE_LENGTH = 11;

    private static Map<String, Class> inventoryScreenAssets;

    static {
        inventoryScreenAssets = new HashMap<>();
        inventoryScreenAssets.put("InventoryScreenTextures.atlas", TextureAtlas.class);
        inventoryScreenAssets.put("Fonts/InventoryItemListTitle.fnt", BitmapFont.class);
        inventoryScreenAssets.put("Fonts/InventoryItemName.fnt", BitmapFont.class);
        inventoryScreenAssets.put("i18n/ItemBundle", I18NBundle.class);
    }

    private TextureAtlas inventoryAtlas;
    private Table inventoryTable;
    private I18NBundle itemBundle;
    private int pageNum; //The page number
    private int maxPageNum; //The last page
    private final float inventoryTableWidth;
    private final float inventoryTableHeight;
    private final float inventoryTableX;
    private final float inventoryTableY;
    private Label pageLabel;
    private Label.LabelStyle itemStyle;

    public InventoryScreen(IslandGame game) {
        super(game, ScreenType.INVENTORY, inventoryScreenAssets);

        inventoryAtlas = game.getManager().get("InventoryScreenTextures.atlas");
        itemBundle = game.getManager().get("i18n/ItemBundle");

        pageNum = 0; //default to page 1 (index 0)
        maxPageNum = (int)Math.ceil((double)game.getPlayer().getInventoryInBag().size() / (double)PAGE_LENGTH) - 1;

        itemStyle = new Label.LabelStyle();
        itemStyle.font = getGame().getManager().get("Fonts/InventoryItemName.fnt");
        itemStyle.fontColor = Color.BLACK;

        getInfoBoxBg().validate();
        //Add inventory list box
        Image listBox = new Image(inventoryAtlas.findRegion("ListBox"));
        final float listBoxWidth = getInfoBoxBg().getWidth() * .4f;
        final float listBoxHeight = listBoxWidth * 6f / 7f;
        final float listBoxX = getInfoBoxBg().getX() + getInfoBoxBg().getWidth() * .01f;
        final float listBoxY = getInfoBoxBg().getY() + getInfoBoxBg().getHeight() * .14f;
        listBox.setSize(listBoxWidth, listBoxHeight);
        listBox.setPosition(listBoxX, listBoxY);
        getStage().addActor(listBox);

        //Create a table to show items in inventory
        inventoryTableWidth = listBoxWidth * .95f;
        inventoryTableHeight = listBoxHeight * .95f;
        inventoryTableX = listBoxX + .025f * listBoxWidth;
        inventoryTableY = listBoxY + .025f * listBoxHeight;

        inventoryTable = inventoryTable(inventoryTableWidth, inventoryTableHeight, inventoryTableX, inventoryTableY);
        getStage().addActor(inventoryTable);

        //Add item sort button
        final float sortSideSize = listBoxWidth * .15f;
        final float sortXPos = listBoxX + listBoxWidth * .2f;
        final float sortYPos = listBoxY - sortSideSize - listBoxHeight * .02f;
        Image sortButton = new Image(inventoryAtlas.findRegion("SortButton"));
        sortButton.setSize(sortSideSize, sortSideSize);
        sortButton.setPosition(sortXPos, sortYPos);
        getStage().addActor(sortButton);
        sortButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                getGame().getPlayer().sortInventory();
                //Re-add inventory table
                resetInventoryTable();
            }
        });

        //Add left arrow
        Image leftArrow = new Image(inventoryAtlas.findRegion("LeftButton"));
        leftArrow.setSize(sortSideSize, sortSideSize);
        leftArrow.setPosition(listBoxX, sortYPos);
        getStage().addActor(leftArrow);
        addInputDetection(leftArrow, true);

        //Add right arrow
        Image rightArrow = new Image(inventoryAtlas.findRegion("RightButton"));
        rightArrow.setSize(sortSideSize, sortSideSize);
        rightArrow.setPosition(listBoxX + listBoxWidth - sortSideSize, sortYPos);
        getStage().addActor(rightArrow);
        addInputDetection(rightArrow,  false);

        //Add page number label
        float pageLabelWidth = listBoxWidth;
        float pageLabelHeight = listBoxHeight * .1f;
        float pageLabelX = listBoxX;
        float pageLabelY = listBoxY + listBoxHeight * 1.01f;
        pageLabel = new Label(pageLabelText(), itemStyle);
        pageLabel.setSize(pageLabelWidth, pageLabelHeight);
        pageLabel.setPosition(pageLabelX, pageLabelY);
        pageLabel.setAlignment(Align.bottomLeft);
        pageLabel.setFontScale(.3f);
        getStage().addActor(pageLabel);

    }

    public Table inventoryTable(float width, float height, float xpos, float ypos) {
        float tableCellHeight = height / 12f;

        int startItemPos = pageNum * PAGE_LENGTH;
        int endItemPage = Math.min((pageNum + 1) * PAGE_LENGTH, getGame().getPlayer().getInventoryInBag().size());
        List<Item> itemsOnPage = new ArrayList<>(getGame().getPlayer().getInventoryInBag()).subList(startItemPos, endItemPage);

        Table table = new Table();
        table.setSize(width, height);
        table.setPosition(xpos, ypos);
        float itemNameWidth = width * .8f;
        float itemNumWidth = width *.2f;
        //Add Item Name title
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = getGame().getManager().get("Fonts/InventoryItemListTitle.fnt");
        titleStyle.fontColor = Color.BLACK;
        Label nameTitle = new Label(getInfoBundle().get("Item"), titleStyle);
        nameTitle.setAlignment(Align.topLeft);
        nameTitle.setWidth(itemNameWidth);
        Label numTitle = new Label("#", titleStyle);
        numTitle.setAlignment(Align.topLeft);
        numTitle.setWidth(itemNumWidth);
        nameTitle.setFontScale(.6f);
        numTitle.setFontScale(.6f);

        table.row().height(tableCellHeight);

        table.add(nameTitle).width(itemNameWidth).expandX().top();
        table.add(numTitle).width(itemNumWidth).expandX().top();


        Label itemNumLabel = null;
        Label itemLabel = null;
        //Add all items and their quantities
        for (Item item : itemsOnPage) {
            table.row().height(tableCellHeight);
            itemLabel = new Label(itemBundle.get(item.getNameKey()), itemStyle);
            itemNumLabel = new Label(getGame().getPlayer().getInventory().get(item).toString(), itemStyle);
            itemLabel.setWidth(itemNameWidth);
            itemLabel.setFontScale(.35f);
            itemLabel.setAlignment(Align.topLeft);
            itemNumLabel.setFontScale(.35f);
            itemNumLabel.setAlignment(Align.topLeft);
            itemNumLabel.setWidth(itemNumWidth);
            table.add(itemLabel).width(itemNumWidth).fillY().top().left();
            table.add(itemNumLabel).width(itemNumWidth).fillY().top();
        }
        //This essentially aligns the cells of the table to the top
        if (itemLabel != null) {
            table.getCell(itemLabel).expand();
            table.getCell(itemNumLabel).expand();
        }

        return table;
    }

    @Override
    public void dispose() {
        inventoryAtlas.dispose();
        super.dispose();
    }

    public void addInputDetection(Image arrow, final boolean left) {
        if (pageNum != maxPageNum) {
            arrow.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    if (left)
                        decrementPageNum();
                    else
                        incrementPageNum();
                    //Re-add inventory table
                    resetInventoryTable();
                }
            });
        }
    }

    public void resetInventoryTable() {
        inventoryTable.remove();
        inventoryTable = inventoryTable(inventoryTableWidth, inventoryTableHeight,
                inventoryTableX, inventoryTableY);
        getStage().addActor(inventoryTable);
    }

    public void decrementPageNum() {
        if (pageNum <= 0)
            pageNum = maxPageNum;
        else
            pageNum--;
        pageLabel.setText(pageLabelText());
    }

    public void incrementPageNum() {
        if (pageNum >= maxPageNum)
            pageNum = 0;
        else
            pageNum++;
        pageLabel.setText(pageLabelText());
    }

    public String pageLabelText() {
        StringBuilder sb = new StringBuilder(getInfoBundle().get("Page"));
        sb.append(" ");
        sb.append(pageNum + 1);
        sb.append("/");
        sb.append(maxPageNum + 1);
        return sb.toString();
    }


}
