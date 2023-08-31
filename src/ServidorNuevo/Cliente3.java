package ServidorNuevo;

import ServidorNuevo.Topicos;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente3 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese el puerto del servidor: ");
        int serverPort = Integer.parseInt(scanner.nextLine());

        System.out.print("Ingrese su nombre: ");
        String clientName = scanner.nextLine();

        System.out.print("Ingrese el tópico (TOPICA, TOPICB, TOPICC, TEATRO): ");
        String topicStr = scanner.nextLine();
        Topicos topico = Topicos.valueOf(topicStr);

        try (
                Socket socket = new Socket("localhost", serverPort);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner consoleScanner = new Scanner(System.in)
        ) {
            System.out.println(in.readLine()); // Bienvenida del servidor

            out.println(clientName);
            System.out.println(in.readLine()); // Elegir tópico

            out.println(topicStr);

            System.out.println("Conexión establecida. Puedes empezar a chatear.");
            System.out.println("Escribe 'exit' para desconectarte.");

            Thread thread = new Thread(() -> {
                try {
                    while (true) {
                        String message = in.readLine();
                        if (message != null) {
                            System.out.println(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();

            while (true) {
                String message = consoleScanner.nextLine();
                out.println(message);
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
            }

            thread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}