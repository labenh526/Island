package com.laben.islands;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.laben.islands.Screens.*;
import com.laben.islands.Screens.GameScreen;
import com.laben.islands.Screens.MapViewScreen;
import com.laben.islands.items.Item;
import com.strongjoshua.console.Console;
import com.strongjoshua.console.GUIConsole;

import java.awt.*;
import java.util.*;

/** This Class is the main game class which manages the game and is created upon the games initialization
 * 	This class contains all information pertaining to the game's current game state
 **/
public class IslandGame extends Game {
	//Desktop Aspect Ratio 800:480, Android Aspect Ratio 480:640
	public static int GAME_WIDTH = 800;
	public static int GAME_HEIGHT = 480;

	//Developer tools
	private final int CONSOLE_KEY = Input.Keys.Z;
	private boolean isDevMode = false;
	private Console console;
	private Skin consoleSkin;

	private Island currentIsland;
	private int currentLevel;
	private AssetManager manager; //Manages all game assets
	private Tile currentTile; //The current tile that the player is on
	private Application.ApplicationType platform;
	private Player player;
	private boolean inputAllowed;
	private Map<String, Class> assets;
	private TextureAtlas atlas;

	//For text boxes:
	private InputProcessor currentInput;
	private Stage currentStage;
	private Image grey;
	private Image textBox;
	private Label text;
	private Label.LabelStyle textStyle;
	
	@Override
	public void create () {
		//Determine the platform that the game is being run on
		platform = Gdx.app.getType();

		GAME_WIDTH = Application.ApplicationType.Android.equals(Gdx.app.getType()) ? 480 : 800;
		GAME_HEIGHT = Application.ApplicationType.Android.equals(Gdx.app.getType()) ? 640 : 480;

		inputAllowed = true;
		manager = new AssetManager();

		assets = new HashMap<>();
		assets.put("i18n/GeneralBundle", I18NBundle.class);
		assets.put("GeneralTextures.atlas", TextureAtlas.class);
		assets.put("Fonts/TextBox.fnt", BitmapFont.class);
		assets.put("Fonts/ConsoleFont.fnt", BitmapFont.class);
		loadAllAssets(manager, assets);

		//--- Code to execute while loading assets ---
		//create player
		player = new Player();
		setCurrentLevel(1);

		setCurrentIsland(new Island(getCurrentLevel()));
		setStartingPos();

		Random itemRand = new Random();
		for (Item item : Item.masterItemSet) {
			getPlayer().addItemToInventory(item, itemRand.nextInt(10) + 1);
		}

		manager.finishLoading();
		//--- Everything requiring loaded assets below this ---
		atlas = manager.get("GeneralTextures.atlas");

		consoleSkin = new Skin(new FileHandle("ConsoleSkin.json"), atlas);
		manager.finishLoading();
		consoleSkin.add("default-rect", new TextureRegion(atlas.findRegion("Black")));
		consoleSkin.add("default", new TextButton.TextButtonStyle(new TextureRegionDrawable(atlas.findRegion("Black")), new TextureRegionDrawable(atlas.findRegion("Black")), new TextureRegionDrawable(atlas.findRegion("Black")), manager.get("Fonts/ConsoleFont.fnt")));
		console = new GUIConsole(consoleSkin);
		console.setDisabled(!isDevMode);
		console.setSizePercent(100,90);
		console.setPositionPercent(0, 67);
		console.setDisplayKeyID(Input.Keys.UNKNOWN);
		console.setCommandExecutor(new IslandCommandExecutor(this));
		console.setPosition(0, 0);
		console.setTitle("Developer Console");

		//setScreen(new InventoryScreen(this));
		setScreen(new com.laben.islands.Screens.GameScreen(this, getCurrentTile()));


		//For TextBox
		textStyle = new Label.LabelStyle();
		textStyle.font = manager.get("Fonts/TextBox.fnt");
		textStyle.fontColor = Color.BLACK;

	}

	@Override
	public void setScreen(Screen screen) {
		currentStage = ((AbstractScreen)screen).getStage();
		super.setScreen(screen);
	}

