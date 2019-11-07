package Hangman.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {

    public Server(){
        try {
            ServerSocket ss = new ServerSocket(44444);
            while (true){
                Socket socket = ss.accept();
                new ClientHandler(socket, this).run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Main server shut down");
    }

}