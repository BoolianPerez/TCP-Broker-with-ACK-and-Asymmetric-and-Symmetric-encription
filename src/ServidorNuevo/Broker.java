package ServidorNuevo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;


public class Broker {

    private static final int PORT = 12345;
    private static ArrayList<ThreadParaCliente> clients = new ArrayList<>();

    public static ArrayList<ThreadParaCliente> getClients() {
        return clients;
    }

    public static void setClients(ArrayList<ThreadParaCliente> clients) {
        Broker.clients = clients;
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado en el puerto " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión entrante.");
                ThreadParaCliente threadParaCliente = new ThreadParaCliente(clientSocket, clients);
                clients.add(threadParaCliente);
                threadParaCliente.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
