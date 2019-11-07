package Hangman.game;

import org.json.simple.JSONObject;

import java.util.ArrayList;

class GameState {
    String state;
    String word;
    String previousWord;
    ArrayList<String> guessedLetters;
    int numUniqueLetters;
    int gameScore;
    int livesLeft;
    int numCorrectGuesses;
    boolean lastGuessWasCorrect;
    char[] theHiddenWord;

    GameState(String word, int gameScore, String previousWord){
        this.word = word;
        this.previousWord = previousWord;
        this.gameScore = gameScore;
        this.guessedLetters = new ArrayList<String>();
        this.livesLeft = 3;      //START VALUE FOR ATTEMPS
        this.numCorrectGuesses = 0;
        this.numUniqueLetters = calcUniqueChars(word);

        this.theHiddenWord = new char[word.length()];
        for (int i = 0; i < theHiddenWord.length; i++ ){
            theHiddenWord[i] = '_';
        }

    }

    private int calcUniqueChars(String word) {
        int count = (int) word.chars().distinct().count();
        return count;
    }

    public void registerCorrectGuess(String letter){
        this.guessedLetters.add(letter);
        this.numCorrectGuesses++;
        this.lastGuessWasCorrect = true;
    }

    public void registerIncorrectGuess(String letter){
        this.guessedLetters.add(letter);
        this.livesLeft--;
        System.out.println("Attemps left: " + livesLeft);
        this.lastGuessWasCorrect = false;
    }

    public String packageJSON(){
        JSONObject gameStateDetails = new JSONObject();
        gameStateDetails.put("state", state);
        gameStateDetails.put("score", gameScore);
        gameStateDetails.put("livesLeft", livesLeft);
        gameStateDetails.put("numCorrect", numCorrectGuesses);
        gameStateDetails.put("prevGuesses", guessedLetters.toString());
        gameStateDetails.put("prevWord", previousWord);
        gameStateDetails.put("hiddenWord", buildHiddenWord());

        JSONObject gameState = new JSONObject();
        gameState.put("gameState", gameStateDetails);

        return gameState.toJSONString();
    }

    private String buildHiddenWord() {
        char c;
        char[] wordArr = word.toCharArray();
        for(int i = 0; i < guessedLetters.size(); i++){
            c = this.guessedLetters.get(i).toCharArray()[0];
            for(int j = 0; j< wordArr.length; j++)
                if (c ==wordArr[j])
                    theHiddenWord[j] = c;
        }
        StringBuilder sb = new StringBuilder();
        for (char ch : this.theHiddenWord) sb.append(" " + ch);
        String text = sb.toString();
        return text;
    }
}
