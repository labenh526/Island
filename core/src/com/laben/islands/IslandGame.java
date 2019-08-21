package com.laben.islands;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.I18NBundle;
import com.laben.islands.Screens.GameScreen;
import com.laben.islands.Screens.InfoScreen;
import com.laben.islands.Screens.InventoryScreen;
import com.laben.islands.Screens.MapViewScreen;
import com.laben.islands.items.Item;

import java.util.Collection;
import java.util.Map;

/** This Class is the main game class which manages the game and is created upon the games initialization
 * 	This class contains all information pertaining to the game's current game state
 **/
public class IslandGame extends Game {
	//Desktop Aspect Ratio 800:480, Android Aspect Ratio 480:640
	public static int GAME_WIDTH = 800;
	public static int GAME_HEIGHT = 480;

	private Island currentIsland;
	private int currentLevel;
	private AssetManager manager; //Manages all game assets
	private Tile currentTile; //The current tile that the player is on
	private Application.ApplicationType platform;
	private Player player;
	private boolean inputAllowed;
	
	@Override
	public void create () {
		//Determine the platform that the game is being run on
		platform = Gdx.app.getType();

		GAME_WIDTH = Application.ApplicationType.Android.equals(Gdx.app.getType()) ? 480 : 800;
		GAME_HEIGHT = Application.ApplicationType.Android.equals(Gdx.app.getType()) ? 640 : 480;

		inputAllowed = true;

		manager = new AssetManager();
		manager.load("i18n/GeneralBundle", I18NBundle.class);
		//create player
		player = new Player();
		player.setStamina(1);
		setCurrentLevel(76);
		setCurrentIsland(new Island(getCurrentLevel()));
		setStartingPos();
		player.addItemToInventory(Item.masterItemSet.get(1), 10);
		player.addItemToInventory(Item.masterItemSet.get(0));
		player.addItemToInventory(Item.masterItemSet.get(2), 3);
		player.addItemToInventory(Item.masterItemSet.get(0), 7);
		player.addItemToInventory(Item.masterItemSet.get(3), 1);
		player.addItemToInventory(Item.masterItemSet.get(4), 11);
		player.addItemToInventory(Item.masterItemSet.get(5), 2);
		player.addItemToInventory(Item.masterItemSet.get(6));
		player.addItemToInventory(Item.masterItemSet.get(7), 19);
		player.addItemToInventory(Item.masterItemSet.get(8), 2);
		player.addItemToInventory(Item.masterItemSet.get(9));
		setScreen(new InventoryScreen(this));
		//setScreen(new com.laben.islands.Screens.GameScreen(this, getCurrentTile()));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		getScreen().dispose(); //dispose of all disposables in the current screen
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
}
