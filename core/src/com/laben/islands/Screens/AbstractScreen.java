package com.laben.islands.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.laben.islands.IslandGame;


public abstract class AbstractScreen implements Screen {
    //Returns the stage object for this screen
    public abstract Stage getStage();

    protected void setInputProcessor(IslandGame game, Stage stage) {
        Gdx.input.setInputProcessor(stage);
    }
}
