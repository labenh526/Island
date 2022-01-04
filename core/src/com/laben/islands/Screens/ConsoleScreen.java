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
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.laben.islands.Island;
import com.laben.islands.IslandGame;
import com.laben.islands.Player;
import com.laben.islands.items.Item;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.BiConsumer;

//Console does not use localization for strings
public class ConsoleScreen extends AbstractScreen{
    private Stage stage;
    private IslandGame game;
    private Map<String, Class> assets;
    private ScrollPane pane;
    private BitmapFont consoleFont;
    private Table consoleTable;
    private Table rootTable;
    private TextField inputField;
    private Constructor<? extends InfoScreen> prevScreen;
    private I18NBundle itemBundle;

    private static final Map<String, Command> commands;

    public ConsoleScreen(IslandGame game, Class<? extends InfoScreen> prevScreen) {
        this.game = game;

        //Load necessary assets
        assets = new HashMap<>();
        assets.put("Fonts/ConsoleFont.fnt", BitmapFont.class);
        assets.put("i18n/ItemBundle", I18NBundle.class);
        IslandGame.loadAllAssets(game.getManager(), assets);
        game.getManager().finishLoading();

        itemBundle = game.getManager().get("i18n/ItemBundle");

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
                new TextureRegionDrawable(game.getGeneralAtlas().findRegion("Cursor")), new TextureRegionDrawable(game.getGeneralAtlas().findRegion("Black")),
                new TextureRegionDrawable(game.getGeneralAtlas().findRegion("Grey")));
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

        try {
            this.prevScreen = prevScreen.getDeclaredConstructor(IslandGame.class);
        } catch (Exception e) {
            log("Error storing previous screen, 'exit' command may not function correctly", Color.RED);
            e.printStackTrace();
        }

