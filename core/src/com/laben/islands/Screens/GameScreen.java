package com.laben.islands.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.laben.islands.*;

import java.util.*;

public class GameScreen extends AbstractScreen{

    public static final float GAME_TABLE_WIDTH = .55f * (float) IslandGame.GAME_WIDTH;
    public static final float GAME_TABLE_HEIGHT = 11f / 12f * (float)IslandGame.getGameHeight();

    private Stage stage;
    private Map<String, Class> assets;
    private Table gameTable;
    private IslandGame game;
    private TextureAtlas atlas;
    private final Tile tile;
    private Label staminaLabel;
    private Table rootTable;
    private IslandGame.StaminaBar staminaBar;


    public GameScreen(final IslandGame game, final Tile tile) {
        this.game = game;
        this.tile = tile;
        stage = new Stage(new FitViewport(IslandGame.getGameWidth(), IslandGame.getGameHeight()));
        Gdx.input.setInputProcessor(stage);

        //Load assets
        assets = new HashMap<>();
        assets.put("GameScreenTextures.atlas", TextureAtlas.class);
        assets.put("Fonts/StaminaTextFont.fnt", BitmapFont.class);
        assets.put("Fonts/GameScreenRegion.fnt", BitmapFont.class);
        IslandGame.loadAllAssets(game.getManager(), assets);
        game.getManager().finishLoading();
        atlas = game.getManager().get("GameScreenTextures.atlas");


        //Create root table
        rootTable = new Table();
        rootTable.setRound(false);
        stage.addActor(rootTable);
        rootTable.setFillParent(true);

        //Add gameview table
        gameTable = initializeGameTable();
        rootTable.row().height(GAME_TABLE_HEIGHT);
        float leftPadding  = (.05f * (float)IslandGame.GAME_WIDTH);
        float topBottomPadding = 2.5f / 60.0f * (float)IslandGame.GAME_HEIGHT;
        rootTable.add(gameTable).expand().width(GAME_TABLE_WIDTH).padLeft(leftPadding).padTop(topBottomPadding)
                .padBottom(topBottomPadding).left();

        rootTable.validate();
        gameTable.validate();

        //Add trees
        addTrees();

        //Create stamina text label
        Label.LabelStyle staminaLabelStyle = new Label.LabelStyle();
        staminaLabelStyle.font = game.getManager().get("Fonts/StaminaTextFont.fnt");
        staminaLabelStyle.fontColor = Color.BLACK;
        staminaLabel = new Label("Stamina", staminaLabelStyle);
        staminaLabel.setAlignment(Align.topLeft);
        staminaLabel.setSize(.4f * IslandGame.getGameWidth(), .5f / 6f * IslandGame.getGameHeight());
        stage.addActor(staminaLabel);
        float staminaLabelXPos = gameTable.localToStageCoordinates(new Vector2(gameTable.getWidth(),
                gameTable.getHeight())).x + .02f * (float)IslandGame.getGameWidth();
        float staminaLabelYPos = gameTable.localToStageCoordinates(new Vector2(gameTable.getWidth(),
                gameTable.getHeight())).y - staminaLabel.getHeight();
        staminaLabel.setPosition(staminaLabelXPos, staminaLabelYPos);


        staminaLabel.validate();
        float staminaBarWidth = .35f * (float)IslandGame.getGameWidth();
        float staminaBarHeight = .06f * (float)IslandGame.getGameHeight();
        float staminaBarX = staminaLabel.getX();
        float staminaBarY = staminaLabel.getY()  - staminaBarHeight  + .04f * (float)IslandGame.getGameHeight();

        staminaBar = IslandGame.createStaminaBar(stage, atlas.findRegion("StaminaBackground"), atlas.findRegion("GreyStaminaBackground"),
                atlas.findRegion("StaminaBar"), staminaBarX, staminaBarY, staminaBarWidth, staminaBarHeight);

        Image staminaBackground = staminaBar.getBlackBackground();

        //Create level text (uses same font as stamina)
        Label.LabelStyle levelLabelStyle = new Label.LabelStyle();
        levelLabelStyle.font = game.getManager().get("Fonts/StaminaTextFont.fnt");
        levelLabelStyle.fontColor = Color.BLACK;
        Label levelLabel = new Label("Level " + game.getCurrentLevel(), staminaLabelStyle);
        levelLabel.setAlignment(Align.bottomRight);
        levelLabel.setSize(.4f * IslandGame.getGameWidth(), .5f / 6f * IslandGame.getGameHeight());
        levelLabel.validate();
        stage.addActor(levelLabel);
        float levelLabelXPos = staminaBackground.getX() + staminaBackground.getWidth() - levelLabel.getWidth();
        float levelLabelYPos = staminaBackground.getY() + staminaBackground.getHeight();
        levelLabel.setPosition(levelLabelXPos, levelLabelYPos);
        stage.addActor(levelLabel);

        //Create mini map bg
        Image miniMapBackground = new Image(atlas.findRegion("MiniMapBackground"));
        miniMapBackground.setPosition(staminaBackground.getX(), staminaBackground.getY() -
                .05f * IslandGame.getGameHeight() - staminaBackground.getWidth());
        miniMapBackground.setSize(staminaBackground.getWidth(), staminaBackground.getWidth());
        stage.addActor(miniMapBackground);

        //Create mini map
        miniMapBackground.validate();
        Table miniMap = initializedMiniMapTable();
        miniMap.setPosition(miniMapBackground.getX() + miniMapBackground.getWidth() * .025f,
                miniMapBackground.getY() + miniMapBackground.getWidth() * .025f);
        miniMap.setSize(miniMapBackground.getWidth() * .95f, miniMapBackground.getWidth() * .95f);
        stage.addActor(miniMap);
        miniMap.toFront();

        //Add current location marker to mini map
        miniMap.validate();
        Image redDot = new Image(atlas.findRegion("reddot"));
        redDot.setSize(miniMap.getColumnWidth(0), miniMap.getRowHeight(0));
        float xStartPos = miniMap.localToStageCoordinates(new Vector2(0, 0)).x;
        float yStartPos = miniMap.localToStageCoordinates(new Vector2(0, 0)).y;
        float xMod = miniMap.getWidth() / (float)miniMap.getColumns() * (float)game.getCurrentTile()
                .getCoordinates().x;
        float yMod = miniMap.getHeight() / (float)miniMap.getRows() *
                ((float)miniMap.getRows() - 1 - (float)game.getCurrentTile().getCoordinates().y);
        redDot.setPosition(xStartPos + xMod, yStartPos + yMod);
        addMapViewListener(redDot);
        stage.addActor(redDot);
        redDot.toFront();

        //Add arrows
        if (tile.tileAbove() != null)
            addArrow(new BitSet(2));
        if (tile.tileRight() != null) {
            BitSet right = new BitSet();
            right.set(0);
            addArrow(right);
        }
        if (tile.tileBelow() != null) {
            BitSet bottom = new BitSet();
            bottom.set(1);
            addArrow(bottom);
        }
        if (tile.tileLeft() != null) {
            BitSet left = new BitSet();
            left.set(0);
            left.set(1);
            addArrow(left);
        }

        //Create region text background
        Image regionTextBackground = new Image(atlas.findRegion(tile.getRegion().getTerrain().toString() + "Map"));
        float regionTextBoxWidth = gameTable.getWidth() * .33f;
        float regionTextBoxHeight = gameTable.getHeight() * .15f;
        regionTextBackground.toFront();
        float regionTextBoxPosX = gameTable.getX();
        float regionTextBoxPosY = gameTable.getY() + gameTable.getHeight() - regionTextBoxHeight;
        regionTextBackground.setSize(regionTextBoxWidth, regionTextBoxHeight);
        regionTextBackground.setPosition(regionTextBoxPosX, regionTextBoxPosY);
        stage.addActor(regionTextBackground);

        Image regionTextBox = new Image(atlas.findRegion("RegionTextBox"));
        regionTextBox.toFront();
        regionTextBox.setPosition(regionTextBoxPosX, regionTextBoxPosY);
        regionTextBox.setSize(regionTextBoxWidth, regionTextBoxHeight);
        stage.addActor(regionTextBox);

        //Add text onto region text background
        Label.LabelStyle regionTextStyle = new Label.LabelStyle();
        regionTextStyle.font = game.getManager().get("Fonts/GameScreenRegion.fnt");
        regionTextStyle.fontColor = Color.BLACK;
        String[] splitRegionNameArray = tile.getRegion().toString().split("\\s+");
        StringBuilder splitRegionName = new StringBuilder(splitRegionNameArray[0]);
        splitRegionName.append("\n");
        splitRegionName.append(game.getGeneralBundle().get(splitRegionNameArray[1]));
        Label regionLabel = new Label(splitRegionName.toString(), regionTextStyle);
        //regionLabel.setAlignment(Align.topLeft);
        regionLabel.setSize(regionTextBoxWidth * .9f, regionTextBoxHeight * .9f);
        regionLabel.setPosition(regionTextBoxPosX + regionTextBoxWidth * .05f,
                regionTextBoxPosY + regionTextBoxHeight * .05f);
        regionLabel.setFontScale(.3f);
        stage.addActor(regionLabel);

        //Add info button
        Image infoButton = new Image(atlas.findRegion("InfoButton"));
        float infoButtonSideSize = gameTable.getWidth() * .2f;
        infoButton.setSize(infoButtonSideSize, infoButtonSideSize);
        infoButton.setPosition(miniMapBackground.getX(), miniMapBackground.getY() - infoButtonSideSize * 1.05f);
        stage.addActor(infoButton);
        infoButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                dispose();
                game.setScreen(new InventoryScreen(game));
            }
        });

        //Add treasure
        if (game.getCurrentTile().hasTreasure()) {
            float treasureSideSize = gameTable.getWidth() / 5f;
            float treasureX = gameTable.getX() + treasureSideSize * 2f;
            float treasureY = gameTable.getY() + treasureSideSize * 3f;
            Image treasure = new Image(atlas.findRegion("Treasure"));
            treasure.setPosition(treasureX, treasureY);
            treasure.setSize(treasureSideSize, treasureSideSize);
            stage.addActor(treasure);
        }

    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        //Game Logic:



        //Graphics:
        Gdx.gl.glClearColor((float)(204/255.0), 0, (float)(102/255.0), 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //Set Stamina text
        staminaLabel.setText(staminaString());
        staminaBar.updateStaminaInstantaneous(game);
        stage.draw();


        //Check if arrow keys pressed
        if (game.isInputAllowed()) {
            if (tile.tileAbove() != null && Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                game.setCurrentTile(tile.tileAbove());
                game.loadGameScreen();
            } else if (tile.tileBelow() != null && Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                game.setCurrentTile(tile.tileBelow());
                game.loadGameScreen();
            } else if (tile.tileRight() != null && Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                game.setCurrentTile(tile.tileRight());
                game.loadGameScreen();
            } else if (tile.tileLeft() != null && Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                game.setCurrentTile(tile.tileLeft());
                game.loadGameScreen();
            }
        }

    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        IslandGame.unloadAllAssets(game.getManager(), assets.keySet());
        atlas.dispose();
    }

    private Table initializedMiniMapTable() {
        Island.IterableRegionMap regionMap = game.getCurrentIsland().getIterableRegionMap();
        Table table = new Table();
        table.setRound(false);
        //Get image files for each terrain type
        Map<Terrain, TextureAtlas.AtlasRegion> terrainAtlasRegionMap = new HashMap<>();
        for (Terrain terrain : Terrain.TERRAIN_SET) {
            StringBuilder terrainFileName = new StringBuilder(terrain.toString());
            terrainFileName.append("Map");
            terrainAtlasRegionMap.put(terrain, atlas.findRegion(terrainFileName.toString()));
        }
        //Iterate through each row
        for (List<Region> row : regionMap) {
            //Iterate through each tile in row's region
            for (Region region : row) {
                Image image = new Image(terrainAtlasRegionMap.get(region.getTerrain()));
                table.add(image).uniform().expand().fill();
            }
            table.row();
        }
        addMapViewListener(table);
        return table;
    }

    private Table initializeGameTable() {
        Table table = new Table();
        //table.setBackground(new TextureRegionDrawable(atlas.findRegion(tile.getRegion().getTerrain().getBackgroundImage())));
        Tile.GraphicsItem[][] gItems = tile.getGraphicsItemsTable();
        for (Tile.GraphicsItem[] row : gItems) {
            for (Tile.GraphicsItem item : row) {
                String modifier;
                if (item.equals(Tile.GraphicsItem.ROCK))
                    modifier = "Rock";
                else if (item.equals(Tile.GraphicsItem.GRASS))
                    modifier = "Grass";
                else modifier = "Background";
                Image itemImage = new Image(atlas.findRegion(tile.getRegion().getTerrain().toString() + modifier));
                table.add(itemImage).uniform().expand().fill();
            }
            table.row();
        }

        return table;
    }

    private String staminaString() {
        StringBuilder staminaText = new StringBuilder(game.getGeneralBundle().get("Stamina"));
        staminaText.append(" (");
        staminaText.append(game.getPlayer().getStamina());
        staminaText.append("/");
        staminaText.append(game.getPlayer().getMaxStamina());
        staminaText.append(")");
        return staminaText.toString();
    }


    private void addMapViewListener(Actor actor) {
        actor.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                game.loadMapViewScreen();
            }
        });
    }

    /* Adds a specific arrow to the game screen. The arrow is represented in binary by using a bitset in order to
       minimize cost. The binary values from 0-3 for each arrow is as follows: Top, Right, Bottom, Left
     */
    private void addArrow(BitSet arrow) {
        //BitSets
        BitSet right = new BitSet(2);
        right.set(0); //1
        BitSet bottom = new BitSet(2);
        bottom.set(1); //2
        BitSet left = new BitSet(2);
        left.set(1);
        left.set(0); //3
        //Set variables
        Image arrowImage;
        float arrowWidth;
        float arrowHeight;
        Cell gameTableCell = rootTable.getCell(gameTable);
        Vector2 pos;
        final Tile newTile;
        //Top
        if (arrow.equals(new BitSet(2))) {
            arrowImage = new Image(atlas.findRegion("UpArrow"));
            arrowWidth = gameTable.getWidth() / 5f;
            arrowHeight = gameTable.getHeight() / 8f;
            pos = new Vector2((gameTable.getWidth() - arrowWidth) / 2f,
                    gameTable.getHeight() - gameTableCell.getPadTop() - arrowHeight);
            newTile = tile.tileAbove();
        }
        //Right
        else if (arrow.equals(right)) {
            arrowImage = new Image(atlas.findRegion("RightArrow"));
            arrowWidth = gameTable.getHeight() / 8f;
            arrowHeight = gameTable.getWidth() / 5f;
            pos = new Vector2((gameTable.getWidth() - arrowWidth),
                    (gameTable.getHeight() - arrowHeight) / 2f - gameTableCell.getPadBottom());
            newTile = tile.tileRight();
        }
        //Bottom
        else if (arrow.equals(bottom)) {
            arrowImage = new Image(atlas.findRegion("DownArrow"));
            arrowWidth = gameTable.getWidth() / 5f;
            arrowHeight = gameTable.getHeight() / 8f;
            pos = new Vector2((gameTable.getWidth() - arrowWidth) / 2f,
                    0 - gameTableCell.getPadBottom());
            newTile = tile.tileBelow();
        }
        //Left
        else if (arrow.equals(left)) {
            arrowImage = new Image(atlas.findRegion("LeftArrow"));
            arrowWidth = gameTable.getHeight() / 8f;
            arrowHeight = gameTable.getWidth() / 5f;
            pos = new Vector2(0,
                    (gameTable.getHeight()  - arrowHeight) / 2f - gameTableCell.getPadBottom());
            newTile = tile.tileLeft();
        }
        else throw new IllegalArgumentException("Invalid BitSet Argument");


        arrowImage.setSize(arrowWidth, arrowHeight);
        arrowImage.setPosition(gameTable.localToStageCoordinates(pos).x, gameTable.localToStageCoordinates(pos).y);
        arrowImage.addListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                game.setCurrentTile(newTile);
                game.loadGameScreen();
            }
        });
        arrowImage.toFront();
        stage.addActor(arrowImage);
    }

    private void addTrees() {
        //For each corner starting in topleft and moving clockwise
        for (int corner = 0; corner < 4; corner++) {
            List<Vector2> sortedTrees = new ArrayList<>(tile.getTrees().treeCoordinates(corner));
            Collections.sort(sortedTrees, new Comparator<Vector2>() {
                @Override
                public int compare(Vector2 o1, Vector2 o2) {
                    if (o1.y == o2.y)
                        return 0;
                    if (o1.y > o2.y)
                        return -1;
                    else
                        return 1;
                }
            });
            for(Vector2 treePos : sortedTrees) {
                float treeWidth = GAME_TABLE_WIDTH / 7f;
                float treeHeight = GAME_TABLE_HEIGHT / 7f * 3f;
                Vector2 absolutePos;
                if (corner == 0)
                    absolutePos = new Vector2(treePos.x,
                            treePos.y + GAME_TABLE_HEIGHT - Tile.TREE_CORNER_SIDE_SIZE - treeHeight);
                else if (corner == 1)
                    absolutePos = new Vector2(treePos.x + GAME_TABLE_WIDTH - Tile.TREE_CORNER_SIDE_SIZE - treeWidth,
                            treePos.y + GAME_TABLE_HEIGHT - Tile.TREE_CORNER_SIDE_SIZE - treeHeight);
                else if (corner == 2)
                    absolutePos = new Vector2(treePos.x + GAME_TABLE_WIDTH - Tile.TREE_CORNER_SIDE_SIZE - treeWidth,
                            treePos.y);
                else absolutePos = new Vector2(treePos.x, treePos.y);

                Image treeImage = new Image(atlas.findRegion(tile.getRegion().getTerrain().toString() + "Tree"));
                treeImage.setPosition(gameTable.localToStageCoordinates(absolutePos).x,
                        gameTable.localToStageCoordinates(absolutePos).y - ((IslandGame.GAME_HEIGHT - GAME_TABLE_HEIGHT)/2f));
                treeImage.setSize(treeWidth, treeHeight);
                stage.addActor(treeImage);
            }
        }
    }

    @Override
    public Stage getStage() {
        return stage;
    }
}
