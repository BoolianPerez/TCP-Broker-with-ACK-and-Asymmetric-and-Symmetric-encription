package ServidorNuevo;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;


public class ThreadParaCliente extends Thread{
    private Socket clienteSocket;
    private static KeyPair par=FirmaDigital.generarparKeys();
    private Topicos topico;
    private String nombreCliente;
    private BufferedReader in;
    private PrintWriter out;
    private PublicKey clientePublic;
    private ArrayList<ThreadParaCliente> clientes;
    public static PublicKey exponerPublic()
    {
        return par.getPublic();
    }

    public ThreadParaCliente(Socket socket, ArrayList<ThreadParaCliente> clientes) throws IOException {
        this.clienteSocket = socket;
        this.clientes = clientes;
        in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
        out = new PrintWriter(clienteSocket.getOutputStream(), true);
    }
    public static String enviarMensaje(String mensaje, PublicKey destino, PrivateKey yo)
    {
        try {
            String laMensajeada=FirmaDigital.cifradoPublic(mensaje, destino);
            String superMensaje = laMensajeada +"|||"+ FirmaDigital.encriptarYHashear(mensaje,yo);
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
    @Override
    public void run() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            out.println("Bienvenido al servidor de chat. Por favor, elige un nombre:");
            nombreCliente = in.readLine();
            out.println("Elige un tópico (TOPICA, TOPICB, TOPICC, TEATRO):");
            String topicoStr = in.readLine();
            topico = Topicos.valueOf(topicoStr);
            clientePublic=FirmaDigital.base64APublica(in.readLine());
            out.println("Conexión establecida. Puedes empezar a chatear.");


            for (ThreadParaCliente cliente : clientes) {
                if (cliente != this && cliente.topico == topico) {
                        out.println(cliente.nombreCliente + " está en línea.");
                }
            }


            while (true) {
                String mensaje = in.readLine();
                mensaje=nombreCliente + ": " + mensaje;
                String mensajeRecibido = ThreadParaCliente.recibirMensaje(mensaje,clientePublic,par.getPrivate());
                if (mensaje == null) {
                    break;
                }

                if (mensaje.equalsIgnoreCase("exit")) {
                    break;
                }

                for (ThreadParaCliente cliente : clientes) {
                    if (cliente != this && cliente.topico == topico) {
                        String mensajeEncriptar= ThreadParaCliente.enviarMensaje(mensajeRecibido,cliente.clientePublic, par.getPrivate());
                            cliente.out.println(mensajeEncriptar);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
}
