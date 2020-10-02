package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.locks.LockSupport;

public class myMain {
    public static void main(String[] args) throws Exception {
        String current_type = args[0];

        if(current_type.equals("Broker")) {
            System.out.println("Broker started");
            myBroker myBroker = new myBroker();
            myBroker.main(args);
        } else if(current_type.equals("Server")){
            System.out.println("Server started");
            myServer myServer = new myServer();
            myServer.main(args);
        } else if(current_type.equals("Serverp")){
            System.out.println("Serverp started");
            myServerp myServerp = new myServerp();
            myServerp.main(args);
        }
        LockSupport.park();

    }
}
