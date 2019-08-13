package com.laben.islands;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** This Class is the main game class which manages the game and is created upon the games initialization
 * 	This class contains all information pertaining to the game's current game state
 **/
public class IslandGame extends Game {
	public static final int GAME_WIDTH = 800;
	public static final int GAME_HEIGHT = 480;

	private Island currentIsland;
	private int currentLevel;
	private AssetManager manager; //Manages all game assets

	SpriteBatch batch;
	
	@Override
	public void create () {
		manager = new AssetManager();
		batch = new SpriteBatch();
		setCurrentIsland(new Island(16));
		setScreen(new MapViewScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		getScreen().dispose(); //dispose of all disposables in the current screen
		batch.dispose();
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
}
