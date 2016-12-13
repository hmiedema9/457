package project_adi;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.Random;

public class DNSClient {

	public static void main (String argv[]) { 
        try {
        InetAddress ip;
        BufferedReader br = new BufferedReader(
                                new FileReader("/etc/resolv.conf"));
        
        if(argv.length == 0) {
            br.readLine();
            String[] tokens = br.readLine().split(" ");
            ip = InetAddress.getByName(tokens[1]);
        } else{
            ip = InetAddress.getByName(argv[0]);
        }
        
        DatagramSocket sockfd = new DatagramSocket();
        sockfd.setSoTimeout(2000);
        
        DNSRequest request = new DNSRequest(sockfd, ip);
        request.createRequest();
        request.sendRequest();
        
        DNSResponse sResponse = new DNSResponse(sockfd);
        sResponse.recvServersResponse();
        System.out.println(sResponse.toString());
        
        } catch (Exception e) {
            System.out.println("Something went wrong.\n");
            e.printStackTrace();
        }
    }
}
