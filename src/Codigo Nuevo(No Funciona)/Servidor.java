package loHizobel;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Servidor {

    public static void main(String[] args) {
        HashMap<Socket,Topicosli> clientes = new HashMap<>();
        HashMap<Socket, String> nombresClientes = new HashMap();
        try (ServerSocket serversocket = new ServerSocket(5000)) {
            System.out.println("Encendido");
            while (true) {
                Socket socket = serversocket.accept();

                Topicosli topico = null;
                Scanner input = new Scanner(System.in);
                topico = Topicosli.valueOf(input.next().toUpperCase());
                ThreadServer threadServer = new ThreadServer(socket, clientes, nombresClientes);
                threadServer.start();
                clientes.put(socket,topico);
                threadServer.join();
            }

        } catch (IOException | InterruptedException e) {
            System.out.println(e);
        }


    }
}