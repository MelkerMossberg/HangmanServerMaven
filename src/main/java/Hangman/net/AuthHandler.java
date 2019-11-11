package Hangman.net;

import Hangman.database.Database;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

public class AuthHandler {

    ClientHandler cc;
    Key key;
    Database db;

    AuthHandler(ClientHandler clientHandler){
        this.cc = clientHandler;
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        this.db = new Database();
    }

    void login() {
        String jwt = askForCredentials();
        while (jwt.equals("wrong")){
            cc.publish("login","Wrong username and password combo. Try again");
            jwt = askForCredentials();
        }
        cc.publish("loginSuccess", jwt);
    }

    private String askForCredentials() {
        cc.publish("login","Username: ");
        String username = cc.parseInput("body",cc.listenForResponse());
        cc.publish("login","Password: ");
        String password = cc.parseInput("body",cc.listenForResponse());
        return verifyLogin(username, password);
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

    void verifyJWT(String jwt) {
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(jwt);

            /**
             / Testing with a faulty jwt token and a faulty key
             **/
            //Jwts.parser().setSigningKey(key).parseClaimsJws("eyjhbgcioijiuzi1nij9.eyjzdwiioijuzxn0in0.2ujspx3d7q41t5mvcuqlpnmdeyci77abyhjbt_qoxu4");
            //Key fakeKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            //Jwts.parser().setSigningKey(fakeKey).parseClaimsJws(jwt);
        } catch (JwtException e) {
            // Close connection if JWT exception is thrown
            e.printStackTrace();
            cc.closeConnection();
        }
    }
}
