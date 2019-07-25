import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
public class BlockchainServer {
    private  Blockchain blockchain;
    private ServerSocket server;
    private boolean isRuning = true;
    public BlockchainServer() {
        blockchain = new Blockchain();
        server = null;
    }

    // getters and setters
    public void setBlockchain(Blockchain blockchain) { this.blockchain = blockchain; }

    public Blockchain getBlockchain() { return blockchain; }

    public ServerSocket getServer(){return server;}

    public boolean isRuning(){ return isRuning;}

    public void stop(){ isRuning = false; }

    public void createSocket(int port){
        try {
            server = new ServerSocket(port);
        }catch(IOException e){
            server = null;
        }catch(IllegalArgumentException e){
            server = null;
        }
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            return;
        }

        // TODO: ASSIGNMENT 2
        int portNumber;
        try {
            portNumber = Integer.parseInt(args[0]);
        }catch(NumberFormatException e){
            return;
        }

        Blockchain blockchain = new Blockchain();

        PeriodicCommitRunnable pcr = new PeriodicCommitRunnable(blockchain);
        Thread pct = new Thread(pcr);
        pct.start();



        //TODO: ASSIGNMENT 1
        boolean running = true;
        try{
          //create a socket
            BlockchainServer bcs = new BlockchainServer();
            bcs.createSocket(portNumber);


          //keep it listen to the given port
          while(bcs.isRuning()){
            //one client each time
            Socket clientSocket = bcs.getServer().accept();
            BlockchainServerRunnable bsr = new BlockchainServerRunnable(clientSocket, blockchain);
            Thread bst = new Thread(bsr);
            bst.start();
          }
            pcr.setRunning(false);
            pct.join();

        }catch(Exception e){
            return;
        }

    }

}
