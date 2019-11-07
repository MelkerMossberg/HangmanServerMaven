package Hangman.game;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class RandomWordGenerator {

    Random random;
    BufferedReader br;
    ArrayList<String> list;

    /**
     * Read every word in the text-file and store in an ArrayList when created.
     */
    public RandomWordGenerator(){
        random = new Random();
        list = new ArrayList<>();

        File file = new File("words.txt");
        FileReader fr = null;
        try {
            fr = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        br = new BufferedReader(fr);
        try {
            String s;
            while ((s = br.readLine()) != null) {
                list.add(s);
            }
            br.close();
            fr.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String pickRandomWord(){
        int randomChoice = random.nextInt(list.size());
        String word = list.get(randomChoice).toLowerCase();;
        System.out.println("Random word: " + word);
        return word;

    }
}
