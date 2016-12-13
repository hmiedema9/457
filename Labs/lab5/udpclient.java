/* Lab 5 - UDP
 * By: Hayden Miedema
*/

import java.io.*;
import java.io.ByteArrayOutputStream;
import java.net.*;
import java.util.Scanner;

class udpclient {

  public static void main(String args[]) throws Exception {
	String message = "";
	Scanner in = new Scanner(System.in);
	System.out.println("Enter server IP address");
	String serverIP = in.next();
	InetAddress IPAddress = InetAddress.getByName(serverIP);
        //Ask for port to connect with
	System.out.println("Enter server port number");
        int serverPort = in.nextInt();
	DatagramSocket clientSocket = new DatagramSocket();
	BufferedReader inFromUser =
		  new BufferedReader(new InputStreamReader(System.in));
	
	while(!(message.equals("/exit"))){
	  System.out.println("Enter a message: ");
	  message = inFromUser.readLine();
	  byte[] sendData = message.getBytes();
	  DatagramPacket sendPacket = 
	      new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
	  clientSocket.send(sendPacket);
	  System.out.println("Ok, I sent it");
	  int newPort = sendPacket.getPort();
	  InetAddress newIP = sendPacket.getAddress();
	  DatagramPacket receivePacket = 
	      new DatagramPacket(sendData,sendData.length);
	  clientSocket.receive(receivePacket);	
	  String receiveMessage = new String(sendData);
	  System.out.println("Here is your message back: " + receiveMessage + " from server at" + receivePacket.getAddress());
	}     
}        
}