package Hangman.net;

import Hangman.game.GameHandler;
import java.io.*;
import java.net.Socket;


public class ClientHandler extends Thread {

    Socket socket;
    Server server;
    BufferedReader din;
    PrintWriter dout;
    GameHandler gameHandler;
    static volatile boolean shouldRun;


    public ClientHandler(Socket socket, Server server){
        super("ClientHandlerThread");
        this.socket = socket;
        this.server = server;
        shouldRun = true;
        this.gameHandler = new GameHandler(this);
        try{
            din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dout = new PrintWriter(socket.getOutputStream(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        String newGame = gameHandler.startFirstGame();
        publish(newGame);
        while (shouldRun){
            String input = listenForResponse().toLowerCase();
            if (input.equals("quit")) closeConnection();
            else {
                String output = gameHandler.handle(input);
                publish(output);
            }
        }
    }

    private String listenForResponse() {
        try {
            String response;
            while(shouldRun){
                if ((response = din.readLine()) != null){
                    System.out.println("Handler# "+this.getId()+" received: " + response);
                    return response;
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        };
        return "ListenForResponse is not working if we end up here...";
    }

    private void publish(String body) {
        dout.println(body);
        dout.flush();
    }

    private void closeConnection(){
        publish("quit");
        try {
            din.close();
            dout.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        shouldRun = false;
    }

}
