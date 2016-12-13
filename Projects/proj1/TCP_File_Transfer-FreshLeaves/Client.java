import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Represents a Client used to connect to a server. A client must provide a server IP and port in which to connect
 * to.
 * @author Douglas Money
 * @author Hayden Miedema
 */
public class Client {

    private static Socket socket;
    private static String fileName="";
    private static BufferedReader stdin;
    private static PrintStream os;
    private static PrintStream is;

    /**
     *Runs Client
     *
     **/
    public static void main(String[] args) throws IOException {

        Scanner in = new Scanner(System.in);

        System.out.print("Enter Server IP: ");
        String IP = in.nextLine(); //user sets IP

        System.out.print("Enter port number: ");
        int port = in.nextInt(); //user sets Port

        try {
            socket = new Socket(IP, port); //set up socket with user defined IP and Port
            stdin = new BufferedReader(new InputStreamReader(System.in)); //standard input
        } catch (Exception e) {
            System.err.println("Cannot connect to the server, try again later.");
            System.exit(1);
        }

        os = new PrintStream(socket.getOutputStream());

        /* Keep client running until user types quit or terminates session*/
        while(true) {
            try {
                System.out.print("Enter file name or type 'quit' to close: ");
                fileName = stdin.readLine();
                if(fileName.equals("quit")) { //this ends session and jumps to close socket
                    os.println(fileName);
                    break;
                }
                os.println(fileName); // send fileName to output stream
                fileIn(fileName);//calls method to receive file
            } catch (Exception e) {
                System.err.println("not valid input");
            }
        }

        System.err.println("Disconnected From Server");
        socket.close();
    }


/**
 * Receives and writes file from server to disk
 *
 * */
    public static void fileIn(String fileName) {
        try {
            int bytesRead;
            InputStream in = socket.getInputStream();
            DataInputStream dis = new DataInputStream(in);
            fileName = dis.readUTF();
            OutputStream output = new FileOutputStream(("_" + fileName));
            long size = dis.readLong();
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                output.write(buffer, 0, bytesRead);
                size -= bytesRead;
            }
            System.out.println(fileName+" received from Server.");
        } catch (FileNotFoundException fnf){
            System.err.println("file was not recieved");
        } catch (IOException ex) {
            System.err.println("file could not be received");
        }
    }
}