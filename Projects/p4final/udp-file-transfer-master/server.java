/************************************
* Project 4 - Reliable UDP Transfer
* By: Hayden Miedema and Doug Money
************************************/

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.channels.*;
import java.net.*;
import java.lang.*;


public class server {
    
    public static void main(String args[]) {
    
        /* Get port number from arg */
        
        int port = 0;
        
        try {
            port = (Integer.parseInt(args[0]));
            
            if (port > 65535 || port < 1024) {
                System.err.println("port must be from 1024 to 65535");
                System.exit(0);
            }
            
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("must enter port number as first argument");
            System.exit(0);
            
        } catch (NumberFormatException e) {
            System.err.println("must enter port number as first argument");
            System.exit(0);
        }
        
        
        DatagramSocket serverSocket = null;
        InetAddress clientAddr = null;
        int clientPort = 0;
        String fileName = null;
        
        try {
            // Open a socket to listen
        
            serverSocket = new DatagramSocket(port);
        
        } catch (SocketException e) {
            System.err.println(e);
	        System.exit(1);
        }
        
        
        for(;;) {
        
            try {

                serverSocket.setSoTimeout(0);
                System.out.println("Listening for new client request.");
                
                // Receive a file request 
                
                byte[] data = new byte[Constants.PACK_SIZE];

                DatagramPacket recvPacket = 
                    new DatagramPacket(data,data.length);
                serverSocket.receive(recvPacket);
                
                fileName = new String(recvPacket.getData()).trim();

                // Save client addr and port            
                
                clientAddr = recvPacket.getAddress();
                clientPort = recvPacket.getPort();
	            
                System.out.println("server received request for '" +
                    fileName + "'" + " from " + clientAddr.toString() + " [" + clientPort + "]" );
	            
                // Create a path from the file name,
                //   can throw NoSuchFileException
                
                
            	Path path = Paths.get(fileName).toRealPath(); 
                        
            	if (!Files.exists(path)) {
            	
            	    serverSocket.send(
            	        assembleDenialPacket(fileName, clientAddr, clientPort));
                            
            	} else {
            	
                    // Create a SeekableByteChannel object for the path
                        	
		            FileChannel fileChannel = (FileChannel) Files.newByteChannel(path, StandardOpenOption.READ);
                        	
		            // Get info about the file
                        	
		            double dFileSize = fileChannel.size();

		            int totalPackets = (int) Math.ceil(dFileSize / Constants.DATA_SIZE);
                    int fileSize = (int) dFileSize;
                        	
		            // Send the confirmation of request
                        	
		            serverSocket.send (
                        assembleConfirmPacket (
                            fileName, fileSize, totalPackets, clientAddr, clientPort));

                    // Setup variables for sequencing and acknowledging                    
                            
		            int sequence = 0;
		            int acknowledgment = 0;
		            int ackCount = 0;
                        	
                    Window window = new Window(Constants.WINDOW_SIZE);
                        	
                    int head = 0;
                        	
                    // Construct and send the first five packets

                    System.out.print("packet traffic:[\n");

                    for (int i = 0; i < Constants.WINDOW_SIZE; i++) {
                        
                        if (sequence < totalPackets) {
                            
                            serverSocket.send(
                                constructNextPacket(
                                    fileChannel, clientAddr, clientPort, sequence));
                            
                            window.loadFirstEmpty(sequence);
                            System.out.print(System.currentTimeMillis());
                            System.out.print(" s:" + sequence + ", \n");
                            sequence++;
                            
                        } else {
                            break;
                        }
                        
                    }
                    
                    // Fire off a TimoutThread to check for packet losses
	                    
                    TimeoutThread timeoutThread = new TimeoutThread(serverSocket, clientAddr, clientPort, fileChannel, window);
                    timeoutThread.start();
	                    
	                    
                    // listen for acknowledgments from client
                    
                    boolean[] acksRcvd = new boolean[totalPackets];
                    serverSocket.setSoTimeout(Constants.NO_RESPONSE_TIMEOUT);
                    
                    while (true) {
                        
            		    // listen for any ack
            		    try {
                            serverSocket.receive(recvPacket);
                        } catch (SocketTimeoutException e) {
                            timeoutThread.kill();
                            System.out.print("Client did not acknowledge packets ");
                            for (int i = 0; i < acksRcvd.length; i++) {
                                if (!acksRcvd[i]) {
                                    System.out.print(i + ", ");
                                }
                            }
                            System.out.println("assumed disconnected.]\n");
                            break;
                        }
            		    // parse ack for sequence number
                    	
            		    acknowledgment = getAckNumber(recvPacket.getData());
            		    if ( acknowledgment >= 0 ) {
                		    acksRcvd[acknowledgment] = true;
                        
                            if (checkForComplete(acksRcvd)) {
                                timeoutThread.kill();
                                System.out.println("client acknowledged " +
                                    totalPackets + " of " + totalPackets + " packets]\n");
                                break;
                            }
                            System.out.print(System.currentTimeMillis());
                            System.out.print(" a:" + acknowledgment + ", \n");
                        
                            // update window with new acknowledgment,
                            //     find how many new packets to send from the return value
                            
                            int packetsToSend = window.recvAck(acknowledgment);
                            
                            // If necessary, send new packets and load them into the window
                            
                            for (int i = 0; i < packetsToSend; i++) {
	                            if (sequence >= totalPackets) {
	                                break;
	                            }
                                serverSocket.send(
                                    constructNextPacket(
                                        fileChannel, clientAddr, clientPort, sequence));
                            
                                window.loadFirstEmpty(sequence);
                                System.out.print(System.currentTimeMillis());
                                System.out.print(" s:" + sequence + ", \n");
                                sequence++;
                                
                            }
                        } else {
                            System.out.print("Fatal error: client ack too corrupted.\n");
                            break;
                        }
                    }
                     	
	            }
            
	            	
            } catch (NoSuchFileException x) {
            
                // Send and print denial of request
                try {
		            serverSocket.send(
    			        assembleDenialPacket(fileName, clientAddr, clientPort));
                            
            	} catch (IOException e) {
            	    System.err.println(e);
            	}
            } catch (SocketTimeoutException e) {
                System.err.println(e); 
            } catch (IOException e) {
	            System.err.println(e + "\n" + e.getStackTrace());
	            System.exit(1);
	        } catch (IllegalArgumentException e) {
		        System.err.println(e);
		        System.exit(1);
	        }
        }
        
    }
    
