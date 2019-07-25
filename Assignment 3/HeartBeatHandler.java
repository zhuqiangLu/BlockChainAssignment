import java.net.Socket;
import java.util.*;
import java.net.InetSocketAddress;
public class HeartBeatHandler{
    private Socket clientSocket;
    private String line;
    private int localPort;
    private Map<ServerInfo, Date> serverStatus;
    public HeartBeatHandler(Socket client, String line, int localPort, Map<ServerInfo, Date> serverStatus){
        this.clientSocket = client;
        this.line = line;
        this.localPort = localPort;
        this.serverStatus = serverStatus;
    }

    public void handle(){
        try{
            //get client ip and local ip
            String localIP = (((InetSocketAddress) clientSocket.getLocalSocketAddress()).getAddress()).toString().replace("/", "");
            String remoteIP = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
            
            
            //split it
            String[] tokens = line.split("\\|");
            //System.out.println(line);
            switch(tokens[0]){
                //when a server receives a hb message, it log it
                case "hb":
                    int remotePort = Integer.parseInt(tokens[1]);
                    //log it
                    serverStatus.put(new ServerInfo(remoteIP, remotePort), new Date());
                    
                    
                    //if it is the first time seen
                    if(Integer.parseInt(tokens[2]) == 0){
                        ArrayList<Thread> threads = new ArrayList<Thread>();
                        //broadcast it to all other neighbour
                        String message = String.format("si|%d|%s|%d", localPort, remoteIP, remotePort);
                        //find all peers that are not P
                        for(ServerInfo peer : serverStatus.keySet()){
                            //don't send to yourself or the newbee
                            if((peer.getHost().equals(remoteIP) && peer.getPort() == remotePort)||
                                (peer.getHost().equals(localIP) && peer.getPort() == localPort)){
                                continue;
                            }
                            else{
                                Thread thread = new Thread(new SenderRunnable(peer, message));
                                threads.add(thread);
                                thread.start();
                            }
                            
                        }
                        //join them
                        for(Thread thread : threads){
                            thread.join();
                        }
                    }
                    break;
                
                case "si":
                    String newIP = tokens[2];
                    int newPort = Integer.parseInt(tokens[3]);
                    boolean newBee = true;
                    remotePort = Integer.parseInt(tokens[1]);
                    //see if it's new
                    for(ServerInfo peer : serverStatus.keySet()){
                        if(peer.getHost().equals(newIP) && peer.getPort() == newPort){
                            newBee = false;
                        }
                        
                    }
                    //if new then relay
                    if(newBee){
                        //serverStatus.put(new ServerInfo(newIP, newPort), new Date());
                        String message = String.format("si|%d|%s|%d", localPort, newIP, newPort);
                        ArrayList<Thread> threads = new ArrayList<Thread>();
                        //see if the server info is known or not
                        for(ServerInfo peer : serverStatus.keySet()){
                            String pIP = peer.getHost();
                            int pPort = peer.getPort();
                            //do not notify the newbee/originator/yourself
                            if((pIP.equals(newIP) && pPort ==  newPort)||
                               (pIP.equals(remoteIP)  && pPort == remotePort) ||
                               (pIP.equals(localIP) && pPort == localPort)
                            ){
                                continue;
                            }
                            else{
                                Thread thread = new Thread(new SenderRunnable(peer, message));
                                threads.add(thread);
                                thread.start();;
                            }

                        }

                        //join them
                        for(Thread thread : threads){
                            thread.join();
                        }
                    }
                
                    break;


            }
        }catch(Exception e){
            System.err.println(e);
        }
    }
}