package com.laben.islands;

import com.badlogic.gdx.math.GridPoint2;

import java.util.List;
import java.util.*;

/** This class is an abstraction of an Island with a given width and height. The Island is randomly generated upon
 * creation.
 */
public class Island {

    private static final int AVG_TILES_PER_REGION = 20; //The average number of tiles per each region on the island
    private static final int LEVEL1_ISLAND_SIDE_SIZE = 5; //The starting side size of the island
    private static final int NUM_LEVELS_OF_SAME_SIZE = 5; //The # of levels in a row that have the same size island
    private static final int MAX_ISLAND_SIDE_SIZE = 20;

    private Tile[][] tileSet; //The set of tiles. First number is width value (x), second is height (y)
    private GridPoint2 treasureLocation; //The location of the treasure
    private final int width; //the width in tiles of the island
    private final int height; //the height in tiles of the island
    private IterableRegionMap iterableRegionMap;

    //Input the width and height of the Island as well as the level of this island (higher equals higher difficulty)
    public Island(int width, int height, int numRegions, int level) {
        this.width = width;
        this.height = height;
        initializeIsland(width, height, numRegions, level);
    }

    //Generates an Island, automatically determining its other variables based off of the level
    public Island(int level) {
        int sideSize = Math.min(islandSideSize(level), MAX_ISLAND_SIDE_SIZE);
        width = sideSize;
        height = sideSize;
        int numRegions = (int)Math.ceil(Math.pow(sideSize, 2) / AVG_TILES_PER_REGION);
        initializeIsland(sideSize, sideSize, numRegions, level);
    }

    //Essentially the constructor, generates an island with a given width, height, and level
    private void initializeIsland(int width, int height, int numRegions, int level) {
        tileSet = new Tile[width][height]; //initialize tile set
        boolean successfulMapGenerated;
        do {
            try {
                //Step 1: Generate Numerical Island
                int[][] numMap = mapWithNumericRegions(width, height, numRegions);
                Map<Integer, Set<Integer>> regionAdjacencies = allAdjacentRegionsInNumericMap(numMap);
                List<Integer> orderedRegions = regionsOrderedBySize(numMap);
                //Step 2: Assign specific regions
                Map<Integer, Region> regionIntegerConversionChart = Region.generatedRegions(level, orderedRegions, regionAdjacencies);
                iterableRegionMap = new IterableRegionMap(numMap, regionIntegerConversionChart);
                successfulMapGenerated = true;
            } catch (RuntimeException e) {
                successfulMapGenerated = false;
            }
        } while (!successfulMapGenerated);
        //create tiles
        for (int x = 0; x < getIterableRegionMap().getNumericalMap().length; x++) {
            for (int y = 0; y < getIterableRegionMap().getNumericalMap()[0].length; y++) {
                tileSet[x][y] = new Tile(getIterableRegionMap().getRegionIntegerConversionChart().get(
                        getIterableRegionMap().getNumericalMap()[x][y]), this, new GridPoint2(x, y), level);
            }
        }

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public GridPoint2 getTreasureLocation() {
        return treasureLocation;
    }

    public IterableRegionMap getIterableRegionMap() {
        return iterableRegionMap;
    }

    //Removes the treasure from its current location and moves it to the specified point
    public void changeTreasureLocation(GridPoint2 treasureLocation) {
        tileAtPoint(getTreasureLocation()).setHasTreasure(false);
        tileAtPoint(treasureLocation).setHasTreasure(true);
        this.treasureLocation = treasureLocation;
    }

    public Tile tileAtPoint(GridPoint2 point) {
        return tileSet[point.x][point.y];
    }

    //returns from a list of Integers representing regions from largest to smallest
    private static List<Integer> regionsOrderedBySize(int[][] map) {
        //Find the number of points in each region
        int[] sizeCount = new int[(int)Math.pow(map.length, 2) / AVG_TILES_PER_REGION + 10]; //buffer size of 10
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                sizeCount[map[x][y]] += 100;
            }
        }

        //Puts this data into a Map for easy access, key is the size * 100 about, value is the region num
        Map<Integer, Integer> regionSizes = new HashMap<>();
        int i = 1;

