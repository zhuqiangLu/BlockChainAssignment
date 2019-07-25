import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DummyClient {

   private ServerInfoList pl;
   private String mes = "pikachu";

   public void initialize(){ pl = new ServerInfoList(); }

   public void mesToSevers(String msg){
       if(mes == null){
           return;
       }
       else{
           this.mes = mes;
       }

   }
   public void addSever(String host, int port){
       pl.addServerInfo(new ServerInfo(host, port));
   }

    public void unicast (int serverNumber, ServerInfo p, String message) {
        // implement your code here
        BlockchainClientRunnable bcr = new BlockchainClientRunnable(serverNumber, p.getHost(), p.getPort(), message);
        Thread bct = new Thread(bcr);
        bct.start();

        try{
            bct.join();
            if(!message.equals("cc")){
                System.out.print(bcr.getReply());
            }
        }catch(InterruptedException e){
            return;
        }
    }

    public void broadcast (ServerInfoList pl, String message) {
        // implement your code here
        ArrayList<ServerInfo> si = pl.getServerInfos();
        for(int i = 0; i < si.size(); i++){
            ServerInfo p = si.get(i);
            if(p!=null){
                unicast(i, p, message);
            }
        }
    }

    public void multicast (ServerInfoList serverInfoList, ArrayList<Integer> serverIndices, String message) {
        // implement your code here
        ArrayList<ServerInfo> si = serverInfoList.getServerInfos();
        for(Integer i: serverIndices){
            ServerInfo p = si.get(i);
            if(p!=null){
                unicast(i, p, message);
            }
        }
    }
}
