package ServidorNuevo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Broker {
    private static final int PORT = 12345;
    private static List<ThreadCliente> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado en el puerto " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión entrante.");

                ThreadCliente ThreadCliente = new ThreadCliente(clientSocket, clients);
                clients.add(ThreadCliente);
                ThreadCliente.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
