package Server;


import RPC_Server.HelloService;
import RPC_Server.HelloServiceImp;

import java.io.IOException;

public class ServerStartUp {
    public static void main(String[] args){
        Server server = new Server();
        server.registry(HelloService.class.getName(), HelloServiceImp.class);
        Thread thread = new Thread(server);
        thread.start();
    }
}
