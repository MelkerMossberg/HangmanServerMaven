package Hangman.game;

import Hangman.net.ClientHandler;

public class GameHandler {

    GameState gameState;
    ClientHandler clientHandler;
    RandomWordGenerator random;

    public GameHandler(ClientHandler clientHandler){
        this.clientHandler = clientHandler;
        random = new RandomWordGenerator();
    }

    public String handle(String userInput){
        if(userInput.length() > 1)
            return validateWordGuess(userInput);

        return validateLetterGuess(userInput);
    }

    private String validateWordGuess(String userInput) {
        if (userInput.equals(gameState.word)){
            startAnotherGame("WIN");
            return gameState.packageIntoAString();
        }else {
            startAnotherGame("LOST");
            return gameState.packageIntoAString();
        }
    }

    private String validateLetterGuess(String guess) {
        // ALREADY GUESSED THIS?
        boolean alreadyGuessedBefore = gameState.guessedLetters.contains(guess);
        if (alreadyGuessedBefore){
            return gameState.packageIntoAString();
        }
        // DOES THE WORD CONTAINS MY GUESS?
        boolean wordContainsGuess = gameState.word.contains(guess);
        // YES - IT WAS CORRECT
        if(wordContainsGuess){
            gameState.registerCorrectGuess(guess);
            // AND I WON THIS ROUND
            if (userGuessedAllLetters()){
                startAnotherGame("WIN");
                return gameState.packageIntoAString();
            }
            // BUT I LOST THE GAME
            if (toManyGuesses()){
                startAnotherGame("LOST");
                return gameState.packageIntoAString();
            }
        }
        // NO - THE GUESS WAS INCORRECT
        else{
            // AND YOU LOST THE GAME
            gameState.registerIncorrectGuess(guess);
            if (toManyGuesses()){
                startAnotherGame("LOST");
                return gameState.packageIntoAString();
            }
        }
        // STILL NOT WON OR LOST...
        gameState.state = "LETTER_GUESS";
        return gameState.packageIntoAString();
    }

    private boolean userGuessedAllLetters() {
        return gameState.numCorrectGuesses == gameState.numUniqueLetters;
    }

    private boolean toManyGuesses() {
        return gameState.livesLeft == 0;
    }

    public String startFirstGame() {
        gameState = new GameState(RandomWord(), 0, null);
        gameState.state = "LETTER_GUESS";
        return gameState.packageIntoAString();
    }
    public String startAnotherGame(String gameResult) {
        if (gameResult.equals("WIN")) gameState.gameScore++;
        else gameState.gameScore = 0;

        gameState = new GameState(RandomWord(), gameState.gameScore, gameState.word);
        gameState.state = gameResult;
        return gameState.packageIntoAString(); //TODO: Nu skickas en sträng över TCP där varje key delas upp med "-". Borde hitta alternativ.
    }

    private String RandomWord() {
        return random.pickRandomWord();
    }
}
