package com.laben.islands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** This is the screen that shows when viewing a map
 * **/
public class MapViewScreen implements Screen {

    private final IslandGame game;

    private Stage stage; //The stage which manages the entire scene
    private Table rootTable;
    private Table mapTable;
    private String currentSelectedRegion = null;
    private TextureAtlas atlas;
    private Map<String, Class> assets;
    private Label selectedRegionLabel;

    //Pass in the game object
    public MapViewScreen(final IslandGame game) {
        //Load necessary assets to manager
        assets = new HashMap<>();
        assets.put(Terrain.MAP_VIEW_ATLAS_PATH, TextureAtlas.class);
        assets.put("Fonts/MapViewRegionName.fnt", BitmapFont.class);
        IslandGame.loadAllAssets(game.getManager(), assets);
        game.getManager().finishLoading();
        atlas = game.getManager().get(Terrain.MAP_VIEW_ATLAS_PATH, TextureAtlas.class);

        this.game = game;
        stage = new Stage(new FitViewport(IslandGame.getGameWidth(), IslandGame.getGameHeight()));
        //Add map background
        Image mapBackground = new Image(atlas.findRegion("backomap"));
        mapBackground.setPosition((float)(0.07 * (float)IslandGame.GAME_WIDTH), (float)(.2/6.0 * (float)IslandGame.GAME_HEIGHT));
        mapBackground.setSize((float)(.56* (float)IslandGame.GAME_WIDTH), (float)(.56* (float)IslandGame.GAME_WIDTH));
        stage.addActor(mapBackground);

        //Create root table
        rootTable = new Table();
        rootTable.setRound(false);
        stage.addActor(rootTable);
        rootTable.setFillParent(true);

        //Create table map
        mapTable = initializedMapTable();

        //Create label that states the currently selected reigon
        Label.LabelStyle regionLabelStyle = new Label.LabelStyle();
        regionLabelStyle.font = game.getManager().get("Fonts/MapViewRegionName.fnt");
        regionLabelStyle.fontColor = Color.BLACK;
        selectedRegionLabel = new Label("Hello\nWorld", regionLabelStyle);
        float labelWidth = (float)(.27 * (float)IslandGame.GAME_WIDTH);
        float labelHeight = (float) (1.0 / 3.0 * (float)IslandGame.GAME_HEIGHT);
        selectedRegionLabel.setSize(labelWidth, labelHeight);
        selectedRegionLabel.setAlignment(Align.topLeft);

        /* format root table */
        //Add mapTable
        float leftPadding  = (float)(.1 * (float)IslandGame.GAME_WIDTH);
        float topBottomPadding = (float)(5.0 / 60.0 * (float)IslandGame.GAME_HEIGHT);
        rootTable.row().height((float)(5.0 / 6.0 * (float)IslandGame.GAME_HEIGHT));
        rootTable.add(mapTable).expand().width((int)(.5 * (double)IslandGame.GAME_WIDTH)).left().
                        padLeft(leftPadding).padTop(topBottomPadding).padBottom(topBottomPadding);
        //Add region label
        rootTable.add(selectedRegionLabel).padRight(topBottomPadding).width(labelWidth);




    }

    public IslandGame getGame() {
        return game;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        //Scripts:
        executeScripts();
        //Graphics:
        renderGraphics();
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
    }

    private Table initializedMapTable() {
        Island.IterableRegionMap regionMap = getGame().getCurrentIsland().getIterableRegionMap();
        Table table = new Table();
        table.setRound(false);
        //Get image files for each terrain type
        Map<Terrain, TextureAtlas.AtlasRegion> terrainAtlasRegionMap = new HashMap<>();
        for (Terrain terrain : Terrain.TERRAIN_SET) {
            terrainAtlasRegionMap.put(terrain, atlas.findRegion(terrain.toString()));
        }
        //Iterate through each row
        int rowNum = 0;
        int colNum = 0;
        for (List<Region> row : regionMap) {
            //Iterate through each tile in row's region
            for (Region region : row) {
                table.add(new Image(terrainAtlasRegionMap.get(region.getTerrain()))).uniform().expand().fill();
                colNum++;
            }
            table.row();
            rowNum++;
        }
        return table;
    }

    //All elements of the screen to be graphically rendered
    private void renderGraphics() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();

    }

    //All non graphical elements to be rendered - such as changes in game state
    private void executeScripts() {

    }
}
