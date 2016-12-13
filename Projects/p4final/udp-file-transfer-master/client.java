/************************************
* Project 4 - Reliable UDP Transfer
* By: Hayden Miedema and Doug Money
************************************/

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.channels.*;
import java.net.*;
import java.lang.Integer;
import java.util.EnumSet;

class client {

    public static void main(String args[]) {
        
        if (args.length != 3) {
            System.err.println("usage:\n" +
                               "\t1st arg : IP address of server\n" +
                               "\t2nd arg : port of server\n" + 
                               "\t3rd arg : name of file to obtain");
            System.exit(0);
        }
        
        DatagramSocket clientSocket = null;
        InetAddress serverAddr = null;
        FileChannel fileChannel = null;
        int fileSize = 0;
        int totalPackets = 0;
        
        try {
        
            /* Get IP addr of the server */

            try {
                serverAddr = InetAddress.getByName(args[0]);
                            
            } catch (ArrayIndexOutOfBoundsException x) {
                System.out.println("first argument: invalid IP address");
                System.exit(0);
            } 
                
            /* Get port of the server */
            
            int port = 0;
            
            try {
                port = (Integer.parseInt(args[1]));
                
            } catch (ArrayIndexOutOfBoundsException x) {
                System.err.println("second argument: invalid port number");
                System.exit(0);
                
            } catch (NumberFormatException x) {
                System.err.println("second argument: invalid port number");
                System.exit(0);
            }
            
            if (port > 65535 || port < 1024) {
                System.err.println("second argument: port must be from 1024 to 65535");
            }
            
            // Create a path for output file
            
            Path path = Paths.get(args[2] + ".out");
            
            // Create a SeekableByteChannel object for the path
                        
            fileChannel = (FileChannel) Files.newByteChannel(path,
                EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE));
            
            
            /* Open a socket */
             
            clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(Constants.NO_RESPONSE_TIMEOUT);
        
            /* send request to the server */
            
            byte[] filenameData = args[2].getBytes();
            
            DatagramPacket sendPacket = 
                new DatagramPacket(filenameData, filenameData.length, serverAddr, port);
            clientSocket.send(sendPacket);
            
            /* Receive confirmation or denial */
            
            byte[] recvData = new byte[Constants.PACK_SIZE];
            
            DatagramPacket recvPacket = 
                new DatagramPacket(recvData, Constants.PACK_SIZE);
            clientSocket.receive(recvPacket);
            
            String statusStr = new String(recvPacket.getData());
            
            /* Exit if the file is unavailable */
            
            if (statusStr.startsWith("unable", 0)) {
            
                System.out.println(statusStr);
                clientSocket.close();
                System.exit(1);
                
            } else if (statusStr.startsWith("server", 0)) {
            
                /* Get file size and number of expected packets */
                
                String[] status = statusStr.split(":");

                try {
                    fileSize = Integer.parseInt(status[1]);
                    totalPackets = Integer.parseInt(status[2]);

                } catch (NumberFormatException e) {
                    System.err.println(e);
                }
                
                System.out.println(status[0]);
                System.out.println("file size: " + status[1] + " bytes");
                System.out.println("expected packets: " + status[2]);
                System.out.print("packet traffic: [\n");
                System.out.println(clientSocket.getSendBufferSize());

                
                int seqNumber = 0;
                    
                boolean[] packetsRcvd = new boolean[totalPackets];

                while (!checkForComplete(packetsRcvd)) {
                    // receive packet of data
                    
                    clientSocket.receive(recvPacket);
                    
                    // write the packet contents to the appropriate spot in FileChannel.
                    // We may receive a duplicate of
                    //     some of the packets if the server did not receive the
                    //     acknowledgment or assumed that the packet was lost when
                    //     it really just took longer than Constants.ACK_TIMEOUT.
                    //     In this case, we should simply discard the second packet
                    //     and resend the acknowledgment.
                    
                    seqNumber = getSeqNumber(recvPacket);
                    
                    if (verifyCheckSum(recvPacket.getData())) {
                        if (!packetsRcvd[seqNumber]) {
                            writeToChannel(recvPacket.getData(), fileChannel);
                            packetsRcvd[seqNumber] = true;
                            System.out.print(System.currentTimeMillis());
                            System.out.print(" r:" + seqNumber + ", \n");
                            
                            // send acknowledgment number to server

                            byte[] ack = makeAckArray(seqNumber);
                            
                            DatagramPacket sendAck = 
                                new DatagramPacket(ack, ack.length, serverAddr, port);
                            clientSocket.send(sendAck);
                            System.out.print(System.currentTimeMillis());
                            System.out.print(" a:" + seqNumber + ", \n");
                        } else {
                            System.out.print(System.currentTimeMillis());
                            System.out.print(" r:" + seqNumber + " REPEAT, \n");
                        }
                    } else {
                        System.out.print(System.currentTimeMillis());
                        System.out.print(" r:" + seqNumber + " CORRUPTED, \n");
                    }
                    

                    
                    
                    

                }
                
                System.out.println("received and acknowledged all packets ]");
                System.out.println("New file is '" + args[2] + ".out'");
            } else {
                System.out.println("Network problems. Please try again.");
                clientSocket.close();
                System.exit(1);
            }
        } catch (UnknownHostException e) {
            System.out.println("first argument: invalid IP address");
            System.exit(1);
        } catch (SocketTimeoutException e ) {
            System.err.println("No response from server.");
        } catch (SocketException e) {
            System.err.println("Socket error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
        }
                
        // truncate the file to the correct size
                
        try {
            if (fileChannel != null) {
                fileChannel.truncate((long)fileSize);
                fileChannel.close();       
            }
        } catch (IOException e) {
            System.err.println("Unable to truncate file: " + e.getMessage());
        }
                

        clientSocket.close();
        
    }
    /*
        
    */
    public static int
    getSeqNumber(DatagramPacket dp)
    {
        ByteBuffer seq = ByteBuffer.allocate(4).put(dp.getData(), 0, 4);
        seq.flip();
        return seq.getInt();
    }
    
