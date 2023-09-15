package ServidorNuevo;

import jdk.nashorn.internal.runtime.ECMAException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Scanner;

public class Cliente3 {
    private static KeyPair par=FirmaDigital.generarparKeys();
    private PublicKey destino;
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
    public static String[] separarString(String mensaje, char parametro){
        String[] aux = new String[2];
        String construir="";

        for (int i=0;i<mensaje.length();i++){
            if(mensaje.charAt(i)!=parametro){
                construir=construir+mensaje.charAt(i);
            } else if (mensaje.charAt(i)==parametro) {
                aux[0]=construir;
                construir="";
            }
        }
        aux[1]=construir;
        return aux;
    }
    public static String recibirMensaje(String mensaje, PublicKey publico, PrivateKey privado){
        try {
            String[] ambosMensajes=separarString(mensaje,'|');
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
    public static void main(String[] args) throws IOException {
        Cliente3 cliente1=new Cliente3();
        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese el puerto del servidor: ");
        int puertoServer = Integer.parseInt(scanner.nextLine());

        System.out.print("Ingrese su nombre: ");
        String nombreCliente = scanner.nextLine();

        System.out.print("Ingrese el tópico (TOPICA, TOPICB, TOPICC, TEATRO): ");
        String topicStr = scanner.nextLine();



        try (
                Socket socket = new Socket("localhost", puertoServer);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner consoleScanner = new Scanner(System.in);

        ) {
            out.println(FirmaDigital.publicaABase64(par.getPublic()));
            cliente1.destino=FirmaDigital.base64APublica(in.readLine());
            System.out.println(recibirMensaje(in.readLine(),cliente1.destino, par.getPrivate()));
            out.println(nombreCliente);
            System.out.println(recibirMensaje(in.readLine(),cliente1.destino, par.getPrivate()));
            out.println(topicStr);



            System.out.println("Conexión establecida. Puedes empezar a chatear.");
            System.out.println("Escribe 'exit' para desconectarte.");
            Thread thread = new Thread(() -> {
                try {


                    while (true) {

                        String message = in.readLine();

                        if (message != null) {
                            String mensajeRecibido=Cliente.recibirMensaje(message,cliente1.destino, par.getPrivate());
                            if(mensajeRecibido.equals("exit")){
                                throw new Exception("chau");
                            }
                            else{System.out.println(mensajeRecibido);}
                        }
                    }
                }
                catch(Exception e){
                    System.out.println("Saliendo");
                }
            });
            thread.start();

            while (true) {
                String message = consoleScanner.next();
                String nuevoMensaje = enviarMensaje(message,cliente1.destino,par.getPrivate());
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