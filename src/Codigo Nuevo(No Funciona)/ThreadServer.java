package loHizobel;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ThreadServer extends Thread {

    private Socket socket;
    private HashMap<Socket, Topicosli> clientes;
    private HashMap<Socket, String> nombresClientes;

    public ThreadServer(Socket socket, HashMap<Socket,Topicosli> clientes, HashMap<Socket, String> nombresClientes) {
        this.socket = socket;
        this.clientes = clientes;
        this.nombresClientes = nombresClientes;
    }


    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String outputString = input.readLine();
            while (true) {
                if (outputString.equals("salir")) {
                    throw new SocketException();
                }
                Topicosli top = null;
                for (Map.Entry<Socket,Topicosli>aux:this.clientes.entrySet()){
                    if (aux.getKey().equals(socket))
                    {
                        top = aux.getValue();
                    }
                }
                if (!nombresClientes.containsKey(socket)) {
                    String[] messageString = outputString.split(":", 2);
                    nombresClientes.put(socket, messageString[0]);
                    System.out.println(messageString[0] + messageString[1]);


                    mandarMensajeALosCLientes(socket,top,messageString[0] + messageString[1]);
                } else {
                    System.out.println(outputString);
                    mandarMensajeALosCLientes(socket,top,outputString);
                }
            }
        } catch (SocketException e) {
            String printMessage = nombresClientes.get(socket) + " salio del servidor";
            System.out.println(printMessage);
            mandarMensajeALosCLientes(socket,Topicosli.BROADCAST,printMessage);
            clientes.remove(socket);
            nombresClientes.remove(socket);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    private void mandarMensajeALosCLientes(Socket emisor,Topicosli topico, String outputString) {
        Socket socket;
        PrintWriter printWriter;
//        int i=0;
        for (Map.Entry<Socket,Topicosli>aux:clientes.entrySet()) {
            socket = aux.getKey();
            try {
                if (socket != emisor && (aux.getValue().equals(topico) || topico.equals(Topicosli.BROADCAST))) {
                    printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println(outputString);
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }/*
        while (i < clientes.size()) {
            socket = clientes.get(i);
            i++;
            try {
                if (socket != emisor) {
                    printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println(outputString);
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }

        }
                    */

        }
    }
