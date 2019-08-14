package com.laben.islands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
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
    private Label staminaLabel;
    private float maxStaminaWidth;
    private Image staminaBar;

    public GameScreen(final IslandGame game, Tile tile) {
        this.game = game;
        this.tile = tile;
        stage = new Stage(new FitViewport(IslandGame.getGameWidth(), IslandGame.getGameHeight()));

        //Load assets
        assets = new HashMap<>();
        assets.put("GameScreenTextures.atlas", TextureAtlas.class);
        assets.put("Fonts/StaminaTextFont.fnt", BitmapFont.class);
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
        //Set Stamina text
        staminaLabel.setText(staminaString());
        setStaminaBarWidth();
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
}
