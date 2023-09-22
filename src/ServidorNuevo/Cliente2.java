package ServidorNuevo;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.*;
import java.util.Scanner;

public class Cliente2 {
    private static final KeyPair par=FirmaDigital.generarparKeys();
    private PublicKey destino;
    private SecretKey claveSimetrica;
    public PublicKey getDestino() {
        return destino;
    }
    public static String enviarMensajeSimetrico(String mensaje, SecretKey clave, PrivateKey yo){
        try {
            String laMensajeada=FirmaDigital.cifrarSimetrica(mensaje, clave);
            return laMensajeada +"¬"+ FirmaDigital.encriptarYHashear(mensaje,yo);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }
    public static String recibirMensaje(String mensaje, PublicKey publico, SecretKey clave){
        try {
            String[] ambosMensajes=mensaje.split("¬");
            String mensaje1=FirmaDigital.descifrarSimetrica(ambosMensajes[0], clave);
            String hash=FirmaDigital.descifradoPublic(ambosMensajes[1], publico);
            String mensaje1Hasheado=FirmaDigital.hashear(mensaje1);
            if(hash.equals(mensaje1Hasheado)){
                return mensaje1;
            }
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeyException |
                 BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
        return "NO SE PUEDE CONFIRMAR EL ORIGEN DEL MENSAJERO. POR LO TANTO, NO VA A RECIBIR EL MENSAJE GRACIAs";
    }
    public static String enviarMensaje(String mensaje, PublicKey destino, PrivateKey yo)
    {
        try {
            String laMensajeada=FirmaDigital.cifradoPublic(mensaje, destino);
            return laMensajeada +"¬"+ FirmaDigital.encriptarYHashear(mensaje,yo);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static String recibirMensaje(String mensaje, PublicKey publico, PrivateKey privado){
        try {
            String[] ambosMensajes=mensaje.split("¬");
            String mensaje1=FirmaDigital.descifradoPrivate(ambosMensajes[0], privado);
            String hash=FirmaDigital.descifradoPublic(ambosMensajes[1], publico);
            String mensaje1Hasheado=FirmaDigital.hashear(mensaje1);
            if(hash.equals(mensaje1Hasheado)){
                return mensaje1;
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return "NO SE PUEDE CONFIRMAR EL ORIGEN DEL MENSAJERO. POR LO TANTO, NO VA A RECIBIR EL MENSAJE GRACIAs";
    }
    public static void main(String[] args){
        Cliente2 cliente1=new Cliente2();
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
                Scanner consoleScanner = new Scanner(System.in)

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
                            String mensajeRecibido=Cliente2.recibirMensaje(message,cliente1.getDestino(), par.getPrivate());
                            if(mensajeRecibido.equalsIgnoreCase("exit")){
                                break;
                            }
                            else{System.out.println(mensajeRecibido);}
                        }else{
                            break;
                        }
                    }
                }catch (NullPointerException e)
                {
                    System.err.println(e.getMessage());
                }
                catch(Exception e){
                    System.out.println("Saliendo");
                }
            });
            thread.start();

            while (true) {

                String message = consoleScanner.next();
                String nuevoMensaje = enviarMensaje(message,cliente1.getDestino(),par.getPrivate());
                out.println(nuevoMensaje);
                if (message.equalsIgnoreCase("exit")) {
                    return;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}