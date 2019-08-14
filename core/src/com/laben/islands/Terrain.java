package com.laben.islands;

import java.util.*;

/** Represents the terrain and its properties**/
public class Terrain {

    public static final Terrain WOODS = new Terrain("Woods", 1, "WoodsBackground");
    public static final Terrain DESERT = new Terrain("Desert", 4, "DesertBackground");
    public static final Terrain PLAINS = new Terrain("Plains", 1, "PlainsBackground");
    public static final Terrain TUNDRA = new Terrain("Tundra", 24, "TundraBackground");
    public static final Terrain SWAMP = new Terrain("Swamp", 14, "SwampBackground");
    public static final Terrain JUNGLE = new Terrain("Jungle", 34, "JungleBackground");

    public static final Set<Terrain> TERRAIN_SET = new HashSet<>(Arrays.asList(
            WOODS, DESERT, PLAINS, TUNDRA, SWAMP, JUNGLE));
    public static final String MAP_VIEW_ATLAS_PATH = "MapViewTextures.atlas";



    private final String name; //The text representation of the terrain
    private final int startingLevel; //The level in which this terrain has an equal chance of being randomly selected
    private final String backgroundImage; //The background image used when drawing the Game Screen

    private Terrain(String name, int startingLevel, String backgroundImage) {
        this.name = name;
        this.startingLevel = startingLevel;
        this.backgroundImage = backgroundImage;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Terrain && o.toString().equals(this.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    private int getStartingLevel() {
        return startingLevel;
    }

    //Given the current level, will determine a random terrain
    public static Terrain randomTerrain(int level) {
        return randomTerrain(level, new HashSet<Terrain>());
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    //Given the current level and a set of illegal terrain options, returns a random terrain
    @SuppressWarnings("unchecked")
    public static Terrain randomTerrain(int level, Set<Terrain> illegalTerrain) {
        //Determine the relative probabilities of all available terrain
        Set<Terrain> availableTerrain = (Set<Terrain>)(((HashSet<Terrain>) TERRAIN_SET).clone());
        availableTerrain.removeAll(illegalTerrain);
        Map<Terrain, Double> relativeProbabilities = relativeProbabilities(level, availableTerrain);
        //Calculate sum of relative probabilities
        Double sumOfProbabilities = 0.0;
        for (Double probability : relativeProbabilities.values())
            sumOfProbabilities += probability;
        //Calculate absolute probabilities
        Map<Terrain, Double> absoluteProbabilities = new HashMap<>();
        for (Terrain terrain : relativeProbabilities.keySet())
            absoluteProbabilities.put(terrain, relativeProbabilities.get(terrain) / sumOfProbabilities);
        //Randomly choose terrain based off of its absolute probability
        double p = new Random().nextDouble();
        double cumulativeProbability = 0.0;
        for  (Terrain terrain : absoluteProbabilities.keySet()) {
            cumulativeProbability += absoluteProbabilities.get(terrain);
            if (p <= cumulativeProbability)
                return terrain;
        }
        throw new RuntimeException("Probabilities were somehow inaccurately calculated");
    }

    /*Given the current level, and a set of terrain, generates a Map which corresponds each given terrain with
      a relative probability ranging from 0 to 1 inclusive*/
    private static Map<Terrain, Double> relativeProbabilities(int level, Set<Terrain> terrainSet) {
        Map<Terrain, Double> relativeProbabilities = new HashMap<>();
        for (Terrain terrain : terrainSet)
            relativeProbabilities.put(terrain, relativeProbabilityOfTerrain(level, terrain));
        return relativeProbabilities;
    }

    //Given the current level and a specific terrain, returns the relative probability (0-1) of that terrain
    private static double relativeProbabilityOfTerrain(int level, Terrain terrain) {
        double lv = level;
        if (lv < .5 * (double)terrain.getStartingLevel())
            return 0;
        if (.5 * (double)terrain.getStartingLevel() <= lv && lv < (double)terrain.getStartingLevel())
            return 1.8 * lv / (double)terrain.getStartingLevel() - .8;
        return 1;
    }


}
