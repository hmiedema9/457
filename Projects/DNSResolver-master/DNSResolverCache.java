package project_adi;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class DNSResolverCache {

	public ArrayList<String> cache= new ArrayList<String>();

    public static void main(String args[]) throws Exception{
		DatagramSocket serverSocket = new DatagramSocket(9876);
	 	while(true){
	    	byte[] recvData = new byte[1024];
	    	byte[] sendData = new byte[1024];
	    	DatagramPacket recvPacket = 
			 			new DatagramPacket(recvData,recvData.length);
	    	serverSocket.receive(recvPacket);
	    	String message = new String(recvData);
	    	System.out.println("Got from client: "+message);
	    	InetAddress IPAddress = recvPacket.getAddress();
	    	int port = recvPacket.getPort();
	    	sendData = message.getBytes();
	    	DatagramPacket sendPacket = 
						new DatagramPacket(sendData,sendData.length,IPAddress,port);
	    	serverSocket.send(sendPacket);
		}
    }

}
