package loHizobel;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client2 {
        public static void main(String[] args) {
            String nombre;
            String respuesta;
            Topicosli topico;
            Scanner sc = new Scanner(System.in);
            System.out.println("Ingresa tu nombre");
            respuesta = sc.nextLine();
            nombre = respuesta;
            System.out.println("Ingresa el topico");
            topico = Topicosli.valueOf(sc.nextLine().toUpperCase());

            try (Socket socket = new Socket("localhost", 5000)) {
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                ThreadCliente threadCliente = new ThreadCliente(socket);
                new Thread(threadCliente).start();

                output.println(respuesta + " se unió al servidor.");
                do {
                    System.out.println("te lo ruego dame un mensaje");
                    String mensaje = (nombre + " : ");
                    respuesta = sc.nextLine();
                    if (respuesta.equals("salir")) {
                        output.println("salir");
                        break;
                    }
                    output.println(mensaje + respuesta);
                } while (!respuesta.equals("salir"));
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
        }
    }
