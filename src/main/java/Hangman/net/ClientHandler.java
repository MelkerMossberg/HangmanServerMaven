package Hangman.net;

import Hangman.database.Database;
import Hangman.game.GameHandler;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.security.Key;


public class ClientHandler extends Thread {

    Socket socket;
    Server server;
    BufferedReader din;
    PrintWriter dout;
    GameHandler gameHandler;
    static volatile boolean shouldRun;
    Key key;
    Database db;
    JSONParser jsonParser;


    public ClientHandler(Socket socket, Server server){
        super("ClientHandlerThread");
        this.socket = socket;
        this.server = server;
        shouldRun = true;
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        this.jsonParser = new JSONParser();
        this.db = new Database();
        this.gameHandler = new GameHandler(this);
        try{
            din = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dout = new PrintWriter(socket.getOutputStream(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        String jwt = login();

        String newGameState = gameHandler.startFirstGame();
        publish("game",newGameState);
        while (shouldRun){
            String input = listenForResponse();
            verifyJWT(jwt);
            String inputBody = parseInput("body", input);
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

    private void verifyJWT(String jwt) {
        try {
            /**
            / Testing with a faulty jwt token and a fauly key
            **/
            //Jwts.parser().setSigningKey(key).parseClaimsJws("eyjhbgcioijiuzi1nij9.eyjzdwiioijuzxn0in0.2ujspx3d7q41t5mvcuqlpnmdeyci77abyhjbt_qoxu4");
            //Key fakeKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            //Jwts.parser().setSigningKey(fakeKey).parseClaimsJws(jwt);

            Jwts.parser().setSigningKey(key).parseClaimsJws(jwt);

        } catch (JwtException e) {
            e.printStackTrace();

            closeConnection();
            //don't trust the JWT!
        }
    }

    private String parseInput(String selector, String input) {
        Object JSONInput = null;
        try {
            JSONInput = jsonParser.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject message = (JSONObject) JSONInput;
        return message.get(selector).toString();
    }

    private String login() {
        publish("login","Username:");
        String username = parseInput("body",listenForResponse());
        publish("login","Password:");
        String password = parseInput("body",listenForResponse());
        String jwt = verifyLogin(username, password);
        if (jwt.equals("wrong")){
            publish("login","Wrong username and password combo. Try again");
            login();
        }
        publish("loginSuccess", jwt);
        return jwt;
    }

    private String verifyLogin(String username, String password) {
        if (username.equals(db.getUsername()) && password.equals(db.getPassword())){
            try {
                String jws = Jwts.builder().setSubject(username).signWith(key).compact();
                return jws;

            } catch (JwtException e) {
                e.printStackTrace();
            }
        }
        return "wrong";
    }

    private String listenForResponse() {
        try {
            String response;
            while(shouldRun){
                if ((response = din.readLine()) != null){
                    System.out.println("Handler# "+this.getId()+" received: " + response);
                    return response.toLowerCase();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        };
        return "ListenForResponse is not working if we end up here...";
    }

    private void publish(String state, String body) {
        JSONObject message = new JSONObject();
        message.put("state", state);
        message.put("body", body);
        int contentLength = measureStringByteLength(body);
        message.put("content-length", Integer.toString(contentLength));
        dout.println(message.toJSONString());
        dout.flush();
    }

    private void closeConnection(){
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
