package com.laben.islands.Screens;

import com.laben.islands.IslandGame;

import java.util.HashMap;
import java.util.Map;

public class ClueScreen extends InfoScreen {

    private static Map<String, Class> clueScreenAssets;

    static {
        clueScreenAssets = new HashMap<>();
    }

    public ClueScreen(IslandGame game) {
        super(game, ScreenType.CLUE, clueScreenAssets);
    }

}
