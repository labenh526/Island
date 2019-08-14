package com.laben.islands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.HashMap;
import java.util.Map;

public class GameScreen implements Screen {

    private Stage stage;
    private Map<String, Class> assets;
    private Table gameTable;
    private IslandGame game;
    private TextureAtlas atlas;
    private Tile tile;

    public GameScreen(final IslandGame game, Tile tile) {
        this.game = game;
        this.tile = tile;
        stage = new Stage(new FitViewport(IslandGame.getGameWidth(), IslandGame.getGameHeight()));

        //Load assets
        assets = new HashMap<>();
        assets.put("GameScreenTextures.atlas", TextureAtlas.class);
        IslandGame.loadAllAssets(game.getManager(), assets);
        game.getManager().finishLoading();
        atlas = game.getManager().get("GameScreenTextures.atlas");

        //Create root table
        Table rootTable = new Table();
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
    }

    public Table initializeGameTable() {
        Table table = new Table();
        table.setBackground(new TextureRegionDrawable(atlas.findRegion(tile.getRegion().getTerrain().getBackgroundImage())));
        return table;
    }


    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        //Graphics:
        Gdx.gl.glClearColor((float)(204/255.0), 0, (float)(102/255.0), 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {

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
}
