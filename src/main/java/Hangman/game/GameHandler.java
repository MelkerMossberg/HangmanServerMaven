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
            return gameState.packageJSON();
        }else {
            startAnotherGame("LOST");
            return gameState.packageJSON();
        }
    }

    private String validateLetterGuess(String guess) {
        // ALREADY GUESSED THIS?
        boolean alreadyGuessedBefore = gameState.guessedLetters.contains(guess);
        if (alreadyGuessedBefore){
            return gameState.packageJSON();
        }
        // DOES THE WORD CONTAINS MY GUESS?
        boolean wordContainsGuess = gameState.word.contains(guess);
        // YES - IT WAS CORRECT
        if(wordContainsGuess){
            gameState.registerCorrectGuess(guess);
            // AND I WON THIS ROUND
            if (userGuessedAllLetters()){
                startAnotherGame("WIN");
                return gameState.packageJSON();
            }
            // BUT I LOST THE GAME
            if (toManyGuesses()){
                startAnotherGame("LOST");
                return gameState.packageJSON();
            }
        }
        // NO - THE GUESS WAS INCORRECT
        else{

            gameState.registerIncorrectGuess(guess);
            // AND YOU LOST THE GAME
            if (toManyGuesses()){
                startAnotherGame("LOST");
                return gameState.packageJSON();
            }
        }
        // STILL NOT WON OR LOST...
        gameState.state = "LETTER_GUESS";
        return gameState.packageJSON();
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
        return gameState.packageJSON();
    }
    public String startAnotherGame(String gameResult) {
        if (gameResult.equals("WIN")) gameState.gameScore++;
        else gameState.gameScore = 0;

        gameState = new GameState(RandomWord(), gameState.gameScore, gameState.word);
        gameState.state = gameResult;
        return gameState.packageJSON(); //TODO: Nu skickas en sträng över TCP där varje key delas upp med "-". Borde hitta alternativ.
    }

    private String RandomWord() {
        return random.pickRandomWord();
    }
}
