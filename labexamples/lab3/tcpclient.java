/* CIS 457 - Lab 2 */
/* By: Hayden Miedema */
import java.io.*;
import java.net.*;
import java.util.Scanner;

class tcpclient{
	public static void main(String args[]) throws Exception{
		
		Scanner scan = new Scanner(System.in);
		//ask for an IP
		System.out.println("Enter an ip: ");
		String IP = scan.next();
		//ask for a port
		System.out.println("Enter a port number: ");
		int port = scan.nextInt();
		Socket clientSocket= new Socket(IP,port);
		
		DataOutputStream outToServer = 
			new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer =
			new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream()));		
		BufferedReader inFromUser = 
			new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter a message: ");
		String message = inFromUser.readLine();
		outToServer.writeBytes(message + '\n');
		System.out.println("Ok, I sent it.");
		
		//Read line with inFromServer and print the message back
		String returnMsg = inFromServer.readLine();
		System.out.println("Message BACK from the server: " +returnMsg);


	}

}

