package com.laben.islands;

import com.badlogic.gdx.Gdx;

import java.io.FileNotFoundException;
import java.util.*;

/** A class designed to have methods dealing with generating Markov chains
 * An instance of this class represents a Markov chain from a given table of words. From there names can be pulled
 * out using Markov chain logic with the table as a reference.
 */
public class Markov {

    private final MarkovTable table; //The table representing probabilities
    private final int maxLength;
    private final int order;
    private final Random random;

    /*Input the file name to read from which should be located in the internal assets folder and the order
    * as well as the maximum length the generated word can be*/
    public Markov(String fileName, int order, int maxLength) {
        try {
            random = new Random();
            table = new MarkovTable(fileName, order);
            this.order = order;
            this.maxLength = maxLength;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File Not Found Exception");
        }
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getOrder() {
        return order;
    }

    //Returns the next randomized String generated using Markov chains
    public String nextValue() {
        //Start with a randomized starting value
        StringBuilder word = new StringBuilder(table.startingValues().get(random.nextInt(table.startingValues().size())));
        while (word.length() < getMaxLength()) {
            List<String> possibleValues = table.possibleValues(word.substring(word.length() - order, word.length()));
            if (possibleValues == null)
                return word.toString();
            word.append(possibleValues.get(random.nextInt(possibleValues.size())));
        }
        return word.toString();
    }


    //A class to represent the table of markov probabilities
    private static class MarkovTable {
        private final Map<String, List<String>> table;

        //Input the filename and the order
        MarkovTable(String fileName, int order) throws FileNotFoundException{
            if (!Gdx.files.internal(fileName).exists())
                throw new FileNotFoundException("Specified file does not exist");
            final String[] words = Gdx.files.internal(fileName).readString().split("\\s+");
            //Create Table
            table = generatedTable(words, order);
        }

        //Returns null if no such entry
        List<String> possibleValues(String key) {
            return table.get(key);
        }

        //Returns the set of starting values
        List<String> startingValues() {
           return new ArrayList<>(table.keySet());
        }

        private static Map<String, List<String>> generatedTable(String[] words, int order) {
            Map<String, List<String>> mTable = new HashMap<>();
            //Iterate through every word in the given array of words
            for (String word : words) {
                //If word is the size of the order or smaller, it's useless
                if (word.length() > order) {
                    //Add to table
                    for (int i = 0; i < word.length() - order; i++) {
                        String key = word.substring(i, i + order).toLowerCase();
                        //If this entry to the map doesn't exist, create a new entry
                        if (!mTable.containsKey(key))
                            mTable.put(key, new ArrayList<String>((int)Math.pow(26, order)));
                        mTable.get(key).add(Character.toString(word.charAt(i + order)).toLowerCase());
                    }
                }
            }
            return mTable;
        }
    }
}
