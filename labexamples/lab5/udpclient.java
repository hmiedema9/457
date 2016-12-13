/* UDP Client Class
 * This class is paired with the UDP Server class in order
 * to transfer files from the server to the client.
 * By: Hayden Miedema
*/

import java.io.*;
import java.io.ByteArrayOutputStream;
import java.net.*;
import java.util.Scanner;

class udpclient {

  public static void main(String args[]) throws Exception {

	DatagramSocket clientSocket = new DatagramSocket();
	BufferedReader inFromUser =
		  new BufferedReader(new InputStreamReader(System.in));
	System.out.println("Enter a message: ");
	String message = inFromUser.readLine();
	byte[] sendData = message.getBytes();
	InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
	int port = 9876;
	DatagramPacket sendPacket = 
	    new DatagramPacket(sendData, sendData.length, IPAddress, port);
	clientSocket.send(sendPacket);
	System.out.println("Ok, I sent it\n");
	
	
	
	
	
	//System.out.println("Enter server IP address");
	//String serverIP = in.next();
        //Ask for port to connect with
	//System.out.println("Enter server port number");
        //int serverPort = in.nextInt();
        
}        
}