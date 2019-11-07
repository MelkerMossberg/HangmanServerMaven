package Hangman.game;

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
        System.out.print("Attemps left: " + livesLeft);
        this.lastGuessWasCorrect = false;
    }

    public String packageIntoAString() {
        StringBuilder sb = new StringBuilder();
        String prevGuesses = guessedLetters.toString();
        String hiddenWord = buildHiddenWord();
        return state + "-" + gameScore + "-" + livesLeft  + "-"+ numCorrectGuesses + "-"+ prevGuesses +"-" +lastGuessWasCorrect +"-"+ previousWord + "-" + hiddenWord;
    }

    public String writeJSON(){
        return "";
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
