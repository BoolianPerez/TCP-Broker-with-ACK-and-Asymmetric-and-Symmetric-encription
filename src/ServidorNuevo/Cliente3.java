package ServidorNuevo;

import ServidorNuevo.Topicos;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Scanner;

public class Cliente3 {
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

        try (
                Socket socket = new Socket("localhost", serverPort);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner consoleScanner = new Scanner(System.in)
        ) {
            System.out.println(in.readLine());
            out.println(clientName);
            System.out.println(in.readLine());
            out.println(topicStr);
            System.out.println("Conexión establecida");
            System.out.println("Escribí 'exit' para desconectarte.");

            Thread thread = new Thread(() -> {
                try {
                    while (true) {
                        String message = in.readLine();
                        String mensajeRecibido=Cliente.recibirMensaje(message,destino, par.getPrivate());
                        if (message != null) {
                            System.out.println(mensajeRecibido);
                        } else if (message.equals("exit")) {

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }catch(NullPointerException e){
                    System.out.println("Saliendo");
                }
            });
            thread.start();


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