package com.laben.islands;

import com.badlogic.gdx.Gdx;
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

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScreen implements Screen {

    private Stage stage;
    private Map<String, Class> assets;
    private Table gameTable;
    private IslandGame game;
    private TextureAtlas atlas;
    private final Tile tile;
    private Label staminaLabel;
    private float maxStaminaWidth;
    private Image staminaBar;
    private Table rootTable;

    public GameScreen(final IslandGame game, final Tile tile) {
        this.game = game;
        this.tile = tile;
        stage = new Stage(new FitViewport(IslandGame.getGameWidth(), IslandGame.getGameHeight()));
        Gdx.input.setInputProcessor(stage);

        //Load assets
        assets = new HashMap<>();
        assets.put("GameScreenTextures.atlas", TextureAtlas.class);
        assets.put("Fonts/StaminaTextFont.fnt", BitmapFont.class);
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
        rootTable.row().height(11f / 12f * (float)IslandGame.getGameHeight());
        float leftPadding  = (.05f * (float)IslandGame.GAME_WIDTH);
        float topBottomPadding = 2.5f / 60.0f * (float)IslandGame.GAME_HEIGHT;
        float gameTableWidth = .55f * (float)IslandGame.GAME_WIDTH;
        rootTable.add(gameTable).expand().width(gameTableWidth).padLeft(leftPadding).padTop(topBottomPadding)
                .padBottom(topBottomPadding).left();

        rootTable.validate();
        gameTable.validate();

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

        //Create stamina background
        staminaLabel.validate();
        Image staminaBackground = new Image(atlas.findRegion("StaminaBackground"));
        staminaBackground.setSize(.35f * (float)IslandGame.getGameWidth(),
                .06f * (float)IslandGame.getGameHeight());
        staminaBackground.setPosition(staminaLabel.getX(),
                staminaLabel.getY() - staminaBackground.getHeight() + .04f * (float)IslandGame.getGameHeight());
        stage.addActor(staminaBackground);

        //Create stamina bar
        staminaBackground.validate();
        staminaBar = new Image(atlas.findRegion("StaminaBar"));
        staminaBar.setHeight(.8f * staminaBackground.getHeight());
        staminaBar.setPosition(staminaBackground.getX() + .05f * staminaBackground.getWidth(),
                staminaBackground.getY() + .1f * staminaBackground.getHeight());
        maxStaminaWidth = staminaBackground.getWidth() * .9f;
        stage.addActor(staminaBar);

        //Create grey background to stamina bar
        staminaBar.validate();
        Image greyStaminaBackground = new Image(atlas.findRegion("GreyStaminaBackground"));
        greyStaminaBackground.setPosition(staminaBar.getX(), staminaBar.getY());
        greyStaminaBackground.setSize(maxStaminaWidth, staminaBar.getHeight());
        stage.addActor(greyStaminaBackground);

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

    }


    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        //Graphics:
        Gdx.gl.glClearColor((float)(204/255.0), 0, (float)(102/255.0), 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //Set Stamina text
        staminaLabel.setText(staminaString());
        setStaminaBarWidth();
        stage.draw();

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
        IslandGame.unloadAllAssets(game.getManager(), assets.keySet());
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
        table.setBackground(new TextureRegionDrawable(atlas.findRegion(tile.getRegion().getTerrain().getBackgroundImage())));
        return table;
    }

    private String staminaString() {
        StringBuilder staminaText = new StringBuilder("Stamina (");
        staminaText.append(game.getPlayer().getStamina());
        staminaText.append("/");
        staminaText.append(game.getPlayer().getMaxStamina());
        staminaText.append(")");
        return staminaText.toString();
    }

    private void setStaminaBarWidth() {
        staminaBar.setWidth((float)game.getPlayer().getStamina() /  (float)game.getPlayer().getMaxStamina()  *
                maxStaminaWidth);
        staminaBar.toFront();
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

}
