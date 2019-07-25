import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
public class ServerHeartBeatSenderRunnable implements Runnable{

    private Map<ServerInfo, Date> serverStatus;
    private int sequence;
    private int serverPort;

    /*
        this class will notify all (given) peers the every 2 secs
    */
    public ServerHeartBeatSenderRunnable(Map<ServerInfo, Date> serverStatus, int port){
        this.serverStatus = serverStatus;
        this.sequence = 0;//initiall sequence is 0
        this.serverPort = port;
    }

    @Override
    public void run(){
        //keep broadcast sequence to bother neighbours
        while(true){
            ArrayList<Thread> threads = new ArrayList<Thread>();
            
            //construct the string
            String message = String.format("hb|%d|%d", serverPort, sequence);

            //have a thread to send heart beat
            for(ServerInfo peer : serverStatus.keySet()){
                //pass it to the client runnable
                Thread thread = new Thread(new SenderRunnable(peer, message));
                threads.add(thread);
                thread.start();
            }

            //wait for the completion
            for(Thread thread : threads){
                try{
                    thread.join();
                }catch(Exception e){}
              
               
            }

            //increment the sequence by 1
            sequence += 1;

            //sleep for 2 secs
            try{
                Thread.sleep(2000);
            }catch(InterruptedException e){
                //System.err.println(e);
            }

        }
    }



}