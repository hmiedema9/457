/* TCP Client Class
 * This class is paired with the TCP Server class in order
 * to transfer files from the server to the client.
 * By: Hayden Miedema and Doug Money
*/

import java.io.*;
import java.io.ByteArrayOutputStream;
import java.net.*;
import java.util.Scanner;

class TCPClient {

    private static final String fileOutput = "testoutput";

    public static void main(String args[]) {
        byte[] aByte = new byte[1];
        int bytesRead;
	String thisLine = null;
        Socket clientSocket = null;
        InputStream is = null;
        
        
        Scanner in = new Scanner(System.in);
        //Ask for IP address to connect with
        System.out.println("Enter server IP address");
        String serverIP = in.next();
         //Ask for port to connect with
	System.out.println("Enter server port number");
        int serverPort = in.nextInt();

        try {
            clientSocket = new Socket( serverIP , serverPort );
	    DataOutputStream outToServer =  new DataOutputStream(clientSocket.getOutputStream());
	    BufferedReader inFromServer = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));	
	    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	    System.out.println("Connection established with server on port number " + serverPort);
	    //Enter name of the requested file
	    System.out.println("Enter Filename ");
	    String message = inFromUser.readLine();
	    outToServer.writeBytes(message+"\n");
	    System.out.println("Request Sent");
            is = clientSocket.getInputStream();
        } catch (IOException ex) {
            // Do exception handling
            ex.printStackTrace();
        }
	
	// Creates byte array output stream to write 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (is != null) {

            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            try {
                fos = new FileOutputStream( fileOutput );
                bos = new BufferedOutputStream(fos);
                bytesRead = is.read(aByte, 0, aByte.length);

                do {
		  baos.write(aByte);
                  bytesRead = is.read(aByte);
                } while (bytesRead != -1);

                bos.write(baos.toByteArray());
                bos.flush();
                bos.close();
                System.out.println("File received, hooray!");
                clientSocket.close();
            } catch (IOException ex) {
                // Do exception handling
                ex.printStackTrace();
            }
        }
    }
}
