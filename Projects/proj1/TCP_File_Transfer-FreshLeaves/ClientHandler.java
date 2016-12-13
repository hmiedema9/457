import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ClientHandler runs thread operations. This implementatino allows multiple clients to connect to the server
 * and sends requested files.
 *
 * @author Douglas Money
 * @author Hayden Miedema
 */
public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private BufferedReader in = null;

    /**
     * Create new client session
     * */
    public ClientHandler(Socket client) {
        this.clientSocket = client;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            String outGoingFileName;
            while ((outGoingFileName = in.readLine()) != null) {
                if(outGoingFileName.equals("quit")) {  //signs client off
                    EOFException eof = new EOFException();
                    throw eof;
                }
                sendFile(outGoingFileName);
            }
        }

        catch(EOFException eof){
            try {
                in.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.err.println("Client closed the connection.");
        }catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /****
     * gets file from server disk and sends it through data output stream for reception by client
     *
     ****/
    public void sendFile(String fileName) {
        DataOutputStream dos = null;
        try {
            File myFile = new File(fileName); //set up file
            byte[] mybytearray = new byte[(int) myFile.length()];
            FileInputStream fis = new FileInputStream(myFile); //set up streams
            BufferedInputStream bis = new BufferedInputStream(fis); //set up streams
            DataInputStream dis = new DataInputStream(bis);  //set up streams
            dis.readFully(mybytearray, 0, mybytearray.length); //read file
            OutputStream os = clientSocket.getOutputStream(); //prep socket for file share
            dos = new DataOutputStream(os);
            dos.writeUTF(myFile.getName()); //send file name
            dos.writeLong(mybytearray.length); //send size
            dos.write(mybytearray, 0, mybytearray.length); //send file
            dos.flush(); //clean stream
            System.out.println(fileName + " sent to client.");
        } catch (Exception e) {
            System.err.println("File does not exist!");
        }
    }
}