    /* 
        Returns a DatagramPacket with request confirmation information, ready to be sent
    */
    public static DatagramPacket
    assembleConfirmPacket(String fn, int fs, int tp, InetAddress addr, int port)
    {
        String msg = new String("server received request for '" + fn + "'");
        System.out.println("  file size: " + fs);
        System.out.println("  packets to send: " + tp);
        msg += (":" + fs + ":" + tp + ":");
        	
    	byte[] data = msg.getBytes();
        	
    	return new DatagramPacket(data, data.length, addr, port);
    }
    
    /* 
        Returns a DatagramPacket with request denial information, ready to be sent
    */
    public static DatagramPacket
    assembleDenialPacket(String fn, InetAddress addr, int port)
    {
        String msg = new String("unable to locate '" + fn + "' on server");
        System.out.println("  - " + msg);
    	byte[] data = msg.getBytes();
    	return new DatagramPacket(data, data.length, addr, port);
    }
    
    /*
        Returns a DatagramPacket with 1000 bytes of file data
        and a complete header, ready to be sent
    */
    public static DatagramPacket
    constructNextPacket(FileChannel fc, InetAddress addr, int port, int seq)
    {
        ByteBuffer buf = ByteBuffer.allocate(Constants.DATA_SIZE);
        long pos = seq * Constants.DATA_SIZE;
        try {
            if (pos >= 0) {
                fc.read(buf, pos);
            }
        } catch (IOException e) {
	        System.err.println(e + "\n" + e.getStackTrace());
	        return null;
        }
        
        
        // TODO part 3: compute the checkSum of the buf and seq here
        //computeChecksum(seq, buf);
        
        byte[] packet = new byte[Constants.PACK_SIZE];
        packet = makePacket(seq, buf);
        
        return new DatagramPacket(packet, packet.length, addr, port);
    }
    /*
        TODO part 3: include the checkSum in the header construction
        
        Helper for constructNextPacket()
        Returns a byte array with header and data, ready to be loaded into a packet
        Packet includes:
            4 bytes of sequence number
            TODO (Up to) 20 bytes of checksum
            1000 bytes data
    */
    public static byte[]
    makePacket(int seq, ByteBuffer buf)
    {
        byte[] packArr = new byte[Constants.PACK_SIZE];
        byte[] seqArr = ByteBuffer.allocate(Constants.SEQ_SIZE).putInt(seq).array();
        System.arraycopy(seqArr, 0, packArr, 0, Constants.SEQ_SIZE);
        System.arraycopy(buf.array(), 0, packArr, Constants.HEAD_SIZE, Constants.DATA_SIZE);
        

        // compute checksum  
        
        short sum = (short) (packArr[0] + packArr[1]);
        // it did overflow
        if (sum >= 256) {
            sum = (short)(sum - 255);
        }
        
        for (int i = 2; i < Constants.PACK_SIZE; i++) {

            sum = (short) (sum + (short) packArr[i]);
          
            if (sum >= 256) { // it overflowed
              sum = (short)(sum - 255);
            }
        }
        
        sum = (short) ~sum;
        
        packArr[Constants.SEQ_SIZE] = (byte) sum;
        
        return packArr;
    }
    /*
        Returns sequence number from data of acknowledgment packet
    */
    public static int
    getAckNumber(byte[] ackArr)
    {
        int size = Constants.MAX_BIT_ERRORS + 2;
        int[] allegedSeqNumbers = new int[size];
        
        for (int i = 0; i < size; i++) {
            ByteBuffer seqBuf = ByteBuffer.allocate(Constants.SEQ_SIZE);
            seqBuf.put(ackArr, i*4, Constants.SEQ_SIZE);
            seqBuf.flip();
            allegedSeqNumbers[i] = seqBuf.getInt();
        }
        
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (i != j && allegedSeqNumbers[i] == allegedSeqNumbers[j])
                    return allegedSeqNumbers[i];
        
        return -1;

    }

    
    public static boolean
    checkForComplete(boolean[] ackd)
    {
        int count = 0;
        
        for (boolean a : ackd) {
            if (a) {
                count++;
            } else {
                break;
            }
        }        
        if (count == ackd.length) {
            return true;
        }
        return false;
    }
    
}

