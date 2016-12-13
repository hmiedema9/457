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
	
	BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	int port = 0;
	Scanner in = new Scanner(System.in);
	//Ask for port for server to listen on
        String fileRequested = "";
	ServerSocket serverSocket = new ServerSocket();
	
	while(port == 0){
	  System.out.println("Please enter port number: ");
	  port = in.nextInt();
	}try{
	    serverSocket = new ServerSocket(port);
	}catch(Exception e){
	    System.out.println("This port is already in use!");
	    port = 0;
	  }
	
	// Start the server with the given socket and wait for requests
        while (true) {
	    System.out.println("Waiting ... ");
            Socket connectionSocket = serverSocket.accept();
            Runnable r = new ClientHandler(connectionSocket);
            Thread t = new Thread(r);
            t.start();
            //BufferedOutputStream outToClient = null;
}
}
}

class ClientHandler implements Runnable {
    Socket connectionSocket;
    ClientHandler(Socket connection){
      connectionSocket = connection;
    }

    public void run() {
    //Try establishing the connection to the server
            try {
                connectionSocket = serverSocket.accept();
                System.out.println("Connection on port " + port + " established" );
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                fileRequested = inFromClient.readLine();
                System.out.println("Received request for file: "+fileRequested); 
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
                return;
                }
                BufferedInputStream bis = new BufferedInputStream(fis);

                try {
                    bis.read(mybytearray, 0, mybytearray.length);
                    outToClient.write(mybytearray, 0, mybytearray.length);
                    outToClient.flush();
                    outToClient.close();
                    //connectionSocket.close();
                    System.out.println(fileRequested + " was sent successfully");
                    // File sent successfully
                    
                } catch (IOException ex) {
                    // Do exception handling
                    ex.printStackTrace();
                }
            }  
    }
}

/*           
class ClientHandler implements Runnable {
	Socket connectionSocket;
	ClientHandler(Socket connection)
	{
	connectionSocket = connection;
	}
	
	public void run(){
            //Try establishing the connection to the server
            try {
                connectionSocket = serverSocket.accept();
                System.out.println("Connection on port " + port + " established" );
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		fileRequested = inFromClient.readLine();
		System.out.println("Received request for file: "+fileRequested); 
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
		    return;
                }
                BufferedInputStream bis = new BufferedInputStream(fis);

                try {
                    bis.read(mybytearray, 0, mybytearray.length);
                    outToClient.write(mybytearray, 0, mybytearray.length);
                    outToClient.flush();
                    outToClient.close();
                    //connectionSocket.close();
                    System.out.println(fileRequested + " was sent successfully");
                    // File sent successfully
                    
                } catch (IOException ex) {
                    // Do exception handling
                    ex.printStackTrace();
                }
            }
 }
}
   */     
    
	
