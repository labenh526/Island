package com.laben.islands.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.laben.islands.Island;
import com.laben.islands.IslandGame;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsoleScreen extends AbstractScreen{
    private Stage stage;
    private IslandGame game;
    private TextureAtlas atlas;
    private Map<String, Class> assets;
    private ScrollPane pane;
    private BitmapFont consoleFont;
    private Table consoleTable;
    private Table rootTable;
    private TextField inputField;

    //Console does not use localization for strings
    public ConsoleScreen(IslandGame game) {
        this.game = game;
        //Load necessary assets
        assets = new HashMap<>();
        assets.put("Fonts/ConsoleFont.fnt", BitmapFont.class);
        assets.put("GeneralTextures.atlas", TextureAtlas.class);
        IslandGame.loadAllAssets(game.getManager(), assets);
        game.getManager().finishLoading();

        atlas = game.getManager().get("GeneralTextures.atlas");
        stage = new Stage(new FitViewport(IslandGame.getGameWidth(), IslandGame.getGameHeight()));
        setInputProcessor(game, stage);

        consoleFont = game.getManager().get("Fonts/ConsoleFont.fnt");

        rootTable = new Table();
        rootTable.setRound(false);
        stage.addActor(rootTable);
        rootTable.setFillParent(true);

        //Label for console
        Label.LabelStyle consoleTitleLabelStyle = new Label.LabelStyle();
        consoleTitleLabelStyle.font = consoleFont;
        consoleTitleLabelStyle.fontColor = Color.WHITE;
        Label consoleTitleLabel = new Label("Developer Console", consoleTitleLabelStyle);
        float labelWidth = (float)IslandGame.GAME_WIDTH / 3f;
        float labelHeight = .15f * (float)IslandGame.GAME_HEIGHT;
        consoleTitleLabel.setSize(labelWidth, labelHeight);
        consoleTitleLabel.setAlignment(Align.topLeft);

        //Scrolling console
        consoleTable = new Table();
        pane = new ScrollPane(consoleTable);
        pane.setScrollingDisabled(false, true);
        consoleTable.align(Align.bottom);
        //Input field
        TextField.TextFieldStyle inputFieldStyle = new TextField.TextFieldStyle(consoleFont, Color.WHITE,
                new TextureRegionDrawable(atlas.findRegion("Cursor")), new TextureRegionDrawable(atlas.findRegion("Black")),
                new TextureRegionDrawable(atlas.findRegion("Grey")));
        inputField = new TextField("", inputFieldStyle);
        inputField.setWidth(.95f * IslandGame.GAME_WIDTH);
        inputField.setHeight(.07f * (float)IslandGame.GAME_HEIGHT);


        //Construct root table
        rootTable.row().height(.05f * (float)IslandGame.GAME_HEIGHT);
        rootTable.add(consoleTitleLabel).width(labelWidth).center();
        rootTable.row().height(.8f * (float)IslandGame.GAME_HEIGHT).width(.97f * IslandGame.GAME_WIDTH);
        rootTable.add(pane).padLeft(.05f * (float)IslandGame.GAME_WIDTH).expand().fill();
        rootTable.row().height(inputField.getHeight());
        rootTable.add(inputField).expand().width(inputField.getWidth()).padBottom(.03f);
        consoleTable.validate();
        pane.validate();
        rootTable.validate();

        log("'help' for list of commands, 'exit' to exit the console");
    }

    //Displays input message in console - defaults to white color
    private void log(String message) {
        log(message, Color.WHITE);
    }

    private void log(String message, Color textColor) {
        consoleTable.row().expandX();
        Label.LabelStyle messageLabelStyle = new Label.LabelStyle();
        messageLabelStyle.font = consoleFont;
        messageLabelStyle.fontColor = textColor;
        Label messageLabel = new Label(message, messageLabelStyle);
        messageLabel.setSize(consoleTable.getWidth(), consoleTable.getHeight() / 15f);
        messageLabel.setAlignment(Align.left);
        messageLabel.setWrap(true);
        messageLabel.setFontScale(.85f);
        consoleTable.add(messageLabel).width(Gdx.graphics.getWidth()).bottom();
        consoleTable.validate();
        pane.validate();
    }

    private void enterCurrentCommand() {
        log(">" + inputField.getText());
        inputField.setText("");
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        //Logic
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            enterCurrentCommand();
        }

        //Graphics
        Gdx.gl.glClearColor(0, 0, 0, 1); //black bg
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
}
