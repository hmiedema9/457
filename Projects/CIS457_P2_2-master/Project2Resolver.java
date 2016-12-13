import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.ArrayList;

/********************************************************************************
 * CIS 457
 * Project 2
 * Recursive DNS caching resolver
 *
 *
 * @author Jonathan Powers, Brett Greenman, Kevin Anderson
 * @version Fall 2015
 ********************************************************************************/

class Project2Resolver {

    /* Port value used if user input is invalid */
    static int DEFAULT_PORT = 8025;

    static ArrayList<String> results;
    static ArrayList<String> answers;
    public static PacketInfo information;
    /**************************************************************************
     * Main method that prompts user input and opens a DatagramSocket
     **************************************************************************/
    public static void main(String[] args) throws IOException {
        //Read command line argument
        String userInput;
        int socketNum, socketTime;
        socketTime = 15000;
        Boolean timedOut = false;
        String nextServer = "198.41.0.4"; //Initial server to check is 198.41.0.4
        DatagramSocket serverSocket = null;
        if(args.length > 0)
            userInput = args[0];
        else
            userInput = "8025";

        //Check for valid argument
        if(isInteger(userInput))
            socketNum = Integer.parseInt(userInput);
        else {
            System.out.printf("The port you specified was not an integer. A default value of %d has been substituted.\n", DEFAULT_PORT);
            socketNum = DEFAULT_PORT;
        }

        // Create a socket that listens on port designated by user.
	  try {
	      serverSocket = new DatagramSocket(socketNum);
	      serverSocket.setSoTimeout(socketTime);
        System.out.printf("DNS resolver started on port %d\n", socketNum);

        /********************************
         * Create the cache class, to be started and used only while
         * the server is running
         *********************************/

        DNSCache serverCache = new DNSCache();

        /*********************************************************/
        while(true){
            boolean wasCached = false; //flag used for cached responses
            // Set server to listen for a DatagramPacket from client
            byte[] receiveData = new byte[1024];
            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(packet);

            // Print packet info from client using helper class PacketInfo()
            information = new PacketInfo(packet);
            information.getValues();
            information.getQuestions();
            information.getAnswers();
            information.getAuthority();
            //System.out.printf("%s\n", information.getValues());
            //System.out.printf("%s\n", information.getQuestions());
            //System.out.printf("%s\n", information.getAnswers());
            //System.out.printf("%s\n", information.getAuthority());
            //System.out.printf("%s\n----------------------\n", information.getAdditional());
            System.out.printf("\nReceived query from client for %s\n", information.getNameRequested());

            // Check for question type A and class type IN before proceeding
            if(!information.getValidQuestion()) {
                information.setErrorCode();
                System.out.println("\nSending answer to client\n");

                //send error code back to client
                InetAddress retAddress = InetAddress.getByName("127.0.0.1");
                int port = packet.getPort();
                DatagramPacket toClient = new DatagramPacket(information.getByteArray(),
                        information.getByteArray().length, retAddress, port);
                serverSocket.send(toClient);
            }

            //Send DatagramPacket down server tree with recursion desired bit unset
            information.unsetRecursion();
           
            boolean doneSearching = false;
            boolean error = false;
            InetAddress address;
            DatagramPacket response;
            PacketInfo responseInfo = null;

            //-------------------------------------------------------------------------
            if(serverCache.checkCompleteDomain(information.getNameRequested())){
                // if cache has complete domain, skip recursive search for IP
                int f = 0;
                wasCached = true;
                System.out.println("Query is in cache\n");
                doneSearching = true;
                byte [] respond = new byte[512];
                ByteBuffer buffer =ByteBuffer.wrap(respond);
                buffer.putShort((short)information.getID());
                buffer.putShort((short)0x8080); //hex representation of flags
                buffer.putShort((short) 1); //QDCount
                buffer.putShort((short) serverCache.getValue(information.getNameRequested()).size()); //ANCount
                buffer.putShort((short) 0); //NSCount
                buffer.putShort((short) 0); //ARCount
                f += 12;
                //Questions section
                for(int i = 0; i < information.getNameRequested().length(); i++){
                    buffer.put((byte) information.getNameRequested().charAt(i));
                    f++;
                }
                buffer.putShort((short) information.getQType());
                buffer.putShort((short) information.getQClass());
                f += 4;
                //Answers section
                for(String s : serverCache.getValue(information.getNameRequested())){
                    for(int i = 0; i < information.getNameRequested().length(); i++){
                        buffer.put((byte) information.getNameRequested().charAt(i));
                        f ++;
                    }
                    buffer.putShort((short) 1); //Type field
                    buffer.putShort((short) 1); //Class Field
                    buffer.putInt(2000); //TTL field. TODO CHANGE THIS VALUE TO LIFESPAN
                    buffer.putShort((short) 4); //RDLength, assuming IPv4 address
                    f += 10;
                    for(int i = 0; i < s.length(); i++){
                        if(s.charAt(i) != '.'){
                            buffer.put((byte) s.charAt(i));
                            f ++;
                         }
                    }
                }
                System.out.println("\nSending answer to client\n");
                address = InetAddress.getByName("127.0.0.1");
                int port = packet.getPort();
                DatagramPacket toClient = new DatagramPacket(respond,
                        f, address, port);
                serverSocket.send(toClient);

            } /*else if (serverCache.checkTopLevel(){
                 *********************************************************************
                 * Either need to send the parsed string 'edu' 'com' etc. from the full
                 * domain request or figure out where the top level is code/string is
                 * stored in the header and send that.
                 **********************************************************************
            }*/
            else{
                System.out.println("Query is not in cache. Querying default root server.\n");
            }
            //-------------------------------------------------------------------------

            //Loop until an answer is found or there is an error
            while(!doneSearching && !error) {
                address = InetAddress.getByName(nextServer);
                DatagramPacket sendPacket = new DatagramPacket(information.getByteArray(),
                        information.getByteArray().length, address, 53);
                serverSocket.send(sendPacket);

                byte[] fromServer = new byte[1024];
                response = new DatagramPacket(fromServer, fromServer.length);
                serverSocket.receive(response);

                responseInfo = new PacketInfo(response);

                System.out.printf("\nQuerying server %s\n", nextServer);
                //read information in received packet
                responseInfo.getValues();
                responseInfo.getQuestions();
                responseInfo.getAnswers();
                responseInfo.getAuthority();
                responseInfo.getAdditional();

                //This code is useful for viewing more complete packing info in terminal
                //System.out.printf("%s\n", responseInfo.getValues());
                //System.out.printf("%s\n", responseInfo.getQuestions());
                //System.out.printf("%s\n", responseInfo.getAnswers());
                //System.out.printf("%s\n", responseInfo.getAuthority());
                //System.out.printf("%s\n----------------------\n", responseInfo.getAdditional());
                //System.out.println(responseInfo.isAnswer());
                //System.out.println(responseInfo.nextIP());

                System.out.printf("Received answer: %b\n", responseInfo.isAnswer());
                error = responseInfo.isError();
                doneSearching = responseInfo.isAnswer();
                nextServer = responseInfo.nextIP();
                results = responseInfo.getResults();

                if(!doneSearching) {
                    System.out.println("Authority records found:");
                    for(int i = 0; i < results.size(); i++) {
                        System.out.println(results.get(i));
                    }
                }
                if(nextServer.equals("")) {
                    error = true;
                    responseInfo.setErrorCode();
                }
            }

            //Send answer back to client
            if(!wasCached){
                answers = responseInfo.getAnswerRecords();
                System.out.println("Answers found:");
                ArrayList<String> ipList = new ArrayList<String>(); //used to add answer to cache
                for(int i = 0; i < answers.size(); i++) {
                    System.out.println(answers.get(i));
                    ipList.add(answers.get(i));
                }
                serverCache.appendComplete(responseInfo.getNameRequested(), ipList); //TODO ADD TTL
                System.out.println("\nSending answer to client\n");

                address = InetAddress.getByName("127.0.0.1");
                int port = packet.getPort();
                DatagramPacket toClient = new DatagramPacket(responseInfo.getByteArray(),
                        responseInfo.getByteArray().length, address, port);
                serverSocket.send(toClient);
            }
        }
} catch (NumberFormatException e) {
	      System.out.printf("The port you specified cannot be used. Please launch the application again.\n");
	      System.exit(1);
	  }
	  catch (SocketTimeoutException e) {
	      System.out.println("Connection timed out");
	      System.exit(1);
	  }
    }

    /*********************************************************
     * Method to check if user generated input is an integer
     **********************************************************/
    public static boolean isInteger(String str) {

        try {
            int i = Integer.parseInt(str);
        }
        catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

}
