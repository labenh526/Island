package com.laben.islands;

import com.badlogic.gdx.math.GridPoint2;



/** This class represents a Tile which is part of an Island and all things contained within the tile **/
public class Tile {

    private final Region region; //The region this tile is apart of
    private final Island island; //The Island object this tile is contained within
    private final GridPoint2 coordinates; //The coordinates on the Island in which this tile is located
    private boolean hasTreasure; //Whether or not the treasure is located on this tile

    //Generates a Tile given it's region, island, coordinates on the island, and level
    public Tile(Region region, Island island, GridPoint2 coordinates, int level) {
        this.region = region;
        this.island = island;
        this.coordinates = coordinates;
        hasTreasure = false; //defaults to false
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

    private static boolean coordinatesInBounds(GridPoint2 coordinatePair, Island island) {
        return 0 <= coordinatePair.x  && coordinatePair.x < island.getWidth() &&
                0 <= coordinatePair.y && coordinatePair.y < island.getHeight();
    }
}
