package project_adi;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class DNSRequest {

	public static final int DNS_PORT = 53;
    public static final short CLASS_IN = 1;
    public static final short TYPE_A = 1;
    
    private DatagramSocket socket;
    private InetAddress address;
    private String domainName;
    private DatagramPacket sendPacket;
    
    public DNSRequest(DatagramSocket socket, InetAddress address) {
    
        this.socket = socket;
        this.address = address;
        
        domainName = getDomainName();
    }
    
    private String getDomainName() {
        
        BufferedReader inFromUser =
        new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter a domain name: ");
        String domain = "";
        try {
            domain = inFromUser.readLine();
        } catch (IOException e) {
            System.out.println("Could not get domain name.\n");
            e.printStackTrace();
        }
        return domain;
    }
    
    private short generateRandID() {
        Random r  = new Random();
        return (short) r.nextInt();
    }
    
    public void createRequest() throws IOException {
        
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(b);
        
        short id = generateRandID();
        short flags = 0;
        flags |= (1 << 8);
        short qcount = 1;
        short ancount = 0;
        short authcount = 0;
        short addcount = 0;
        
        d.writeShort(id);
        d.writeShort(flags);
        d.writeShort(qcount);
        d.writeShort(ancount);
        d.writeShort(authcount);
        d.writeShort(addcount);
        
        String[] labels = domainName.split("\\.");
        
        for(String label : labels){
            d.writeByte(label.length());
            d.writeBytes(label);
        }
        
        d.writeByte(0);
        d.writeShort(TYPE_A);
        d.writeShort(CLASS_IN);
        d.flush();
        
        byte[] sendData = b.toByteArray();
        
    
        sendPacket = new DatagramPacket(
                    sendData,sendData.length,address,DNS_PORT);
    }
    
    public void sendRequest() throws IOException {
        
        try {
            socket.send(sendPacket);
            System.out.println("Sent the query.\n");
        } catch (IOException e) {
            System.out.println("Could not send request. Please make sure IP address is valid.\n");
            System.exit(0);
        }
    }

}
