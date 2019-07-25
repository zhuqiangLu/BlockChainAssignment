import java.io.*;
import java.util.Scanner;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockchainClient {
    public static void main(String[] args) {

        if (args.length != 1) {
            return;
        }
        ServerInfoList pl = new ServerInfoList();
        String configFileName = args[0];
        if(configFileName != null){

            pl.initialiseFromFile(configFileName);
        }


        Scanner sc = new Scanner(System.in);
        BlockchainClient bcc = new BlockchainClient();

        while (true) {
            if(sc.hasNext()){
                String message = sc.nextLine();

                if(!bcc.command(message, pl)){
                    break;
                }
            }
            else{
                break;
            }


        }

    }

    private boolean command(String message, ServerInfoList pl){
        if(message.matches("(ad\\|)(.*)(\\|)(\\d+)")){ ad(message,pl); }
        else if(message.matches("ls")) {ls(pl);}
        else if(message.matches("(rm\\|)((-\\d+)|(\\d+))")){ rm(message, pl);}
        else if(message.matches("(up\\|)(\\d+)(\\|)(.*)(\\|)(\\d+)")) {up(message, pl);}
        else if(message.matches("cl")) {cl(pl);}
        else if(message.matches("tx.*")){ tx(message, pl); }
        else if(message.matches("(pb)((\\|\\d+)*)$")) { pb(message, pl); }
        else if(message.matches("sd")) { sd(pl); return false;}
        else{ System.out.println("Unknown Command\n"); }
        return true;
    }

    private void ls(ServerInfoList pl){
        System.out.println(pl.toString());
    }

    private void ad(String line, ServerInfoList pl){
        System.out.println("called");
        Pattern add = Pattern.compile("(ad\\|)(.*)(\\|)(\\d+)");
        Matcher m = add.matcher(line);
        if(m.find()){
            String host = m.group(2);
            Integer port = Integer.parseInt(m.group(4));

            if(pl.addServerInfo(new ServerInfo(host, port))){
                System.out.println("Succeeded\n");
            }
            else{
                System.out.println("Failed\n");
            }
        }

    }

    private void rm(String line, ServerInfoList pl){
        Pattern rm = Pattern.compile("(rm\\|)((-\\d+)|(\\d+))");

        Matcher m = rm.matcher(line);

        if(m.find()){
            int i = Integer.parseInt(m.group(2));
            if(i > -1 && i < pl.getServerInfos().size()){
                if(pl.removeServerInfo(i)){
                    System.out.println("Succeeded\n");
                }
                else{
                    System.out.println("Failed\n");
                }
            }
            else{
                System.out.println("Failed\n");
            }
        }
    }

    private void up(String line, ServerInfoList pl){
        Pattern up = Pattern.compile("(up\\|)(\\d+)(\\|)(.*)(\\|)(\\d+)");
        Matcher m = up.matcher(line);
        if(m.find()){
            int i = Integer.parseInt(m.group(2));
            String host = m.group(4);
            Integer port = Integer.parseInt(m.group(6));
            if(pl.updateServerInfo(i, new ServerInfo(host, port))){
                System.out.println("Succeeded\n");
            }
            else{
                System.out.println("Failed\n");
            }
        }

    }

    private void cl(ServerInfoList pl){
        if(pl.clearServerInfo()){
            System.out.println("Succeeded\n");
        }
        else{
            System.out.println("Failed\n");
        }
    }

    private void tx(String line ,ServerInfoList pl){
        this.broadcast(pl, line);
    }

    private void pb(String line, ServerInfoList pl){
        Pattern pb = Pattern.compile("(pb)((\\|\\d+)*)$");
        Matcher m = pb.matcher(line);
        m.find();

        if(m.group(2).equals("")){
            broadcast(pl, line);

        }
        else{
            String is = m.group(2);
            int i = 1;
            ArrayList<Integer> indices = new ArrayList<>();
            while((i+1)<=is.length() ){
                Integer id = Character.getNumericValue(is.charAt(i));
                if(id < pl.getServerInfos().size()){
                    indices.add(id);
                }

                i += 2;
            }

            multicast(pl, indices, "pb");

        }

    }

    private void sd( ServerInfoList pl) { broadcast(pl, "cc");}

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