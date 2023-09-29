package ServidorNuevo;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;


public class ThreadParaCliente extends Thread{
    private Socket clienteSocket;
    private static final int PORT = 12345;
    private static String pass = "Ao3T4h4GGvs4Q28Q";
    private static String salt = "aCo3355";
    private static SecretKey claveSimetrica;

    static {
        try {
            claveSimetrica = FirmaDigital.getKeyFromPassword(pass,salt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private static ArrayList<ThreadParaCliente> clients = new ArrayList<>();
    private static final KeyPair par=FirmaDigital.generarparKeys();
    private Topicos topico;
    private String nombreCliente;
    private BufferedReader in;
    private PrintWriter out;
    private PublicKey clientePublic;
    private ArrayList<ThreadParaCliente> clientes;

    public ThreadParaCliente(Socket socket, ArrayList<ThreadParaCliente> clientes) throws IOException {
        this.clienteSocket = socket;
        this.clientes = clientes;
        in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
        out = new PrintWriter(clienteSocket.getOutputStream(), true);
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
    public static String recibirMensajeSimetrico(String mensaje, PublicKey publico, SecretKey clave){
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
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeyException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
        return "NO SE PUEDE CONFIRMAR EL ORIGEN DEL MENSAJERO. POR LO TANTO, NO VA A RECIBIR EL MENSAJE GRACIAs";
    }
    @Override
    public void run() {
        try {
            clientePublic=FirmaDigital.base64APublica(in.readLine());
            out.println(FirmaDigital.publicaABase64(par.getPublic()));
            out.println(enviarMensaje(pass,clientePublic, par.getPrivate()));
            out.println(enviarMensaje(salt,clientePublic, par.getPrivate()));
            nombreCliente = in.readLine();
            String topicoStr = in.readLine();
            topico = Topicos.valueOf(topicoStr);
            String h="Conexión establecida. Puedes empezar a chatear.";
            out.println(enviarMensajeSimetrico(h,claveSimetrica,par.getPrivate()));


            for (ThreadParaCliente cliente : clientes) {
                if (cliente != this && cliente.topico == topico) {
                    String enLinea=cliente.nombreCliente + " está en línea.";
                    String mensajeEncriptar= ThreadParaCliente.enviarMensajeSimetrico(enLinea,claveSimetrica, par.getPrivate());
                    cliente.out.println(mensajeEncriptar);
                }
            }


            while (true) {
                String mensaje = in.readLine();

                String mensajeRecibido = nombreCliente + ": " + ThreadParaCliente.recibirMensajeSimetrico(mensaje,clientePublic,claveSimetrica);


                if (mensaje.equalsIgnoreCase("exit")) {
                    break;
                }

                for (ThreadParaCliente cliente : clientes) {
                    if (cliente != this && cliente.topico == topico) {
                        String mensajeEncriptar= ThreadParaCliente.enviarMensajeSimetrico(mensajeRecibido, claveSimetrica, par.getPrivate());
                            cliente.out.println(mensajeEncriptar);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("se fue");
        } finally {
            try {
                in.close();
                out.close();
                clienteSocket.close();
                synchronized (clientes) {
                    clientes.remove(this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
