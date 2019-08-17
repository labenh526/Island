package com.laben.islands;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;

import java.util.*;


/** This class represents a Tile which is part of an Island and all things contained within the tile **/
public class Tile {

    private static final double ROCK_CHANCE = .00204;
    private static final double GRASS_CHANCE = .08163;
    public static final float TREE_CORNER_SIDE_SIZE = (float) IslandGame.GAME_WIDTH * .1f;

    public enum GraphicsItem {EMPTY, GRASS, ROCK} //Randomly generated images purely for aesthetics

    private final Region region; //The region this tile is apart of
    private final Island island; //The Island object this tile is contained within
    private final GridPoint2 coordinates; //The coordinates on the Island in which this tile is located
    private boolean hasTreasure; //Whether or not the treasure is located on this tile
    private final GraphicsItem[][] graphicsItemsTable; //[y][x] format
    private final Trees trees;

    //Generates a Tile given it's region, island, coordinates on the island, and level
    public Tile(Region region, Island island, GridPoint2 coordinates, int level) {
        this.region = region;
        this.island = island;
        this.coordinates = coordinates;
        hasTreasure = false; //defaults to false
        graphicsItemsTable = generatedGraphicsItemsTable();
        trees = new Trees();
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

    public Trees getTrees() {
        return trees;
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

    /* Represents all of the trees */
    public class Trees {
        private final Map<Integer, Set<Vector2>> treeCorners;
        private final Random random;

        public Trees() {
            treeCorners = new HashMap<>();
            for (int i = 0; i < 4; i++)
                treeCorners.put(i, new HashSet<Vector2>());
            random = new Random();
            //Generate trees 1 corner at a time
            for (int j = 0; j < 4; j++) {
                int numTrees = (int) Math.round(region.getTerrain().getTreesPerCorner() + random.nextGaussian() *
                        region.getTerrain().getTreesPerCorner() * .3);
                if (numTrees > 0) {
                    for (int i = 0; i < numTrees; i++)
                        generateTree(j);
                }
            }
        }

        //Corners start at 0 in the top left and go clockwise
        private void generateTree(int corner) {
            Vector2 treePos = new Vector2(random.nextFloat() * TREE_CORNER_SIDE_SIZE,
                    random.nextFloat() * TREE_CORNER_SIDE_SIZE);
            if (validTreePos(treePos, corner))
                treeCorners.get(corner).add(treePos);
        }

        //Tree positions are invalid if they are located in the same area as a GraphicsItem
        private boolean validTreePos(Vector2 pos, int corner) {
            //Determine which cells in the tile to check for graphics items. Depends on the corner
            int startingYPoint = corner == 0 || corner == 1 ? 3 :
                    (int)(7f * (1f - TREE_CORNER_SIDE_SIZE / GameScreen.GAME_TABLE_HEIGHT));
            int startingXPoint = corner == 0 || corner == 3 ? 0 :
                    (int)(7f * (1f - TREE_CORNER_SIDE_SIZE / GameScreen.GAME_TABLE_WIDTH));
            int endingYPoint = corner == 0 || corner == 1 ? (int)Math.ceil(
                    TREE_CORNER_SIDE_SIZE * 7f / GameScreen.GAME_TABLE_HEIGHT + 3f) : graphicsItemsTable.length;
            int endingXPoint = corner == 0 || corner == 3 ? (int)Math.ceil(
                    TREE_CORNER_SIDE_SIZE * 7f / GameScreen.GAME_TABLE_WIDTH) : graphicsItemsTable[0].length;

            int x = 0;
            int y = 0; //These ints are the relative x and y values
            for (int i = startingYPoint; i < endingYPoint; i++) {
                for (int j = startingXPoint; j < endingXPoint; j++) {
                    //If not empty
                    if (graphicsItemsTable[i][j] != GraphicsItem.EMPTY ||
                            vectorWithinGraphicsTableCell(pos, new GridPoint2(x, y))) {
                        return false;
                    }
                    x++;
                }
                y++;
            }
            return true;
        }

        private boolean vectorWithinGraphicsTableCell(Vector2 vector, GridPoint2 tableCell) {
            float cellSideSize = GameScreen.GAME_TABLE_WIDTH / 7;
            return (float)tableCell.x * cellSideSize <= vector.x && vector.x < (float)tableCell.x * cellSideSize &&
                    (float)tableCell.y * cellSideSize <= vector.y && vector.y < (float)tableCell.x * cellSideSize;
        }

        public Set<Vector2> treeCoordinates(int corner) {
            return treeCorners.get(corner);
        }
    }
}
