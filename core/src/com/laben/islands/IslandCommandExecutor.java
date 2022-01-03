package com.laben.islands;

import com.badlogic.gdx.Game;
import com.strongjoshua.console.CommandExecutor;

public class IslandCommandExecutor extends CommandExecutor {

    private IslandGame game;

    public IslandCommandExecutor(IslandGame game) {
        this.game = game;
    }

    public void exit() {
        game.setDevMode(false);
    }

}
