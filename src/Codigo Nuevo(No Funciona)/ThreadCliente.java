package loHizobel;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

    public class ThreadCliente implements Runnable {

        private Socket socket;
        private BufferedReader cin;

        public ThreadCliente(Socket socket) throws IOException {
            this.socket = socket;
            this.cin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String message = cin.readLine();
                    System.out.println(message);
                }
            } catch (SocketException e) {
                System.out.println("Saliste del Chat");
            } catch (IOException exception) {
                System.out.println(exception);
            } finally {
                try {
                    cin.close();
                } catch (Exception exception) {
                    System.out.println(exception);
                }
            }
        }
    }