// TimoeoutThread is a helper thread that continuously
// checks each of the values in window.timeSent[]:
//   If more than ACK_TIMEOUT seconds has occurred since
//   any timeSent, we should resend the packet
//   in that slot of the window.
    
class TimeoutThread extends Thread {

    
    private volatile boolean isRunning = true;
    
    private DatagramSocket socket; 
    private FileChannel fileChannel;
    private int clientPort;
    private InetAddress clientAddr;
    private Window window;
    
    
    
    public TimeoutThread(DatagramSocket sock, InetAddress a, 
                            int p, FileChannel fc, Window win)
    {
        this.socket = sock;
        this.fileChannel = fc;
        this.clientPort = p;
        this.clientAddr = a;
        this.window = win;
    }
    
    public void
    run() {
	
        while (isRunning) {
        
            try {
                for (int i = 0; i < Constants.WINDOW_SIZE; i++) {
                    int sequence = window.getSeqNumber(i);
                    if (sequence >= 0) {
                        if ((System.currentTimeMillis() - window.getTimeSent(i)) > 
                                Constants.ACK_TIMEOUT ) {
                        
                            System.out.print(System.currentTimeMillis());
                            System.out.print(" s:" + sequence + " REPEAT, \n");
                            
                            if (!isRunning) {
                                break;
                            }
                                socket.send(
                                    server.constructNextPacket(
                                        fileChannel, clientAddr, clientPort, sequence));
                                        
                                window.updateTimeSent(sequence);
                            
                        }
                    }
                }
                
            } catch (SocketException e) {
                System.err.println(e);
            } catch (IOException e) {
	            System.err.println(e + "\n" + e.getStackTrace());
            }

            
        }
        
        try {
            fileChannel.close();
        } catch (IOException e) {
            System.err.println(e + "\n" + e.getStackTrace());
        }
    }
    
    public void
    kill() {
        isRunning = false;
    }
    
}

