import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SenderRunnable implements Runnable{

    private ServerInfo peerInfo;
    private String message;
    /* 
        this class will notify a given peer with the given port and sequence number
    */
    public SenderRunnable(ServerInfo peerInfo, String message){
        this.peerInfo = peerInfo;
        this.message = message;
    }

    @Override 
    public void run(){
        try{
            Socket peer = new Socket();
            //connect to the peer and set a timeout of 2000 -> 2 sec
            peer.connect(new InetSocketAddress(peerInfo.getHost(), peerInfo.getPort()), 2000);
            //obtain output stream of peer
            PrintWriter writer = new PrintWriter(peer.getOutputStream(), true);

            //send the sequence number with the port
            writer.print(this.message);
            writer.flush();

            //close everything
            writer.close();
            peer.close();
            
        }catch(Exception e){
            //System.err.println(e);
        }
    }

    
}