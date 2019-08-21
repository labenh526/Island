package com.laben.islands.Screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
        inventoryScreenAssets.put("Fonts/ItemDescriptionTitle.fnt", BitmapFont.class);
        inventoryScreenAssets.put("Fonts/ItemDescriptionText.fnt", BitmapFont.class);
        inventoryScreenAssets.put("ItemTextures.atlas", TextureAtlas.class);
    }

    private TextureAtlas inventoryAtlas;
    private Table inventoryTable;
    private I18NBundle itemBundle;
    private TextureAtlas itemAtlas;
    private int pageNum; //The page number
    private int maxPageNum; //The last page
    private final float inventoryTableWidth;
    private final float inventoryTableHeight;
    private final float inventoryTableX;
    private final float inventoryTableY;
    private Label pageLabel;
    private Label.LabelStyle itemStyle;
    private Label.LabelStyle selectedItemStyle;
    private Label currentlySelectedLabel1;
    private Label currentlySelectedLabel2;
    private Item currentlySelectedItem;
    private Label descTitle;
    private Image descImage;
    private Label descText;
    private Label useLabel;
    private Label valueLabel;

    public InventoryScreen(IslandGame game) {
        super(game, ScreenType.INVENTORY, inventoryScreenAssets);

        inventoryAtlas = game.getManager().get("InventoryScreenTextures.atlas");
        itemBundle = game.getManager().get("i18n/ItemBundle");
        itemAtlas = game.getManager().get("ItemTextures.atlas");

        currentlySelectedItem = null;

        pageNum = 0; //default to page 1 (index 0)
        maxPageNum = (int)Math.ceil((double)game.getPlayer().getInventoryInBag().size() / (double)PAGE_LENGTH) - 1;

        itemStyle = new Label.LabelStyle();
        itemStyle.font = getGame().getManager().get("Fonts/InventoryItemName.fnt");
        itemStyle.fontColor = Color.BLACK;
        selectedItemStyle = new Label.LabelStyle(itemStyle);
        selectedItemStyle.background = new TextureRegionDrawable(inventoryAtlas.findRegion("SelectedLabel"));


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
                if (currentlySelectedItem != null) {
                    int pageNumOfItem = pageNumOfItem(currentlySelectedItem);
                    if (pageNumOfItem != pageNum) {
                        setPageNum(pageNumOfItem);
                    }
                }
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
        final float pageLabelWidth = listBoxWidth;
        final float pageLabelHeight = listBoxHeight * .1f;
        final float pageLabelX = listBoxX;
        final float pageLabelY = listBoxY + listBoxHeight * 1.01f;
        pageLabel = new Label(pageLabelText(), itemStyle);
        pageLabel.setSize(pageLabelWidth, pageLabelHeight);
        pageLabel.setPosition(pageLabelX, pageLabelY);
        pageLabel.setAlignment(Align.bottomLeft);
        pageLabel.setFontScale(.3f);
        getStage().addActor(pageLabel);

        /* Initialize Description Box */
        //Add description box bg
        final float descBoxSideSize = getInfoBoxBg().getWidth() * .5f;
        final float descBoxXPos = listBoxX + listBoxWidth + (getInfoBoxBg().getX() + getInfoBoxBg().getWidth() - listBoxX
        - listBoxWidth - descBoxSideSize) / 2;
        final float descBoxYPos = getInfoBoxBg().getY() + (getInfoBoxBg().getHeight() - descBoxSideSize) / 2;
        Image descBox = new Image(inventoryAtlas.findRegion("DescBox"));
        descBox.setSize(descBoxSideSize, descBoxSideSize);
        descBox.setPosition(descBoxXPos, descBoxYPos);
        getStage().addActor(descBox);

        //Add desc title
        final float descTitleWidth = descBoxSideSize * 31f / 32f;
        final float descTitleHeight = descBoxSideSize * .15f;
        final float descTitleX = descBoxXPos + descBoxSideSize * 1f / 64f;
        final float descTitleY = descBoxYPos + descBoxSideSize * 63f / 64f - descTitleHeight;
        Label.LabelStyle descTitleStyle = new Label.LabelStyle();
        descTitleStyle.font = getGame().getManager().get("Fonts/ItemDescriptionTitle.fnt");
        descTitleStyle.fontColor = Color.BLACK;
        descTitle = new Label(itemBundle.get("EmptyItem"), descTitleStyle);
        descTitle.setFontScale(.6f);
        descTitle.setPosition(descTitleX, descTitleY);
        descTitle.setSize(descTitleWidth, descTitleHeight);
        descTitle.setAlignment(Align.center);
        getStage().addActor(descTitle);

        //Add image
        final float descImageSideSize = descTitleWidth * .3f;
        final float descImageX = descTitleX + (descTitleWidth - descImageSideSize) / 2f;
        final float descImageY = descTitleY - descImageSideSize;
        descImage = new Image(itemAtlas.findRegion("Empty"));
        descImage.setSize(descImageSideSize, descImageSideSize);
        descImage.setPosition(descImageX, descImageY);
        getStage().addActor(descImage);

        //Add description text
        final float descTextWidth = descBoxSideSize * 15f / 16f; //Extra pixel on each side for margins
        final float descTextHeight = descBoxSideSize * .3f;
        final float descTextX = descBoxXPos + descBoxSideSize * 1f / 32f;
        final float descTextY = descImageY - descTextHeight - descImageSideSize * .2f;
        Label.LabelStyle descTextStyle = new Label.LabelStyle();
        descTextStyle.font = getGame().getManager().get("Fonts/ItemDescriptionText.fnt");
        descTextStyle.fontColor = Color.BLACK;
        descText = new Label(itemBundle.get("EmptyDesc"), descTextStyle);
        descText.setFontScale(.4f);
        descText.setPosition(descTextX, descTextY);
        descText.setSize(descTextWidth, descTextHeight);
        descText.setAlignment(Align.topLeft);
        descText.setWrap(true);
        getStage().addActor(descText);

        //Add use button
        final float useLabelWidth = descBoxSideSize * .2f;
        final float useLabelHeight = useLabelWidth * 3f / 4f;
        final float useLabelX = descTextX;
        final float useLabelY = descTextY - useLabelHeight * 1.05f;
        Label.LabelStyle useLabelStyle = new Label.LabelStyle();
        useLabelStyle.font = getGame().getManager().get("Fonts/InventoryItemName.fnt");
        useLabelStyle.fontColor = Color.BLACK;
        useLabelStyle.background = new TextureRegionDrawable(inventoryAtlas.findRegion("UseButton"));
        useLabel = new Label(getInfoBundle().get("Use"), useLabelStyle);
        useLabel.setPosition(useLabelX, useLabelY);
        useLabel.setSize(useLabelWidth, useLabelHeight);
        useLabel.setAlignment(Align.center);
        useLabel.setFontScale(.5f);
        getStage().addActor(useLabel);
        darkenUseButton();
        useLabel.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (currentlySelectedItem.usable(getGame()))
                    useCurrentlySelectedItem();
            }
        });

        //Add value text
        final float valueTextWidth = descBoxXPos + descBoxSideSize - useLabelX - useLabelWidth;
        final float valueTextHeight = useLabelHeight;
        final float valueTextX = useLabelX + useLabelWidth * 1.2f;
        final float valueTextY = useLabelY;
        Label.LabelStyle valueTextStyle = new Label.LabelStyle(descTextStyle);
        valueTextStyle.fontColor = Color.CYAN;
        valueLabel = new Label(getInfoBundle().get("Value")+ ": ??", valueTextStyle);
        valueLabel.setPosition(valueTextX, valueTextY);
        valueLabel.setSize(valueTextWidth, valueTextHeight);
        valueLabel.setAlignment(Align.left);
        valueLabel.setFontScale(.4f);
        getStage().addActor(valueLabel);

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
            table.add(itemLabel).width(itemNameWidth).fillY().top().left();
            table.add(itemNumLabel).width(itemNumWidth).fillY().top();
            addInputToItemInInventory(itemLabel, item, itemLabel, itemNumLabel);
            addInputToItemInInventory(itemNumLabel, item, itemLabel, itemNumLabel);
            if (item.equals(currentlySelectedItem))
                setCurrentlySelectedLabel(itemLabel, itemNumLabel);
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

    private void addInputDetection(Image arrow, final boolean left) {
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

    private void resetInventoryTable() {
        inventoryTable.remove();
        inventoryTable = inventoryTable(inventoryTableWidth, inventoryTableHeight,
                inventoryTableX, inventoryTableY);
        getStage().addActor(inventoryTable);
    }

    private void decrementPageNum() {
        if (pageNum <= 0)
            pageNum = maxPageNum;
        else
            pageNum--;
        pageLabel.setText(pageLabelText());
    }

    private void incrementPageNum() {
        if (pageNum >= maxPageNum)
            pageNum = 0;
        else
            pageNum++;
        pageLabel.setText(pageLabelText());
    }

    private void setPageNum(int pageNum) {
        this.pageNum = pageNum;
        pageLabel.setText(pageLabelText());
    }

    private int pageNumOfItem(Item item) {
        int itemIndex = new ArrayList<>(getGame().getPlayer().getInventoryInBag()).indexOf(item);
        return itemIndex / PAGE_LENGTH;
    }

    private String pageLabelText() {
        StringBuilder sb = new StringBuilder(getInfoBundle().get("Page"));
        sb.append(" ");
        sb.append(pageNum + 1);
        sb.append("/");
        sb.append(maxPageNum + 1);
        return sb.toString();
    }

    private void darkenUseButton() {
        useLabel.setColor(1f, 1f, 1f, .4f);
    }

    private void normalizeUseButtonColor() {
        useLabel.setColor(1f, 1f, 1f, 1f);
    }

    private void setCurrentlySelectedItem(Item item) {
        currentlySelectedItem = item;
        descTitle.setText(itemBundle.get(item == null ? "EmptyItem" : item.getNameKey()));
        descText.setText(itemBundle.get(item == null ? "EmptyDesc" : item.getDescKey()));
        descImage.validate();
        float descImageX = descImage.getX();
        float descImageY = descImage.getY();
        float descImageWidth = descImage.getWidth();
        float descImageHeight = descImage.getHeight();
        descImage.remove();
        descImage = new Image(itemAtlas.findRegion(item == null ? "Empty" : item.getNameKey()));
        descImage.setPosition(descImageX, descImageY);
        descImage.setSize(descImageWidth, descImageHeight);
        getStage().addActor(descImage);
        valueLabel.setText(getInfoBundle().get("Value") + ": " + (item == null ? "??" : item.getValue()));

        if (item != null && item.usable(getGame()))
            normalizeUseButtonColor();
        else
            darkenUseButton();
    }

    private void addInputToItemInInventory(Label label, final Item item, final Label selectedLabel1, final Label selectedLabel2) {
        label.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                setCurrentlySelectedItem(item);
                setCurrentlySelectedLabel(selectedLabel1, selectedLabel2);
            }
        });
    }

    private void setCurrentlySelectedLabel(Label label1, Label label2) {
        if (currentlySelectedLabel1 != null && currentlySelectedLabel2 != null) {
            currentlySelectedLabel1.setStyle(itemStyle);
            currentlySelectedLabel2.setStyle(itemStyle);
        }
        currentlySelectedLabel1 = label1;
        currentlySelectedLabel2 = label2;
        label1.setStyle(selectedItemStyle);
        label2.setStyle(selectedItemStyle);
    }

    private void useCurrentlySelectedItem() {
        getGame().getPlayer().removeItemFromInventory(currentlySelectedItem);
        Item itemToUse = currentlySelectedItem;
        if (getGame().getPlayer().getInventory().get(itemToUse) <= 0)
            deselectCurrentItem();
        resetInventoryTable();
        itemToUse.use(getGame());
        if (currentlySelectedItem != null && !currentlySelectedItem.usable(getGame()))
            darkenUseButton();
    }

    private void deselectCurrentItem() {
        if (currentlySelectedItem != null) {
            setCurrentlySelectedItem(null);
            currentlySelectedLabel1.setStyle(itemStyle);
            currentlySelectedLabel2.setStyle(itemStyle);
            currentlySelectedLabel1 = null;
            currentlySelectedLabel2 = null;
        }
    }



}
