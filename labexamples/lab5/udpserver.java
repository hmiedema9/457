/* UDP Server Class
 * This class is paired with the UDP Client class in order
 * to transfer files from the server to the client.
 * By: Hayden Miedema
*/

import java.io.*;
import java.net.*;
import java.util.Scanner;

class udpserver{

public static void main(String args[]) throws Exception {
		
	DatagramSocket serverSocket = new DatagramSocket(9876);
	while(true){
	  byte[] receiveData = new byte[1024];
	  DatagramPacket receivePacket = 
	      new DatagramPacket(receiveData,receiveData.length);
	  serverSocket.receive(receivePacket);	
	  String message = new String(receiveData); //converts into a string from a byte array
	  System.out.println("Got the message");
	  System.out.println("The message was: " + message + " from client " + receivePacket.getAddress());
	  
	}
	
	
	
	
	
	
	
	
	//Scanner in = new Scanner(System.in);
	//Ask for port for server to listen on
	//System.out.println("Enter port number");
        //int port = in.nextInt();
        
        
}
}