        //log("Warning: It is possible to render the game unusable using this console", Color.YELLOW);
        log("Use at your own risk", Color.YELLOW);
        log("'help' for list of commands, 'exit' to exit the console");
    }

    //Displays input message in console - defaults to white color
    protected void log(String message) {
        log(message, Color.WHITE);
    }

    protected void log(String message, Color textColor) {
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

    protected void logParameterNumError(String commandName) {
        log("Unrecognized number of parameters for command: " + commandName, Color.RED);
    }

    private void enterCurrentCommand() {
        log(">" + inputField.getText());
        String[] inputs = inputField.getText().split("\\s+");
        inputField.setText("");
        //Execute command
        Command command = commands.get(inputs[0]);
        if (command == null)
            log("Unrecognized command: " + inputs[0], Color.RED);
        else {
            command.getScript().accept(this, inputs);
        }
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
        Gdx.gl.glClearColor(0, 0, 0, 1); //black bg
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            enterCurrentCommand();
        }
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
    }

    public IslandGame getGame() {
        return game;
    }

    protected Constructor<? extends InfoScreen> getPrevScreen() {
        return prevScreen;
    }

    /*Simple class to encapsulate command data - Script takes ConsoleScreen as input and params as array
      For simplicity and not having to slice the array, we always ignore the first param (the command name)
      in our scripts */
    private static class Command {
        private String helpDesc;
        private BiConsumer<ConsoleScreen, String[]> script;
        Command(String helpDesc, BiConsumer<ConsoleScreen, String[]> script) {
            this.helpDesc = helpDesc;
            this.script = script;
        }

        public BiConsumer<ConsoleScreen, String[]> getScript() {
            return script;
        }

        public String getHelpDesc() {
            return helpDesc;
        }

    }

    static {
        commands = new HashMap<>();
        commands.put("help", new Command("displays all commands and their function", (console, params) -> {
            if (params.length > 1)
                console.logParameterNumError("help");
            else {
                commands.keySet().stream()
                        .sorted()
                        .forEach(name -> console.log(name + " : " + commands.get(name).helpDesc));
            }
        }));
        commands.put("exit", new Command("exits the console", (console, params) -> {
            if (params.length > 1)
                console.logParameterNumError("exit");
            else {
                try {
                    console.dispose();
                    console.getGame().setScreen(console.getPrevScreen().newInstance(console.getGame()));
                } catch (Exception e) {
                    console.log("Error instantiating previous screen", Color.RED);
                    e.printStackTrace();
                }
            }
        }));
        commands.put("setstamina", new Command("[num] sets player stamina to num or max (whichever is lower). Input 'max' as num to set to max",
                (console, params) -> {
                    if (params.length != 2)
                        console.logParameterNumError("setstamina");
                    else {
                        Player player = console.getGame().getPlayer();
                        try {
                            int stam = 0;
                            if (params[1].toLowerCase().equals("max"))
                                stam = player.getMaxStamina();
                            else {
                                stam = Integer.parseInt(params[1]);
                            }
                            player.setStamina(Math.min(Math.max(0, stam), player.getMaxStamina()));
                        } catch (Exception e) {
                            console.log("Error with input for command: setstamina", Color.RED);
                            console.log("Make sure argument is integer or 'max'", Color.RED);
                        }
                    }
                }));
        commands.put("setmaxstamina", new Command("[num] sets player stamina to num",
                (console, params) -> {
                    if (params.length != 2)
                        console.logParameterNumError("setmaxstamina");
                    else {
                        try {
                            Player player = console.getGame().getPlayer();
                            player.setMaxStamina(Math.max(1, Integer.parseInt(params[1])));
                            if (player.getMaxStamina() < player.getStamina())
                                player.setStamina(player.getMaxStamina());
                        } catch (Exception e) {
                            console.log("Error with inputs for command: setmaxstamina", Color.RED);
                            console.log("Make sure argument is integer", Color.RED);
                        }
                    }
                }));
        commands.put("setlevel", new Command("[num] sets level to num", (console, params) -> {
            if (params.length != 2)
                console.logParameterNumError("setlevel");
            else {
                try {
                    console.getGame().initializeLevel(Integer.parseInt(params[1]));
                } catch (Exception e) {
                    console.log("Error with input for command: setlevel", Color.RED);
                    console.log("Make sure argument is integer");
                }
            }
        }));
        commands.put("giveitemsrand", new Command("[min] [max] gives player a random quantity between min and max of every item in the game. Values are must be in range 0-999",
                (console, params) -> {
                    if (params.length != 3)
                        console.logParameterNumError("giveitemsrand");
                    else {
                        try {
                            Random itemRand = new Random();
                            Player player = console.getGame().getPlayer();
                            int min = Integer.parseInt(params[1]);
                            int max = Integer.parseInt(params[2]);
                            if (max < min || min < 0 || max > 999)
                                throw new Exception("Input error");
                            for (Item item : Item.masterItemSet) {
                                player.addItemToInventory(item, min + itemRand.nextInt(max - min + 1));
                            }
                        } catch (Exception e) {
                            console.log("Error with input for command: giveitemsrand", Color.RED);
                            console.log("Ensure max is greater than min and params lie in specified range", Color.RED);
                        }
                    }
                }));
        commands.put("removeitem", new Command("[name] [quantity] removes quantity number of name from inventory. Use 'all' for name to apply to all items in inventory. Use 'all' for quantity to remove all of a given item(s)",
                (console, params) -> {
            if (params.length != 3)
                console.logParameterNumError("removeitem");
            else {
                try {
                    int quantity = params[2].equalsIgnoreCase("all") ? -1 : Integer.parseInt(params[2]);
                    if (quantity < -1)
                        throw new Exception();
                    Player player = console.getGame().getPlayer();
                    if (params[1].equalsIgnoreCase("all")) {
                        for (Item item : player.getInventory().keySet()) {
                            int quantityToRemove = Math.min(quantity == -1 ? Integer.MAX_VALUE : quantity, player.getInventory().get(item));
                            player.removeItemFromInventory(item, quantityToRemove);
                        }
                    } else {
                        Optional<Item> item = Item.masterItemSet.stream().filter(i -> params[1].equalsIgnoreCase(console.itemBundle.get(i.getNameKey()))).findFirst();
                        if (item.isPresent()) {
                            if (player.getInventory().containsKey(item.get())) {
                                int quantityToRemove = Math.min(quantity == -1 ? Integer.MAX_VALUE : quantity, player.getInventory().get(item.get()));
                                player.removeItemFromInventory(item.get(), quantityToRemove);
                            }
                        } else
                            console.log("Error: Item " + params[1] + " does not exist", Color.RED);
                    }
                } catch (Exception e) {
                    console.log("Error with input for command: removeitem", Color.RED);
                }
            }
                }));
    }
}
