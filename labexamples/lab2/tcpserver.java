/* CIS 457 - Lab 2 */
/* By: Hayden Miedema */
import java.io.*;
import java.net.*;
import java.util.Scanner;

class tcpserver{
	public static void main(String argv[]) throws Exception{
		ServerSocket listenSocket = new ServerSocket(9876);
		while(true){
		  Socket s = listenSocket.accept();
		  Runnable r = new ClientHandler(s);
		  Thread t = new Thread(r);
		  t.start();
		}	
			
	}
}

class ClientHandler implements Runnable{
	Socket connectionSocket;
	ClientHandler(Socket connection){
	  connectionSocket = connection;
	}

  public void run(){
      
  
  
  }

}

