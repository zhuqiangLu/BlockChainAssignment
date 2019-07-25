import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.*;

public class BlockchainServerRunnable implements Runnable{

    private Socket clientSocket;
    private Blockchain blockchain;
    private int localPort;
    private Map<ServerInfo, Date> serverStatus;

    public BlockchainServerRunnable(Socket clientSocket, int localPort,Blockchain blockchain, Map<ServerInfo, Date> serverStatus) {
        this.clientSocket = clientSocket;
        this.blockchain = blockchain;
        this.serverStatus = serverStatus;
        this.localPort = localPort;
    }

    public void run() {
        try {
            serverHandler(clientSocket);
            clientSocket.close();
        } catch (IOException e) {
        }
    }

    public void serverHandler(Socket client) {
       
        try {

            BufferedReader inputReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter outWriter = new PrintWriter(client.getOutputStream(),true);
            
           

            //get client ip and local ip
            String localIP = (((InetSocketAddress) clientSocket.getLocalSocketAddress()).getAddress()).toString().replace("/", "");
            String remoteIP = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");

            while (true) {
                
                String inputLine = inputReader.readLine();
                if (inputLine == null) {
                    break;
                }

              
                String[] tokens = inputLine.split("\\|");
            
                
                switch (tokens[0]) {
                    case "hb":
                        new HeartBeatHandler(clientSocket, inputLine, localPort, serverStatus).handle();
                        break;

                    case "si":
                        new HeartBeatHandler(clientSocket, inputLine, localPort, serverStatus).handle();
                        break;

                    case "cu":
                        
                        ObjectOutputStream oout = new ObjectOutputStream(client.getOutputStream());
                        new CatchUpHandler(oout, blockchain, inputLine).handle();
                        break;

                    case "lb": 
                    
                        new SyncHandlerRunnable(remoteIP, blockchain).handle(inputLine);
                        
                        break;

                    case "tx":
                        if (blockchain.addTransaction(inputLine))
                            outWriter.print("Accepted\n\n");
                        else
                            outWriter.print("Rejected\n\n");
                            outWriter.flush();
                        break;

                    case "pb":
                        outWriter.print(blockchain.toString() + "\n");
                        outWriter.flush();
                        break;

                    case "cc":
                        return;

                    default:
                        outWriter.print("Error\n\n");
                        outWriter.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } 
    }


    
}
