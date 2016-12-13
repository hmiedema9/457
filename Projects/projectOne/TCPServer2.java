import java.io.*;
import java.net.*;
import java.util.*;

class TCPServer2 {
    public static void main(String[] args) throws Exception {
    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
    ServerSocket listenSocket = new ServerSocket();
    int port = 0;
    while(port == 0)
        {
        System.out.print("Please enter port number: ");
        port = Integer.parseInt(inFromUser.readLine());
        try
            {
            listenSocket = new ServerSocket(port);
            }
        catch (Exception e)
            {
            System.out.println("This port is already in use!");
            port = 0;
            }
        }
    System.out.println("Waiting for Request...");
    while(true){
        Socket connectionSocket = listenSocket.accept();
        Runnable r = new ClientHandler(connectionSocket);
        Thread t = new Thread(r);
        t.start();
    }
    }
}

class ClientHandler implements Runnable {
    Socket connectionSocket;
    ClientHandler(Socket connection)
    {
      connectionSocket = connection;
    }

    public void run() {
    try
        {
        
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        String message = inFromClient.readLine();
        if(message.equals("test"))
            {
            outToClient.writeBytes("success\n");
            System.out.println("Connection established");
            String fileName = inFromClient.readLine();
            File file = new File(fileName);
            System.out.println("Client is requesting " + fileName);
            if(!file.exists())
                {
                System.out.println("File requested does not exist");
                outToClient.writeBytes("err\n");
                return;
                }
            System.out.println("Beginning Transmission... ");
            byte[] byteArray = new byte[1024];
            outToClient.writeBytes(file.length() + "\n");
            InputStream is = new FileInputStream(file);
            OutputStream out = connectionSocket.getOutputStream();
                
            int count;
            while((count = is.read(byteArray)) >= 0)
                {
                out.write(byteArray, 0, count);
                }
            out.close();
            is.close();
                
                
            System.out.println("File Sent");
            }
        }
    catch (Exception e)
        {
        }
        
    }
}
