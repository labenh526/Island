package com.laben.islands;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.awt.*;
import java.util.Collection;
import java.util.Map;

/** This Class is the main game class which manages the game and is created upon the games initialization
 * 	This class contains all information pertaining to the game's current game state
 **/
public class IslandGame extends Game {
	public static final int GAME_WIDTH = 800;
	public static final int GAME_HEIGHT = 480;

	private Island currentIsland;
	private int currentLevel;
	private AssetManager manager; //Manages all game assets
	private Tile currentTile; //The current tile that the player is on
	
	@Override
	public void create () {
		manager = new AssetManager();
		setCurrentIsland(new Island(69));
		setStartingPos();
		setScreen(new MapViewScreen(this));
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
		setCurrentTile(getCurrentIsland().tileAtPoint(new Point(x, y)));
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

}