        while (sizeCount[i] > 0) {
            while (regionSizes.containsKey(sizeCount[i])) {
                sizeCount[i] ++;
            }
            regionSizes.put(sizeCount[i], i);
            i++;
        }

        List<Integer> sizeList = new ArrayList<>(regionSizes.keySet());
        Collections.sort(sizeList);
        Collections.reverse(sizeList);

        List<Integer> sortedRegionNums = new ArrayList<>();
        for (Integer size: sizeList) {
            sortedRegionNums.add(regionSizes.get(size));
        }
        return sortedRegionNums;

    }

    /* Given an island map with numerically defined regions, returns a (Java) Map in which the keys correspond
    * to Integers representing regions and the values correspond to the Integers representing the adjacent
    * regions to the key region. Note that diagonal points are not considered adjacent */
    private  static Map<Integer, Set<Integer>> allAdjacentRegionsInNumericMap(int[][] map) {
        Map<Integer, Set<Integer>>  adjacencyMap = new HashMap<>();
        //Iterate through entire given grid
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                if (!adjacencyMap.containsKey(map[x][y])) {
                    adjacencyMap.put(map[x][y], new HashSet<Integer>());
                }
                //Check surrounding points and add any to set
                adjacencyMap.get(map[x][y]).addAll(adjacentRegionsToPoint(map, new GridPoint2(x, y)));
            }
        }
        return adjacencyMap;
    }

    //Returns a matrix with different numbers representing different regions
    private static int[][] mapWithNumericRegions(int width, int height, int numRegions) {
        //Uses Voronoi Diagram
        int[][] map = new int[width][height]; //All points on grid default to 0
        //Step 1: Select random points on the grid
        Set<GridPoint2> vPoints = randomVPoints(width, height, numRegions);
        //Step 2: Assign those points to a unique non 0 value
        int count = 1;
        for (GridPoint2 point : vPoints)
            map[point.x][point.y] = count++;
        //Step 3: Assign all points on grid to closest v point
        return completeVoronoiMap(map, vPoints);
    }

    //Given a grid with specified non 0 Voronoi points, fills out the entire grid as Voronoi diagram
    private static int[][] completeVoronoiMap(int[][] map, Set<GridPoint2> vPoints) {
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                if (!vPoints.contains(new GridPoint2(x, y))) { //If its not a V Point
                    GridPoint2 closestPoint = closestPoint(new GridPoint2(x, y), vPoints);
                    map[x][y] = map[closestPoint.x][closestPoint.y];
                }
            }
        }
        return numMapWithoutLonePoints(map);
    }

    //Removes all stranded tiles from map
    private static int[][] numMapWithoutLonePoints(int[][] map) {
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[0].length; y++) {
                if (pointIsAlone(map, new GridPoint2(x, y))) {
                    map[x][y] = randomAdjacentValueInNumMap(map, new GridPoint2(x, y));
                }
            }
        }
        return map;
    }

    //Returns whether or not the point is alone
    private static boolean pointIsAlone(int[][] map, GridPoint2 point) {
        int x = point.x;
        int y = point.y;
        int value = map[x][y];
        if (x > 0 && map[x - 1][y] == value)
            return false;
        if (x < map.length - 1 && map[x + 1][y] == value)
            return false;
        if (y > 0 && map[x][y + 1] == value)
            return false;
        return y >= map[0].length - 1 || map[x][y - 1] != value;
    }

    //Returns a random adjacent value in a num map
    private static int randomAdjacentValueInNumMap(int[][] map, GridPoint2 point) {
        int x = point.x;
        int y = point.y;
        Random random = new Random();
        List<Integer> possibleValues = new ArrayList<>(4);
        if (x > 0)
            possibleValues.add(map[x - 1][y]);
        if (x < map.length - 1)
            possibleValues.add(map[x + 1][y]);
        if (y > 0)
            possibleValues.add(map[x][y - 1]);
        if (y < map[0].length - 1)
            possibleValues.add(map[x][y + 1]);
        return possibleValues.get(random.nextInt(possibleValues.size()));
    }

    //returns a set of randomly determined v (Voronoi) points given the dimensions of a map
    private static Set<GridPoint2> randomVPoints(int width, int height, int numVPoints) {
        Set<GridPoint2> points = new HashSet<>();
        Random random = new Random();
        for (int i = 0; i < numVPoints; i++) {
            GridPoint2 vPoint = new GridPoint2(); //the special point on the voronoi diagram
            do {
                vPoint.set(random.nextInt(width), random.nextInt(height));
            } while (points.contains(vPoint));
            points.add(vPoint);
        }
        return points;
    }

    //returns the distance between two points
    private static double distanceBetweenPoints(GridPoint2 a, GridPoint2 b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    //Given a specific point and a set of points, returns the closest point, randomly deciding in ties
    private static GridPoint2 closestPoint(GridPoint2 specificPoint, Set<GridPoint2> points) {
        double distance = Double.POSITIVE_INFINITY;
        Random random = new Random();
        List<GridPoint2> possibleClosestPoints = new ArrayList<>();
        for (GridPoint2 farPoint : points) {
            if (distanceBetweenPoints(specificPoint, farPoint) < distance) {
                possibleClosestPoints.clear();
                possibleClosestPoints.add(farPoint);
                distance = distanceBetweenPoints(specificPoint, farPoint);
            }
            else if (distanceBetweenPoints(specificPoint, farPoint) == distance)
                possibleClosestPoints.add(farPoint);
        }
        return possibleClosestPoints.get(random.nextInt(possibleClosestPoints.size()));
    }

    //Given a numerically defined map and a point on that map, returns all regions adjacent to that point
    private static Set<Integer> adjacentRegionsToPoint(int[][] map, GridPoint2 point) {
        Set<Integer> adjacentRegions = new HashSet<>();
        try { adjacentRegions.add(map[point.x + 1][point.y]); }
        catch (IndexOutOfBoundsException e) {}
        try {adjacentRegions.add(map[point.x - 1][point.y]); }
        catch (IndexOutOfBoundsException e) {}
        try {adjacentRegions.add(map[point.x][point.y + 1]); }
        catch (IndexOutOfBoundsException e) {}
        try {adjacentRegions.add(map[point.x][point.y - 1]); }
        catch (IndexOutOfBoundsException e) {}
        adjacentRegions.remove(map[point.x][point.y]);
        return adjacentRegions;
    }


    //Given the level, returns what the island's side size should be
    private static int islandSideSize(int level) {
        return (int)Math.ceil((double)level / (double)NUM_LEVELS_OF_SAME_SIZE) + LEVEL1_ISLAND_SIDE_SIZE - 1;
    }




    //TODO: Delete this. This is for testing only
    public static void main(String[] args) {
        int[][] map = mapWithNumericRegions(20, 20, 20);
        Map<Integer, Region> regionChart = Region.generatedRegions(35, regionsOrderedBySize(map),
                allAdjacentRegionsInNumericMap(map));
        System.out.print(regionChart);

    }

    //Represents all data of the map in numerical form for easy iteration through the map's rows
    public static class IterableRegionMap implements Iterable<List<Region>>{

        //A list containing each row in order, also represented as a list
        private List<List<Region>> rowList;
        private final int numRows;
        private final int numColumns;
        private final int[][] numericalMap;
        private final Map<Integer, Region> regionIntegerConversionChart;

        IterableRegionMap(int[][] mapData, Map<Integer, Region> regionIntegerConversionChart) {
            numericalMap = mapData;
            this.regionIntegerConversionChart = regionIntegerConversionChart;
            numRows = mapData[0].length;
            numColumns = mapData.length;
            //initialize the list of rows
            rowList = new ArrayList<>(mapData[0].length);
            for (int y = 0; y < mapData[0].length; y++) {
                List<Region> currentRow = new ArrayList<>(mapData.length);
                for (int x= 0; x < mapData.length; x++)
                    currentRow.add(regionIntegerConversionChart.get(mapData[x][y]));
                rowList.add(currentRow);
            }
        }

        public int[][] getNumericalMap() {
            return numericalMap;
        }

        public Map<Integer, Region> getRegionIntegerConversionChart() {
            return regionIntegerConversionChart;
        }

        public int getNumRows() {
            return numRows;
        }

        public int getNumColumns() {
            return numColumns;
        }

        @Override
        public Iterator<List<Region>> iterator() {
            return rowList.iterator();
        }


    }




}
