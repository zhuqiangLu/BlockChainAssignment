import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.Map.Entry;
public class ServerHeartBeatObserver implements Runnable{

    private Map<ServerInfo, Date> serverStatus;

    public ServerHeartBeatObserver(Map<ServerInfo, Date> serverStatus){
        this.serverStatus = serverStatus;
    }

    @Override
    public void run(){
        
        while(true){
            ArrayList<ServerInfo> removeList = new ArrayList<ServerInfo>();
            for (ServerInfo peer : serverStatus.keySet()) {
                // if greater than 2T, remove
                Date date = serverStatus.get(peer);
                if ((new Date().getTime() - date.getTime()) > 4000) {
                    removeList.add(peer);
                    
                }
            }

            for(ServerInfo peer : removeList){
                serverStatus.remove(peer);
            
            }
            

            // sleep for two seconds
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                //System.err.println(e);
            }

        }
    }
}