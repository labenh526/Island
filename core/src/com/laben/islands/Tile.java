package com.laben.islands;

import com.badlogic.gdx.math.GridPoint2;

import java.util.Random;


/** This class represents a Tile which is part of an Island and all things contained within the tile **/
public class Tile {

    private static final double ROCK_CHANCE = .00204;
    private static final double GRASS_CHANCE = .08163;

    public enum GraphicsItem {EMPTY, GRASS, ROCK} //Randomly generated images purely for aesthetics

    private final Region region; //The region this tile is apart of
    private final Island island; //The Island object this tile is contained within
    private final GridPoint2 coordinates; //The coordinates on the Island in which this tile is located
    private boolean hasTreasure; //Whether or not the treasure is located on this tile
    private final GraphicsItem[][] graphicsItemsTable;

    //Generates a Tile given it's region, island, coordinates on the island, and level
    public Tile(Region region, Island island, GridPoint2 coordinates, int level) {
        this.region = region;
        this.island = island;
        this.coordinates = coordinates;
        hasTreasure = false; //defaults to false
        graphicsItemsTable = generatedGraphicsItemsTable();
    }

    //Returns null if no tile is above this one
    public Tile tileAbove() {
        GridPoint2 newCoordinates = new GridPoint2(getCoordinates().x, getCoordinates().y - 1);
        if (coordinatesInBounds(newCoordinates, island))
            return island.tileAtPoint(newCoordinates);
        return null;
    }

    //Returns null if no tile is below this one
    public Tile tileBelow() {
        GridPoint2 newCoordinates = new GridPoint2(getCoordinates().x, getCoordinates().y + 1);
        if (coordinatesInBounds(newCoordinates, island))
            return island.tileAtPoint(newCoordinates);
        return null;
    }

    //Returns null if no tile left of this one
    public Tile tileLeft() {
        GridPoint2 newCoordinates = new GridPoint2(getCoordinates().x - 1, getCoordinates().y );
        if (coordinatesInBounds(newCoordinates, island))
            return island.tileAtPoint(newCoordinates);
        return null;
    }

    //Returns null if no tile right of this one
    public Tile tileRight() {
        GridPoint2 newCoordinates = new GridPoint2(getCoordinates().x + 1, getCoordinates().y);
        if (coordinatesInBounds(newCoordinates, island))
            return island.tileAtPoint(newCoordinates);
        return null;
    }

    public GraphicsItem[][] getGraphicsItemsTable() {
        return graphicsItemsTable;
    }

    public Region getRegion() {
        return region;
    }

    public Island getIsland() {
        return island;
    }

    public GridPoint2 getCoordinates() {
        return coordinates;
    }

    public boolean hasTreasure() {
        return hasTreasure;
    }

    public void setHasTreasure(boolean hasTreasure) {
        this.hasTreasure = hasTreasure;
    }

    private static GraphicsItem[][] generatedGraphicsItemsTable() {
        GraphicsItem[][] table = new GraphicsItem[7][7];
        for (int x = 0; x < table.length; x++) {
            for (int y = 0; y < table[0].length; y++) {
                //Generate random item
                Random random = new Random();
                double p = random.nextDouble();
                if (p < ROCK_CHANCE)
                    table[x][y] = GraphicsItem.ROCK;
                else if (p < GRASS_CHANCE + ROCK_CHANCE)
                    table[x][y] = GraphicsItem.GRASS;
                else table[x][y] = GraphicsItem.EMPTY;
            }
        }
        return table;
    }

    private static boolean coordinatesInBounds(GridPoint2 coordinatePair, Island island) {
        return 0 <= coordinatePair.x  && coordinatePair.x < island.getWidth() &&
                0 <= coordinatePair.y && coordinatePair.y < island.getHeight();
    }
}
