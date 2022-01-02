package com.laben.islands;

import java.util.*;

/** This represents a region which has a specialized name and a specific terrain **/
public class Region {
    private static final String REGION_NAMES_FILE = "Text_Databases/Region_Prefixes.txt";
    private static final Markov REGION_NAME_MARKOV = new Markov(REGION_NAMES_FILE, 3, 10);

    private final Terrain terrain; //the regions terrain
    private final String name; //the name that comes before the terrain

    public Region(String name, Terrain terrain) {
        this.name = name;
        this.terrain = terrain;
    }

    @Override
    public String toString() {
        return name + " " + terrain.toString();
    }

    public Terrain getTerrain() {
        return terrain;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Region && o.toString().equals(this.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /* Given: The level, a list of regions in order descending order of size, and a map marking region adjacencies,
    * Returns a Map designating a specific region to each numerical value */
    public static Map<Integer, Region> generatedRegions(int level, List<Integer> orderedRegions, Map<Integer,
            Set<Integer>> regionAdjacencies) {
        Map<Integer, Region> generatedRegions = new HashMap<>();
        //For every region
        for (Integer regionNum : orderedRegions) {
            //Determine Terrains that aren't allowed
            Set<Terrain> illegalTerrainSet = new HashSet<>();
            for (Integer adjacentRegion : regionAdjacencies.get(regionNum)) {
                if (generatedRegions.containsKey(adjacentRegion)) {
                    illegalTerrainSet.add(generatedRegions.get(adjacentRegion).getTerrain());
                }
            }
            //Determine Terrain
            Terrain terrain = Terrain.randomTerrain(level, illegalTerrainSet);

            //Generate name and make sure that it isn't already being used
            Region region;
            do {
                region = new Region(generatedRegionName(terrain), terrain);
            } while(generatedRegions.values().contains(region));

            //Add the newly generated region to the generatedRegions Map
            generatedRegions.put(regionNum, region);
        }

        return generatedRegions;
    }

    //Generates a name for the region given a specific terrain using Markov chains
    private static String generatedRegionName(Terrain terrain) {
        String generatedName = REGION_NAME_MARKOV.nextValue();
        return generatedName.substring(0, 1).toUpperCase() + generatedName.substring(1);
    }


}
