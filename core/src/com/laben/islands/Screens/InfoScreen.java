package com.laben.islands.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.laben.islands.IslandGame;

import java.util.HashMap;
import java.util.Map;

public abstract class InfoScreen implements Screen {

    private IslandGame game;
    private Stage stage;
    private Map<String, Class> assets;
    private TextureAtlas atlas;

    private Image infoBoxBg; //used for relative positioning in child classes
    private I18NBundle infoBundle;

    public enum ScreenType {PLAYER, INVENTORY, CLUE}

    public InfoScreen(final IslandGame game, ScreenType screenType, Map<String, Class> assetMap) {
        this.game = game;
        stage = new Stage(new FitViewport(IslandGame.getGameWidth(), IslandGame.getGameHeight()));
        Gdx.input.setInputProcessor(stage);


        assets = new HashMap<>(assetMap);
        assets.put("InfoScreenTextures.atlas", TextureAtlas.class);
        assets.put("Fonts/InfoViewTitle.fnt", BitmapFont.class);
        assets.put("i18n/InfoViewBundle", I18NBundle.class);
        IslandGame.loadAllAssets(game.getManager(), assets);
        game.getManager().finishLoading();
        atlas = game.getManager().get("InfoScreenTextures.atlas");
        infoBundle = game.getManager().get("i18n/InfoViewBundle");

        //Positional data calculations
        float tabWidth = IslandGame.GAME_WIDTH * .1f;
        float tabHeight = tabWidth * 16f / 25f;
        float initTabY = IslandGame.GAME_HEIGHT * .99f - tabHeight;
        float infoBoxWidth = IslandGame.getGameWidth() * .975f;
        float infoBoxHeight = infoBoxWidth * 107f / 200f;
        float infoBoxX = (IslandGame.getGameWidth() - infoBoxWidth) / 2f;
        float infoBoxY = initTabY - infoBoxHeight + infoBoxHeight * .009f;
        float initTabX = infoBoxX;

        //Add Inventory tab button
        Image inventoryTab = new Image(atlas.findRegion("ItemTab"));
        inventoryTab.setSize(tabWidth, tabHeight);
        inventoryTab.setPosition(initTabX, initTabY);
        stage.addActor(inventoryTab);
        //Add Player tab button
        Image playerTab = new Image(atlas.findRegion("PlayerTab"));
        playerTab.setSize(tabWidth, tabHeight);
        playerTab.setPosition(initTabX + tabWidth, initTabY);
        stage.addActor(playerTab);
        //Add Clue tab button
        Image clueTab = new Image(atlas.findRegion("ClueTab"));
        clueTab.setSize(tabWidth, tabHeight);
        clueTab.setPosition(initTabX + tabWidth * 2f, initTabY);
        stage.addActor(clueTab);

        //Add main information box
        String bgImg = "ClueBg";
        if (screenType.equals(ScreenType.INVENTORY))
            bgImg = "ItemBg";
        else if (screenType.equals(ScreenType.PLAYER))
            bgImg = "PlayerBg";
        infoBoxBg = new Image(atlas.findRegion(bgImg));
        infoBoxBg.setPosition(infoBoxX, infoBoxY);
        infoBoxBg.setSize(infoBoxWidth, infoBoxHeight);
        stage.addActor(infoBoxBg);

        Image infoBoxOutline = new Image(atlas.findRegion("InfoBoxOutline"));
        infoBoxOutline.setSize(infoBoxWidth, infoBoxHeight);
        infoBoxOutline.setPosition(infoBoxX, infoBoxY);
        stage.addActor(infoBoxOutline);

        //Choose which tab is currently being viewed
        if (screenType.equals(ScreenType.INVENTORY))
            inventoryTab.toFront();
        else if (screenType.equals(ScreenType.CLUE))
            clueTab.toFront();
        else if (screenType.equals(ScreenType.PLAYER))
            playerTab.toFront();

        //Add title of that inventory
        String title = infoBundle.get("Inventory");
        if (screenType.equals(ScreenType.PLAYER))
            title = "bleh";
        else if (screenType.equals(ScreenType.CLUE))
            title = "Clues";
        Label.LabelStyle titleLabelStyle = new Label.LabelStyle();
        titleLabelStyle.font = game.getManager().get("Fonts/InfoViewTitle.fnt");
        titleLabelStyle.fontColor = Color.BLACK;
        Label titleLabel = new Label(title, titleLabelStyle);
        titleLabel.setAlignment(Align.topLeft);
        titleLabel.setSize(.4f * IslandGame.getGameWidth(), .5f / 6f * IslandGame.getGameHeight());
        stage.addActor(titleLabel);
        float titleLabelXPos = infoBoxX + infoBoxWidth * .02f;
        float titleLabelYPos = infoBoxY + infoBoxHeight * .9f;
        titleLabel.setPosition(titleLabelXPos, titleLabelYPos);

        //Add back button
        Image back = new Image(atlas.findRegion("BackButton"));
        back.setSize(tabWidth * .9f, tabHeight * .9f);
        back.setPosition(initTabX+ tabWidth * 3.2f, initTabY + tabHeight *.09f);
        stage.addActor(back);
        back.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                dispose();
                getGame().setScreen(new GameScreen(getGame(), getGame().getCurrentTile()));
            }
        });

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


    Map<String, Class> getAssets() {
        return assets;
    }

    Image getInfoBoxBg() {
        return infoBoxBg;
    }

    Stage getStage() {
        return stage;
    }

    IslandGame getGame() {
        return game;
    }

    I18NBundle getInfoBundle() {
        return infoBundle;
    }
}
