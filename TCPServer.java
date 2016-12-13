/* TCP Server Class
 * This class is paired with the TCP Client class in order
 * to transfer files from the server to the client.
 * By: Hayden Miedema and Doug Money
*/

import java.io.*;
import java.net.*;
import java.util.Scanner;

class TCPServer{

	private static final String fileToSend = "file1.txt";
	
	public static void main(String args[]) throws Exception {
		
	Scanner in = new Scanner(System.in);
	//Ask for port for server to listen on
	System.out.println("Enter port number");
        int port = in.nextInt();
        String fileRequested = "";
	ServerSocket serverSocket = new ServerSocket(port);
	
	// Start the server with the given socket and wait for requests
        while (true) {
	    System.out.println("Waiting ... ");
            Socket connectionSocket = null;
            BufferedOutputStream outToClient = null;

            //Try establishing the connection to the server
            try {
                connectionSocket = serverSocket.accept();
                System.out.println("Connection on port " + port + " established" );
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		fileRequested = inFromClient.readLine();
		System.out.println("The client said: "+fileRequested); 
                outToClient = new BufferedOutputStream(connectionSocket.getOutputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (outToClient != null) {
                File myFile = new File( fileRequested );
                byte[] mybytearray = new byte[(int) myFile.length()];

                FileInputStream fis = null;

                try {
                    fis = new FileInputStream(myFile);
                } catch (FileNotFoundException ex) {
		    // Tell the client that their file could not be found
		    System.out.println("The file " + myFile.getPath() + " was not found.");  
                }
                BufferedInputStream bis = new BufferedInputStream(fis);

                try {
                    bis.read(mybytearray, 0, mybytearray.length);
                    outToClient.write(mybytearray, 0, mybytearray.length);
                    outToClient.flush();
                    outToClient.close();
                    connectionSocket.close();
                    // File sent successfully
                    return;
                } catch (IOException ex) {
                    // Do exception handling
                    ex.printStackTrace();
                }
            }
        }
    }
	
	}
