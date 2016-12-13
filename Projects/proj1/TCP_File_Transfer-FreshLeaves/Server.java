import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * This class represents a Server that is used to communicate with a client class. Many clients can connect
 * to a server.
 * @author Douglas Money
 * @author Hayden Miedema
 */
public class Server {

    private static ServerSocket serverSocket;
    private static Socket clientSocket = null;

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.print("Enter port number: ");
        int port = in.nextInt();//user sets port

        try {
            serverSocket = new ServerSocket(port);//set up server socket at user defined port
            System.out.println("Server started waiting for connections....");
        } catch (Exception e) {
            System.err.println("Port already in use or is invalid.");
            System.exit(1);
        }

        /*Listen for client connections*/
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                System.out.println("Accepted connection : " + clientSocket.getLocalSocketAddress());
                Thread t = new Thread(new ClientHandler(clientSocket));//set up new thread per client connection
                t.start();
            } catch (Exception e) {
                System.err.println("Error connecting");
            }
        }
    }
}