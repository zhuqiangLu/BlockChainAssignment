import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class BlockchainServer {

    public static void main(String[] args) {

        if (args.length != 3) {
            return;
        }

        int localPort = 0;
        int remotePort = 0;
        String remoteHost = null;

        try {
            localPort = Integer.parseInt(args[0]);
            remoteHost = args[1];
            remotePort = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return;
        }

        Blockchain blockchain = new Blockchain();

        
        HashMap<ServerInfo, Date> serverStatus = new HashMap<ServerInfo, Date>();
        Map<ServerInfo, Date> SynServerStatus = Collections.synchronizedMap(serverStatus);
        //add a neightbour which the user provides
        serverStatus.put(new ServerInfo(remoteHost, remotePort), new Date());

        //then catch up
        new SyncHandlerRunnable(remoteHost, blockchain).catchUp(remotePort);
        

        //have a new thread to handle the periodic commit
        PeriodicCommitRunnable pcr = new PeriodicCommitRunnable(blockchain);
        Thread pct = new Thread(pcr);
        pct.start();

        //set up heart beat
        new Thread(new ServerHeartBeatObserver(SynServerStatus)).start();
        new Thread(new ServerHeartBeatSenderRunnable(SynServerStatus, localPort)).start();

        
        //set up head hash sender 
        new Thread(new PeriodicHashSenderRunnable(serverStatus, localPort, blockchain, 1)).start();
        

        //wait for the client connection
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(localPort);

            while (true) {
               
                Socket clientSocket = serverSocket.accept();
                new Thread(new BlockchainServerRunnable(clientSocket,localPort, blockchain, SynServerStatus)).start();
            }
        } catch (IllegalArgumentException e) {
        } catch (IOException e) {
        } finally {
            try {
                pcr.setRunning(false);
                pct.join();
                if (serverSocket != null)
                    serverSocket.close();
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }
        }
    }


    

}


