package Hangman.net;

import Hangman.game.GameHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;


public class ClientHandler extends Thread {

    Socket socket;
    Server server;
    BufferedReader din;
    PrintWriter dout;
    GameHandler gameHandler;
    AuthHandler authHandler;
    static volatile boolean shouldRun;
    JSONParser jsonParser;


    public ClientHandler(Socket socket, Server server){
        super("ClientHandlerThread");
        this.socket = socket;
        this.server = server;
        shouldRun = true;
       // this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        this.jsonParser = new JSONParser();
        this.gameHandler = new GameHandler(this);
        this.authHandler = new AuthHandler(this);
        try{
            din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dout = new PrintWriter(socket.getOutputStream(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        authHandler.login();
        String newGameState = gameHandler.startFirstGame();
        publish("game",newGameState);
        while (shouldRun){
            String input = listenForResponse();
            String inputJWT = parseInput("jwt", input);
            authHandler.verifyJWT(inputJWT);
            String inputBody = parseInput("body", input).toLowerCase();
            int contentLength = Integer.parseInt(parseInput("content-length", input));
            controlLength(inputBody, contentLength);
            if (inputBody.equals("quit")) closeConnection();
            else {
                String output = gameHandler.handle(inputBody);
                publish("game",output);
            }
        }
    }

    private boolean controlLength(String inputBody, int promisedLength) {
        int measuredLength = measureStringByteLength(inputBody);
        System.out.println("Measured: "+ measuredLength + ", Promised: " + promisedLength);
        if (measuredLength == promisedLength)
            return true;
        else return false;
    }

    private int measureStringByteLength(String inputBody) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(inputBody);
            objectOutputStream.flush();
            objectOutputStream.close();
            int length = byteArrayOutputStream.toByteArray().length;
            return length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    String parseInput(String selector, String input) {
        Object JSONInput = null;
        try {
            JSONInput = jsonParser.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject message = (JSONObject) JSONInput;
        return message.get(selector).toString();
    }

    String listenForResponse() {
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

    void publish(String state, String body) {
        JSONObject message = new JSONObject();
        message.put("state", state);
        message.put("body", body);
        int contentLength = measureStringByteLength(body);
        message.put("content-length", Integer.toString(contentLength));
        dout.println(message.toJSONString());
        dout.flush();
    }

    void closeConnection(){
        publish("quit", null);
        System.out.println("Closing thread #" + this.getId());
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