	@Override
	public void render () {
		super.render();
		if (currentInput != null && (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)))
			finishDisplayingTextBox();
		console.draw();
	}
	
	@Override
	public void dispose () {
		getScreen().dispose(); //dispose of all disposables in the current screen
		console.dispose();
		unloadAllAssets(manager, assets.keySet());
	}

	//Sets the the player's starting position in the middle of the bottom row
	public void setStartingPos() {
		int x = (getCurrentIsland().getWidth() - 1)/ 2;
		int y = getCurrentIsland().getHeight() - 1;
		setCurrentTile(getCurrentIsland().tileAtPoint(new GridPoint2(x, y)));
	}


	public void loadMapViewScreen() {
		getScreen().dispose();
		setScreen(new MapViewScreen(this));
	}

	public void loadGameScreen() {
		getScreen().dispose();
		setScreen(new GameScreen(this, getCurrentTile()));
	}

	public AssetManager getManager() {
		return manager;
	}

	public static int getGameWidth() {
		return GAME_WIDTH;
	}

	public static int getGameHeight() {
		return GAME_HEIGHT;
	}

	public Island getCurrentIsland() {
		return currentIsland;
	}

	public void setCurrentIsland(Island currentIsland) {
		this.currentIsland = currentIsland;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public void setCurrentTile(Tile currentTile) {
		this.currentTile = currentTile;
	}

	public Tile getCurrentTile() {
		return currentTile;
	}

	public Player getPlayer() {
		return player;
	}

	public Application.ApplicationType getPlatform() {
		return platform;
	}

	public void resetConsoleInputProcessing() {
		console.resetInputProcessing();
	}

	public void setDevMode(boolean on) {
		isDevMode = on;
		setInputAllowed(!on);
		console.setDisabled(!on);
		if (on) {
			greyOutBackground();
			console.log("'help' for list of commands");
			console.log("'exit' to exit the developer console");
		} else
			grey.remove();
		console.setVisible(on);
	}

	public boolean isDevMode() {
		return isDevMode;
	}

	/* Since this application's target platform is only desktop and android, this method can be used to determine
		which platform the application is currently running on
	*/
	public boolean isAndroid() {
		return getPlatform().equals(Application.ApplicationType.Android);
	}

	/*
              Input an asset manager to load assets into and a map containing keys which correspond to asset file names
              and values which correpsond to that asset's class
            */
	public static void loadAllAssets(AssetManager assetManager, Map<String, Class> assets) {
		for (String asset : assets.keySet())
			assetManager.load(asset, assets.get(asset));
	}

	/*
		Unloads all given assets from manager
	 */
	public static void unloadAllAssets(AssetManager assetManager, Collection<String> assets) {
		for (String asset : assets)
			assetManager.unload(asset);
	}

	public boolean isInputAllowed() {
		return inputAllowed;
	}

	public void setInputAllowed(boolean inputAllowed) {
		this.inputAllowed = inputAllowed;
	}

	public I18NBundle getGeneralBundle() {
		return manager.get("i18n/GeneralBundle");
	}

	//Input the key to the text that is displayed
	public void displayTextBox(String textKey) {
		//Disallow input
		setInputAllowed(false);
		currentInput = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(null);
		resetConsoleInputProcessing();
		//Grey out
		greyOutBackground();
		//Add text box
		textBox = new Image(atlas.findRegion("TextBox"));
		textBox.setSize(getGameWidth(), (float)getGameHeight() / 3f);
		textBox.setPosition(0,0);
		currentStage.addActor(textBox);
		//Add text
		text = new Label(getGeneralBundle().get(textKey), textStyle);
		text.setWrap(true);
		text.setFontScale(.4f);
		textBox.validate();
		text.setSize(textBox.getWidth() * 792f / 800f, textBox.getHeight() * 152f / 160f);
		text.setPosition(textBox.getX() + textBox.getWidth() * 4f / 800f,
				textBox.getY() + textBox.getHeight() * 4f / 160f);
		text.setAlignment(Align.topLeft);
		currentStage.addActor(text);
	}

	//Greys out the background for use with things like text box and console
	private void greyOutBackground() {
		grey = new Image(atlas.findRegion("Grey"));
		grey.setPosition(0, 0);
		grey.setSize(getGameWidth(), getGameHeight());
		currentStage.addActor(grey);
	}

	//Draws a stamina bar to the current stage. Returns the stamina bar object
	public static StaminaBar createStaminaBar(Stage stage, TextureRegion bg, TextureRegion greyBg, TextureRegion bar,
									   float xPos, float yPos, float width, float height) {
		Image staminaBackground = new Image(bg);
		staminaBackground.setSize(width, height);
		staminaBackground.setPosition(xPos, yPos);
		stage.addActor(staminaBackground);

		//Create stamina bar
		staminaBackground.validate();
		Image staminaBar = new Image(bar);
		staminaBar.setHeight(.8f * height);
		staminaBar.setPosition(xPos + .05f * width, yPos + .1f * height);
		float maxStaminaWidth = staminaBackground.getWidth() * .9f;
		stage.addActor(staminaBar);

		//Create grey background to stamina bar
		staminaBar.validate();
		Image greyStaminaBackground = new Image(greyBg);
		greyStaminaBackground.setPosition(staminaBar.getX(), staminaBar.getY());
		greyStaminaBackground.setSize(maxStaminaWidth, staminaBar.getHeight());
		stage.addActor(greyStaminaBackground);

		return new StaminaBar(staminaBackground, staminaBar, greyStaminaBackground, maxStaminaWidth);
	}

	private void finishDisplayingTextBox() {
		Gdx.input.setInputProcessor(currentInput);
		resetConsoleInputProcessing();
		currentInput = null;
		setInputAllowed(true);
		grey.remove();
		textBox.remove();
		text.remove();
	}


	public static class StaminaBar {
		final Image blackBackground;
		final Image bar;
		final Image greyBackground;
		final float maxStaminaWidth;

		private StaminaBar(Image blackBackground, Image bar, Image greyBackground, float maxStaminaWidth) {
			this.bar = bar;
			this.blackBackground = blackBackground;
			this.greyBackground = greyBackground;
			this.maxStaminaWidth = maxStaminaWidth;
		}

		public Image getBlackBackground() {
			return blackBackground;
		}

		public Image getBar() {
			return bar;
		}

		public Image getGreyBackground() {
			return greyBackground;
		}

		public float getMaxStaminaWidth() {
			return maxStaminaWidth;
		}

		//Instantaneously changes the stamina bar to reflect the player's current stamina
		public void updateStaminaInstantaneous(IslandGame game) {
			getBar().setWidth((float)game.getPlayer().getStamina() /  (float)game.getPlayer().getMaxStamina()  *
					getMaxStaminaWidth());
			getBar().toFront();
		}
	}


}
