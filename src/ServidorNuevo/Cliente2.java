package ServidorNuevo;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Scanner;

public class Cliente2 {
    private static KeyPair par=FirmaDigital.generarparKeys();
    private static PublicKey destino = ThreadParaCliente.exponerPublic();
    public static String enviarMensaje(String mensaje, PublicKey destino, PrivateKey yo)
    {
        try {
            String laMensajeada=FirmaDigital.cifradoPublic(mensaje, destino);
            String superMensaje = laMensajeada +"|"+ FirmaDigital.encriptarYHashear(mensaje,yo);
            return superMensaje;
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
    public static String recibirMensaje(String mensaje, PublicKey publico, PrivateKey privado){
        try {
            String[] ambosMensajes=mensaje.split("|");
            String mensaje1=FirmaDigital.descifradoPrivate(ambosMensajes[0], privado);
            String hash=FirmaDigital.descifradoPublic(ambosMensajes[1], publico);
            String mensaje1Hasheado=FirmaDigital.hashear(mensaje1);
            if(hash.equals(mensaje1Hasheado)){
                return mensaje1;
            }
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return "NO SE PUEDE CONFIRMAR EL ORIGEN DEL MENSAJERO. POR LO TANTO, NO VA A RECIBIR EL MENSAJE GRACIAs";
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese el puerto del servidor: ");
        int serverPort = Integer.parseInt(scanner.nextLine());

        System.out.print("Ingrese su nombre: ");
        String clientName = scanner.nextLine();

        System.out.print("Ingrese el tópico (TOPICA, TOPICB, TOPICC, TEATRO): ");
        String topicStr = scanner.nextLine();

        System.out.println("Mensaje de Saludo");
        String mensajeDeSaludo = scanner.nextLine();

        try (
                Socket socket = new Socket("localhost", serverPort);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner consoleScanner = new Scanner(System.in);

        ) {
            System.out.println(in.readLine());

            out.println(clientName);
            System.out.println(in.readLine());

            out.println(topicStr);
            out.println(FirmaDigital.publicaABase64(par.getPublic()));

            System.out.println("Conexión establecida. Puedes empezar a chatear.");
            System.out.println("Escribe 'exit' para desconectarte.");

            Thread thread = new Thread(() -> {
                try {


                    while (true) {
                        String message = in.readLine();

                        if (message != null) {
                            String mensajeRecibido=Cliente.recibirMensaje(message,destino, par.getPrivate());
                            if(mensajeRecibido.equals("exit")){
                                throw new Exception("chau");
                            }
                            else{System.out.println(mensajeRecibido);}
                        } else{

                        }
                    }
                }
                catch(Exception e){
                    System.out.println("Saliendo");
                }
            });
            thread.start();
            //este while ignora el mensaje exit de los otros usuarios
            while (true) {
                String message = consoleScanner.nextLine();
                String nuevoMensaje = enviarMensaje(message,destino,par.getPrivate());
                out.println(nuevoMensaje);
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