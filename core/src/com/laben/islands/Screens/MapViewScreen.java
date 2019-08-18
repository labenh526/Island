package com.laben.islands.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.laben.islands.Island;
import com.laben.islands.IslandGame;
import com.laben.islands.Region;
import com.laben.islands.Terrain;

import java.util.*;

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
    private Set<Image> currentShowingOutlines;

    //Pass in the game object
    public MapViewScreen(final IslandGame game) {
        currentShowingOutlines = new HashSet<>();

        //Load necessary assets to manager
        assets = new HashMap<>();
        assets.put(Terrain.MAP_VIEW_ATLAS_PATH, TextureAtlas.class);
        assets.put("Fonts/MapViewRegionName.fnt", BitmapFont.class);
        IslandGame.loadAllAssets(game.getManager(), assets);
        game.getManager().finishLoading();
        atlas = game.getManager().get(Terrain.MAP_VIEW_ATLAS_PATH, TextureAtlas.class);

        this.game = game;
        stage = new Stage(new FitViewport(IslandGame.getGameWidth(), IslandGame.getGameHeight()));
        Gdx.input.setInputProcessor(stage);
        //Add map background
        Image mapBackground = new Image(atlas.findRegion("backomap"));
        mapBackground.setPosition((0.02f * (float)IslandGame.GAME_WIDTH), (float)(.2/6.0 * (float)IslandGame.GAME_HEIGHT));
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
        selectedRegionLabel = new Label("", regionLabelStyle);
        float labelWidth = (float)(.38 * (float)IslandGame.GAME_WIDTH);
        float labelHeight = (float) (1.0 / 3.0 * (float)IslandGame.GAME_HEIGHT);
        selectedRegionLabel.setSize(labelWidth, labelHeight);
        selectedRegionLabel.setAlignment(Align.topLeft);

        //Create back arrow image
        Image backArrow = new Image(atlas.findRegion("backarrow"));
        backArrow.setSize(.15f * (float)IslandGame.getGameWidth(), .15f * (float)IslandGame.getGameWidth());
        backArrow.setPosition(.7f * (float)IslandGame.getGameWidth(), 1f / 6f * (float)IslandGame.getGameHeight());
        backArrow.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                game.loadGameScreen();
            }
        });
        stage.addActor(backArrow);

        /* format root table */
        //Add mapTable
        float leftPadding  = (.05f * (float)IslandGame.GAME_WIDTH);
        float topBottomPadding = (float)(5.0 / 60.0 * (float)IslandGame.GAME_HEIGHT);
        rootTable.row().height((float)(5.0 / 6.0 * (float)IslandGame.GAME_HEIGHT));
        rootTable.add(mapTable).expand().width((float)(.5 * (double)IslandGame.GAME_WIDTH)).
                        padLeft(leftPadding).padTop(topBottomPadding).padBottom(topBottomPadding).left();
        //Add region label
        rootTable.add(selectedRegionLabel).padRight(topBottomPadding / 3.0f).width(labelWidth).left();

        rootTable.validate();
        mapTable.validate();

        //Add current location marker
        Image redDot = new Image(atlas.findRegion("reddot"));
        redDot.setSize(mapTable.getColumnWidth(0), mapTable.getRowHeight(0));
        float xStartPos = mapTable.localToStageCoordinates(new Vector2(0, 0)).x;
        float yStartPos = mapTable.localToStageCoordinates(new Vector2(0, 0)).y;
        float xMod = mapTable.getWidth() / (float)mapTable.getColumns() * (float)getGame().getCurrentTile()
                .getCoordinates().x;
        float yMod = mapTable.getHeight() / (float)mapTable.getRows() *
                ((float)mapTable.getRows() - 1 - (float)getGame().getCurrentTile().getCoordinates().y);
        redDot.setPosition(xStartPos + xMod, yStartPos + yMod);
        addInputListenerToMapTile(redDot, getGame().getCurrentTile().getRegion());
        stage.addActor(redDot);
        redDot.toFront();
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
        atlas.dispose();
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
                Image image = new Image(terrainAtlasRegionMap.get(region.getTerrain()));
                addInputListenerToMapTile(image, region);
                table.add(image).uniform().expand().fill();
                colNum++;
            }
            table.row();
            rowNum++;
        }
        return table;
    }

    //Adds image actors which draw an outline over the currently selected region
    private void addSelectedRegionOutline() {
        //Remove all previous
        for (Image image: currentShowingOutlines)
            image.remove();
        currentShowingOutlines.clear();
        int[][] numMap = getGame().getCurrentIsland().getIterableRegionMap().getNumericalMap();
        Map<Integer, Region> integerRegionConversionChart = getGame().getCurrentIsland().getIterableRegionMap().getRegionIntegerConversionChart();
        //Iterate through every tile in map
        for (int x = 0; x < numMap.length; x++) {
            for (int y = 0; y < numMap[0].length; y++) {
                //If this is the currently selected region, then draw border lines
                if (currentSelectedRegion.equals(integerRegionConversionChart.get(numMap[x][y]).toString())) {
                    addTileOutline(new GridPoint2(x, y));
                }
            }
        }
    }

    //Adds outline to a specific tile on the map
    private void addTileOutline(GridPoint2 tileGridLocation) {
        int x = tileGridLocation.x;
        int y = tileGridLocation.y;
        int[][] numMap = getGame().getCurrentIsland().getIterableRegionMap().getNumericalMap();
        int value = numMap[x][y];
        //Determine which sides of the tile need the outline
        BitSet outlineLocations = new BitSet(4); //outline locations represented via binary: top, right, bottom, left
        //If right needs outline:
        if (x == numMap.length - 1 || numMap[x + 1][y] != value) {
            outlineLocations.set(1);
            addVerticalLine(tileGridLocation, false, outlineLocations);
        }
        //If left needs outline:
        if (x == 0 || numMap[x - 1][y] != value) {
            outlineLocations.set(3);
            addVerticalLine(tileGridLocation, true, outlineLocations);
        }
        //If top needs outline:
        if (y == 0 || numMap[x][y - 1] != value) {
            outlineLocations.set(0);
            addHorizontalLine(tileGridLocation, true, outlineLocations);
        }
        //If bottom needs outline:
        if (y == numMap[0].length - 1 || numMap[x][y + 1] != value) {
            outlineLocations.set(2);
            addHorizontalLine(tileGridLocation, false, outlineLocations);
        }
    }

    //Adds a vertical line image at the given point on the graph. isLeft determines which side to add it to
    private void addVerticalLine(GridPoint2 pointOnGraph, boolean isLeft, BitSet outlineLocations) {
        Image line = new Image(atlas.findRegion("RegionOutlineVertical"));
        currentShowingOutlines.add(line);
        stage.addActor(line);
        line.toFront();
        line.setSize(mapTable.getRowHeight(0) * line.getWidth() / line.getHeight(),
                mapTable.getRowHeight(0));
        float startingXPos = mapTable.localToStageCoordinates(new Vector2(0, 0)).x;
        float startingYPos = mapTable.localToStageCoordinates(new Vector2(0, 0)).y;
        //x mod = width of each cell * (graph point x (+ 1 if on right), then if on right subtract width of line
        float xMod = mapTable.getWidth() / (float)mapTable.getColumns() * ((float)pointOnGraph.x + (isLeft ?  0f : 1f));
        if (!isLeft)
            xMod -= line.getWidth();
        //y mod calculation is mostly the same, except it accounts for the libgdx (0,0) in bottom left and fixed spot
        float yMod = mapTable.getHeight() / (float)mapTable.getRows() *
                ((float)mapTable.getRows() - 1 - (float)pointOnGraph.y);

        line.setPosition(startingXPos + xMod, startingYPos + yMod);
    }

    //Adds a horizontal line image at the given point on the graph. isTop determines which side to add it to
    private void addHorizontalLine(GridPoint2 pointOnGraph, boolean isTop, BitSet outlineLocations) {
        Image line = new Image(atlas.findRegion("RegionOutlineHorizontal"));
        currentShowingOutlines.add(line);
        stage.addActor(line);
        line.toFront();
        line.setSize(mapTable.getColumnWidth(0),
                mapTable.getColumnWidth(0) * line.getHeight() / line.getWidth());
        float startingXPos = mapTable.localToStageCoordinates(new Vector2(0, 0)).x;
        float startingYPos = mapTable.localToStageCoordinates(new Vector2(0, 0)).y;
        //calculate xmod
        float xMod = mapTable.getWidth() / (float)mapTable.getColumns() * (float)pointOnGraph.x;
        //calculate ymod
        float yMod = mapTable.getHeight() / (float)mapTable.getRows() *
                ((float)mapTable.getRows() - 1 - (float)pointOnGraph.y + (isTop ? 1 : 0));
        //Adjust size to remove gaps in corners of outlines:
        //If no border to right
        if (!outlineLocations.get(1))
            line.setSize(line.getWidth() + line.getHeight(), line.getHeight());
        //If no border to left
        if (!outlineLocations.get(3)) {
            line.setSize(line.getWidth() + line.getHeight(), line.getHeight());
            xMod -= line.getHeight();
        }
        if (isTop)
            yMod -= line.getHeight();

        line.setPosition(startingXPos + xMod, startingYPos + yMod);
    }

    //All elements of the screen to be graphically rendered
    private void renderGraphics() {
        Gdx.gl.glClearColor((float)(204/255.0), 0, (float)(102/255.0), 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    //All non graphical elements to be rendered - such as changes in game state
    private void executeScripts() {
    }

    public String getCurrentSelectedRegion() {
        return currentSelectedRegion;
    }

    public void setCurrentSelectedRegion(String currentSelectedRegion) {
        this.currentSelectedRegion = currentSelectedRegion;
        //Change label's text
        String[] twoPartsOfName = currentSelectedRegion.split("\\s+");
        StringBuilder splitUpName = new StringBuilder(twoPartsOfName[0]);
        splitUpName.append("\n");
        splitUpName.append(twoPartsOfName[1]);
        getSelectedRegionLabel().setText(splitUpName);
        addSelectedRegionOutline();
    }

    public Label getSelectedRegionLabel() {
        return selectedRegionLabel;
    }

    private void addInputListenerToMapTile(Image mapTile, final Region region) {

        mapTile.addListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                setCurrentSelectedRegion(region.toString());
            }
        });
    }
}
