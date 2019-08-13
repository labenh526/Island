package com.laben.islands.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.laben.islands.IslandGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Island Game Title";
		config.width = 800;
		config.height = 480;
		new LwjglApplication(new IslandGame(), config);
	}
}
