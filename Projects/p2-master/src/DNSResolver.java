/**
 * Created by Douglas on 2/18/2016.
 *
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

class DNSResolver{
    static ArrayList<String> answerList;
    static HashMap<String, ArrayList<String>> cache = new HashMap<String, ArrayList<String>>();
    static HashMap<String, Date> dateMap = new HashMap<String, Date>();
    public static DNSPacket dnsPacket;
    public static String rootServer = "192.203.230.10";
    public static void main(String[] args) throws IOException {
        //Read command line argument
               int socketNum = getUserInput(args);
               DatagramSocket resolverSocket = null;
              while(true) {
                  try {
//                      System.out.println("press enter");
//                      Scanner x = new Scanner(System.in);
//                      x.nextLine();

                      resolverSocket = new DatagramSocket(socketNum);
                      System.out.println("Waiting on port: " + socketNum);
                      byte[] receiveDataFromClient = new byte[512];
                      DatagramPacket clientPacket = new DatagramPacket(receiveDataFromClient, receiveDataFromClient.length);
                      resolverSocket.receive(clientPacket);
                      dnsPacket = new DNSPacket(receiveDataFromClient);

                      if (!isValidType(dnsPacket, resolverSocket)) {
                          System.exit(1);
                      }

                      System.out.println("Client is requesting: " + dnsPacket.getQuestionName());

                      if (!isValidType(dnsPacket, resolverSocket)) {
                          System.out.println("Invalid Type");
                          System.exit(1);
                      }
                      //unset recursion bit
                      dnsPacket.unsetRD();

                      boolean answerFound = false;
                      boolean error = false;
                      InetAddress address;
                      DatagramPacket response;
                      DNSPacket responsePacket = null;
                      
                      // Check if query is in cache
                      if (cache.get(dnsPacket.getQuestionName()) != null) {
                          System.out.println("\nResults are in the cache!\n\nPrinting Answers:");
                          answerList = cache.get(dnsPacket.getQuestionName());
                          for (String s : answerList) {
                              System.out.println(s);
                          }
                          byte[] fromServer = new byte[512];
                          response = new DatagramPacket(fromServer, fromServer.length);
                          resolverSocket.receive(response);
                          responsePacket = new DNSPacket(fromServer);
                          error = responsePacket.isError();
                          answerFound = responsePacket.isAnswer();
                          rootServer = responsePacket.getNextIP();
                          if (rootServer.equals("")) {
			      error = true;
			      responsePacket.setRCode();
                             }
                          responsePacket.unsetRD();
                          responsePacket.setQRCode();
                          responsePacket.setAnswer();
                          responsePacket.setRCode();
                          address = InetAddress.getByName("127.0.0.1");
                          int port1 = clientPacket.getPort();
                          DatagramPacket toClient = new DatagramPacket(responsePacket.getQueryBytes(),
                              responsePacket.getQueryBytes().length, address, port1);
			  resolverSocket.send(toClient);
                      } else {
                          while (!answerFound && !error) {
                              address = InetAddress.getByName(rootServer);
                              DatagramPacket sendPacket = new DatagramPacket(dnsPacket.getQueryBytes(),
                                      dnsPacket.getQueryBytes().length, address, 53);
                              resolverSocket.send(sendPacket);
                              byte[] fromServer = new byte[512];
                              response = new DatagramPacket(fromServer, fromServer.length);
                              resolverSocket.receive(response);
                              responsePacket = new DNSPacket(fromServer);
                              System.out.println("Querying server " + rootServer);
                              error = responsePacket.isError();
                              answerFound = responsePacket.isAnswer();
                              rootServer = responsePacket.getNextIP();
                              if (rootServer.equals("")) {
                                   error = true;
                                   responsePacket.setRCode();
                             }
                          }

                          answerList = responsePacket.getAnswerRecords();
                          System.out.println("Answers found:");
                          for (String anAnswerList : answerList)
                              System.out.println(anAnswerList);
                      }

                      cache.put(responsePacket.getQuestionName(), answerList);

                      System.out.println("Sending answer to client");
                      address = InetAddress.getByName("127.0.0.1");
                      int port = clientPacket.getPort();
                      DatagramPacket toClient = new DatagramPacket(responsePacket.getQueryBytes(),
                              responsePacket.getQueryBytes().length, address, port);
                      resolverSocket.send(toClient);

                  } catch (NumberFormatException e) {
                      System.out.println("invalid port");
                      System.exit(1);
                  }
                  printCache();
                  //printTimes();
                  resolverSocket.close();
                  //checkTTL();
              }

        }


    public static int getUserInput(String [] args){
        String portNumber = "";
        if(args.length > 0) {
            String userInput = args[0];
            if (userInput.matches("\\d*"))
                portNumber = userInput;
        }
        else{
            System.out.println("Please provide desired Port Number");
            System.exit(1);
        }

        return Integer.parseInt(portNumber);
    }
    public static boolean isValidType(DNSPacket dnsPacket, DatagramSocket socket) throws IOException {

        if(!dnsPacket.isValidQuestion()) {
            dnsPacket.setRCode();
            System.out.println("Invalid question rcode: " + dnsPacket.getRCode());
            //send error code back to client
            InetAddress retAddress = InetAddress.getByName("127.0.0.1");
            DatagramPacket toClient = new DatagramPacket(dnsPacket.getQueryBytes(),
                    dnsPacket.getQueryBytes().length, retAddress, 5300);
            socket.send(toClient);
            return false;
        }
        return true;

    }
    public static void checkTTL() {
        Date current = new Date();
        for (String key : cache.keySet()) {
            if (current.after(dateMap.get(key))) {
                dateMap.remove(key);
                cache.remove(key);
            }
        }
    }
    public static void printCache() {
        System.out.println("Printing Cache\n" + cache.toString() + "\n");
    }
    public static void printTimes() {
        System.out.println("Printing Times\n" + dateMap.toString() + "\n");
    }
    private static byte[] buildRequestData(String host) {
        //head + (host length +1) + eof sign + qtype + qclass
        int size = 12 + host.length() + 1 + 1+ 4;
        ByteBuffer buff = ByteBuffer.allocate(size);
        Random random = new Random();
        byte[] seq = new byte[2];
        random.nextBytes(seq);
        buff.put(seq);
        byte[] header = {0x01,0x00,0x00,0x01,0x00,0x00,0x00,0x00,0x00,0x00};
        buff.put(header);
        //add query question domain field
        String[] parts = host.split("\\.");
        for(int i= 0; i < parts.length;i++) {
            buff.put((byte) parts[i].length());
            buff.put(parts[i].getBytes());
        }
        buff.put((byte)0x00);
        byte[] tmp = {0x00,0x01,0x00,0x01};
        buff.put(tmp);
        return buff.array();
    }

}