    /*
        
    */
    public static boolean
    verifyCheckSum(byte[] packArr)
    {
        byte targetSum = packArr[Constants.SEQ_SIZE];
        packArr[Constants.SEQ_SIZE] = 0;
        
        short sum = (short) (packArr[0] + packArr[1]);
        // it did overflow
        if (sum >= 256)
            sum = (short)(sum - 255);
        
        for (int i = 2; i < Constants.PACK_SIZE; i++) {
        
            sum = (short) (sum + (short) packArr[i]);
          
            if (sum >= 256) // it overflowed
                sum = (short)(sum - 255);
            
        }
        
        sum = (short) ~sum;
        
        if ((byte) sum == targetSum) {
            return true;
        } else {
            return false;
        }
    }
    /*
    
    */
    public static byte[]
    makeAckArray(int seq)
    {
    
        int size = 0;
        for (int i = 0; i < Constants.MAX_BIT_ERRORS + 2; i++) {
            size += Constants.SEQ_SIZE;
        }
        
        ByteBuffer ack = ByteBuffer.allocate(size);
    
        for (int i = 0; i < Constants.MAX_BIT_ERRORS + 2; i++) {
            ack.putInt(seq).array();
        }
        
        return ack.array();
    }
    
    /*
        Write packet data to channel and return the sequence number
    */
    public static void
    writeToChannel(byte[] packet, FileChannel fc)
    {
        ByteBuffer data = ByteBuffer.allocate(Constants.DATA_SIZE);
        data.put(packet, Constants.HEAD_SIZE, Constants.DATA_SIZE);
        data.flip();
        
        ByteBuffer seq =
            ByteBuffer.allocate(Constants.SEQ_SIZE).put(packet, 0, Constants.SEQ_SIZE);
        seq.flip();
        long pos = (long) seq.getInt() * Constants.DATA_SIZE;

        try {
            fc.write(data, pos);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    
    public static boolean
    checkForComplete(boolean[] rcvd)
    {
        int count = 0;
        for (boolean a : rcvd) {
            if (!a) {
                break;
            } else {
                count++;
            }
        }        
        if (count == rcvd.length) {
            return true;
        }
        return false;
    }
    
}

