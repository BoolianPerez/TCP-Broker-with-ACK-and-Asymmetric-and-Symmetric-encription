package ServidorNuevo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ThreadCliente extends Thread{
    private Socket clientSocket;
    private Topicos topic;
    private String clientName;
    private BufferedReader in;
    private PrintWriter out;
    private List<ThreadCliente> clients;

    public ThreadCliente(Socket socket, List<ThreadCliente> clients) throws IOException {
        this.clientSocket = socket;
        this.clients = clients;
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            out.println("Bienvenido al servidor de chat. Por favor, elige un nombre:");
            clientName = in.readLine();
            out.println("Elige un tópico (TOPICA, TOPICB, TOPICC, TEATRO):");
            String topicStr = in.readLine();
            topic = Topicos.valueOf(topicStr);

            out.println("Conexión establecida. Puedes empezar a chatear.");

            synchronized (clients) {
                for (ThreadCliente client : clients) {
                    if (client != this && client.topic == topic) {
                        out.println(client.clientName + " está en línea.");
                    }
                }
            }

            while (true) {
                String message = in.readLine();
                if (message == null) {
                    break;
                }

                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                synchronized (clients) {
                    for (ThreadCliente client : clients) {
                        if (client != this && client.topic == topic) {
                            client.out.println(clientName + ": " + message);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
                synchronized (clients) {
                    clients.remove(this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
