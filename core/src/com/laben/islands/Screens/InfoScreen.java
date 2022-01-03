package com.laben.islands.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

public abstract class InfoScreen extends AbstractScreen {

    private IslandGame game;
    private Stage stage;
    private Map<String, Class> assets;
    private TextureAtlas atlas;

    private Image infoBoxBg; //used for relative positioning in child classes
    private I18NBundle infoBundle;
    private IslandGame.StaminaBar staminaBar;
    private Label staminaText;
    private int currentStamina;
    private int currentMaxStamina;

    public enum ScreenType {PLAYER, INVENTORY, CLUE}

    public InfoScreen(final IslandGame game, final ScreenType screenType, Map<String, Class> assetMap) {
        this.game = game;
        stage = new Stage(new FitViewport(IslandGame.getGameWidth(), IslandGame.getGameHeight()));
        setInputProcessor(game, stage);


        assets = new HashMap<>(assetMap);
        assets.put("InfoScreenTextures.atlas", TextureAtlas.class);
        assets.put("Fonts/InfoViewTitle.fnt", BitmapFont.class);
        assets.put("i18n/InfoViewBundle", I18NBundle.class);
        assets.put("Fonts/StaminaTextFont.fnt", BitmapFont.class);
        IslandGame.loadAllAssets(game.getManager(), assets);
        game.getManager().finishLoading();
        atlas = game.getManager().get("InfoScreenTextures.atlas");
        infoBundle = game.getManager().get("i18n/InfoViewBundle");

        currentMaxStamina = 0;
        currentStamina = 0;

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
        if (screenType != ScreenType.INVENTORY) {
            inventoryTab.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    dispose();
                    getGame().setScreen(new InventoryScreen(game));
                }
            });
        }
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
        if (screenType != ScreenType.CLUE) {
            clueTab.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    dispose();
                    getGame().setScreen(new ClueScreen(game));
                }
            });
        }


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
                if (Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isKeyPressed(Input.Keys.E) &&
                        Gdx.input.isKeyPressed(Input.Keys.V)) {
                    getGame().setDevMode(!getGame().isDevMode());
                } else {
                    dispose();
                    getGame().setScreen(new GameScreen(getGame(), getGame().getCurrentTile()));
                }
            }
        });

        //Add Stamina bar
        back.validate();
        float staminaWidth = IslandGame.getGameWidth() * .25f;
        float staminaHeight = back.getHeight() / 2f;
        float staminaXPos = back.getX() + back.getWidth() * 1.3f;
        float staminaYPos = back.getY();
        staminaBar = IslandGame.createStaminaBar(stage, atlas.findRegion("StaminaBackground"),
                atlas.findRegion("GreyStaminaBackground"), atlas.findRegion("StaminaBar"),
                staminaXPos, staminaYPos, staminaWidth, staminaHeight);
        //Add stamina text
        Label.LabelStyle staminaStyle = new Label.LabelStyle();
        staminaStyle.fontColor = Color.BLACK;
        staminaStyle.font = game.getManager().get("Fonts/StaminaTextFont.fnt");
        staminaText = new Label("", staminaStyle);
        float staminaTextX = staminaXPos;
        float staminaTextY = staminaYPos + staminaHeight;
        float staminaTextWidth = staminaWidth;
        float staminaTextHeight = back.getHeight() - staminaHeight;
        staminaText.setPosition(staminaTextX, staminaTextY);
        staminaText.setSize(staminaTextWidth, staminaTextHeight);
        staminaText.setAlignment(Align.topLeft);
        stage.addActor(staminaText);

    }

    @Override
    public void show() {
        setInputProcessor(game, stage);
    }

    @Override
    public void render(float delta) {
        //Graphics:
        Gdx.gl.glClearColor((float)(204/255.0), 0, (float)(102/255.0), 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        staminaBar.updateStaminaInstantaneous(game);
        updateStaminaText();
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

    @Override
    public Stage getStage() {
        return stage;
    }

    IslandGame getGame() {
        return game;
    }

    I18NBundle getInfoBundle() {
        return infoBundle;
    }

    private void updateStaminaText() {
        if (currentStamina != game.getPlayer().getStamina() || currentMaxStamina != game.getPlayer().getMaxStamina()) {
            StringBuilder sb = new StringBuilder(game.getGeneralBundle().get("Stamina"));
            sb.append(" ");
            sb.append(game.getPlayer().getStamina());
            sb.append("/");
            sb.append(game.getPlayer().getMaxStamina());
            staminaText.setText(sb.toString());
        }
    }
}
