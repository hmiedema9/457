/* Lab 5 - UDP
 * By: Hayden Miedema
*/

import java.io.*;
import java.net.*;
import java.util.Scanner;

class udpserver{

  public static void main(String args[]) throws Exception {
	
	Scanner in = new Scanner(System.in);
	//Ask for port for server to listen on
	System.out.println("Enter port number");
        int port = in.nextInt();
        InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
	DatagramSocket serverSocket = new DatagramSocket(port);
	while(true){
	  byte[] receiveData = new byte[1024];
	  DatagramPacket receivePacket = 
	      new DatagramPacket(receiveData,receiveData.length);
	  serverSocket.receive(receivePacket);	
	  String message = new String(receiveData); //converts into a string from a byte array
	  System.out.println("Got the message");
	  System.out.println("The message was: " + message + " from client " + receivePacket.getAddress());
	  
	  InetAddress newIP = receivePacket.getAddress();
	  int newPort = receivePacket.getPort();
	  DatagramPacket sendPacket = 
	    new DatagramPacket(receiveData, receiveData.length, newIP, newPort);
	  serverSocket.send(sendPacket);
	  System.out.println("Ok, I sent the message back to the client");
	  
	}
	
	
	
	
	
	
	
	
	
        
        
}
}