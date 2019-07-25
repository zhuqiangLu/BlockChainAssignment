import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collections;
public class ServerInfoList {
    ArrayList<ServerInfo> serverInfos;

    public ServerInfoList(){
        serverInfos = new ArrayList<>();
    }

    public void initialiseFromFile(String filename) {
        //implement here
        try{
            //set up variables
            FileReader fr = new FileReader(filename);


            BufferedReader br = new BufferedReader(fr);
            int MaxserverNum = 0;
            int currentIndex = 0;
            boolean read = true;
            String next = null;
            while(true){
                //if need to read, then get nextline
                if(read) {
                    next = getNextValidEntry(br);

                    if(next == null){
                        break;//break if reach eof
                    }


                }
                if(next == null){
                    break;
                }
                //if next line indicates max server number, then update max server number, and set read to be true
                if(getServerNum(next) != -1){
                    MaxserverNum = getServerNum(next);
                    read = true;
                }
                //if next line is not max server number, must be host or port
                else{
                    String host = next;//assume it is host

                    String port = getNextValidEntry(br); //get next line
                    int serverIndex = 0;
                    //if the these two lines are in pair and have same server index
                    if(isPair(host, port)){
                        String s = host;
                        String s2 = port;
                        host = getHostFromPair(s, s2);
                        port = getPortFromPair(s, s2);



                    }
                    if(isPair(host, port) && (serverIndex = getServerIndex(host, port))!= -1){
                        String hostName = getHostName(host);
                        int portNum = getPortNumber(port);
                        ServerInfo newServer = new ServerInfo(hostName, portNum);

                        //check if the server out of bound
                        if(serverIndex < MaxserverNum ) {
                            if (serverIndex == currentIndex) {
                                this.addServerInfo(newServer);
                                currentIndex++;
                            } else {
                                this.updateServerInfo(serverIndex, newServer);

                            }

                        }
                        else{
                            break;
                        }
                        read = true;
                    }
                    else{
                        //assume host always comes before port
                        int tempIndex = HostIndex(host);

                        if(tempIndex!= -1 ){
                            if(tempIndex == currentIndex){
                                serverInfos.add(HostIndex(host), null);
                                currentIndex++;
                            }
                            if(tempIndex < currentIndex){
                                ServerInfo s = serverInfos.get(tempIndex);
                                if(s != null){
                                    s.setHost(getHostName(host));
                                    this.updateServerInfo(tempIndex, s);
                                }

                            }

                        }
                        else{
                            tempIndex = PortIndex(host);
                            if(tempIndex != -1){
                                if(tempIndex == currentIndex){
                                    serverInfos.add(tempIndex, null);
                                    currentIndex++;
                                }
                                if(tempIndex < currentIndex){
                                    ServerInfo s = serverInfos.get(tempIndex);
                                    if(s!=null) {
                                        s.setPort(getPortNumber(host));
                                        this.updateServerInfo(tempIndex, s);
                                    }
                                }

                            }
                        }

                        next = port;
                        read = false;

                    }
                }
            }

        }catch(IOException e){
            return;
        }
    }

    public ArrayList<ServerInfo> getServerInfos() {return serverInfos; }

    public void setServerInfos(ArrayList<ServerInfo> serverInfos){
        this.serverInfos = serverInfos;
    }

    public boolean addServerInfo(ServerInfo newServerInfo){
        //implement here
        if(isValidPort(newServerInfo) && isValidHost(newServerInfo)){
            serverInfos.add(newServerInfo);
            return true;
        }
        else{
            serverInfos.add(null);
            return false;
        }
    }

    public boolean updateServerInfo(int index, ServerInfo newServer){
        //impement here
        try{
            if(isValidPort(newServer)&&isValidHost(newServer)){
                serverInfos.set(index,newServer);
                return true;
            }
            else{
                serverInfos.set(index,null);
                return false;
            }

        }
        catch(IndexOutOfBoundsException e){
            return false;
        }
    }

    public boolean removeServerInfo(int index) {
        // implement your code here
        try{
            serverInfos.set(index, null);
            return true;
        }catch(Exception e){
            return false;
        }

    }

    public boolean clearServerInfo() {
        // implement your code here
        return serverInfos.removeAll(Collections.singleton(null));

    }

    public String toString() {
        String s = "";
        for (int i = 0; i < serverInfos.size(); i++) {
            if (serverInfos.get(i) != null) {
            s += "Server" + i + ": " + serverInfos.get(i).getHost() + " " + serverInfos.get(i).getPort() + "\n";
            }

        }
        return s;
    }

    private int getServerNum(String line){
        Pattern p = Pattern.compile("(servers\\.num)(\\=)(\\d+)");
        Matcher m = p.matcher(line);
        if(m.find()){
            return Integer.parseInt(m.group(3));
        }
        else{
            return -1;
        }
    }

    private String getHostName(String line){
        Pattern p = Pattern.compile("(server)(\\d+)(\\.host)(\\=)(.*)");
        Matcher m = p.matcher(line);
        if(m.find()){

            return m.group(5);


        }
        else{
            return null;
        }
    }

    private int getPortNumber(String line){
        Pattern p = Pattern.compile("(server)(\\d+)(\\.port)(\\=)(\\d+)");
        Matcher m = p.matcher(line);
        if(m.find()){
            return Integer.parseInt(m.group(5));
        }
        else{
            return -1;
        }
    }

    private String getNextValidEntry(BufferedReader br) {
        String line = null;
        try{
            while( (line = br.readLine())!=null ){
                line = line.replace(" ", "");
                if(line.equals("")||(
                        !line.matches("server\\d+\\.host\\=.*")&&
                        !line.matches("server\\d+\\.port\\=\\d+")&&
                        !line.matches("servers\\.num\\=\\d+")
                        )){
                    continue;
                }
                else{
                    return line;
                }
            }
        }catch(IOException e){
            return null;
        }
       return null;
    }


    private boolean isPair(String host, String port){
        if(host == null || port == null) { return false;}
        return (host.matches("server\\d+\\.host\\=.*") && port.matches("server\\d+\\.port\\=\\d+"))||
                (host.matches("server\\d+\\.port\\=\\d+") && port.matches("server\\d+\\.host\\=.*"));
    }

    private int PortIndex(String port){
        Pattern pPort = Pattern.compile("(server)(\\d+)(\\.port)(\\=)(\\d+)");
        Matcher mPort = pPort.matcher(port);
        if(mPort.find()) {
            return Integer.parseInt(mPort.group(2));
        }
        else{
            return -1;
        }

    }

    private int HostIndex(String host){
        Pattern pHost = Pattern.compile("(server)(\\d+)(\\.host)(\\=)(.*)");
        Matcher mHost = pHost.matcher(host);
        if(mHost.find()){
            return Integer.parseInt(mHost.group(2));
        }
        else{
            return -1;
        }


    }

    private int getServerIndex(String host, String port){
        int pi = PortIndex(port);
        int hi = HostIndex(host);
        if( pi == hi && hi != -1){
            return pi;
        }
        return -1;
    }

    private boolean isValidPort(ServerInfo server){
        if(server == null){return false;}
        int portNum = server.getPort();
        String hostName = server.getHost();
        if(portNum > 65535 || portNum < 1024){
            return false;
        }
        else if(hostName == null || hostName.isEmpty()){
            return false;
        }
        else{
            return true;
        }

    }

    private boolean isValidHost(ServerInfo server){
        if(server == null){return false;}
        String host = server.getHost();
        boolean str = (host.matches("localhost"));
        if(str){return true;}
        boolean ip = false;
        String[] fields = host.split("\\.");
        if(fields.length!=4){
            return false;
        }
        for(String field: fields){
            int intField = Integer.parseInt(field);
            if(intField < 0 || intField > 255){
                return false;
            }
        }
        return true;


    }

    private String getHostFromPair(String s, String s2){
        if(s.matches(("server\\d+\\.host\\=.*"))){
            return s;
        }
        else{
            return s2;
        }
    }

    private String getPortFromPair(String s, String s2){
        if(s2.matches(("server\\d+\\.port\\=\\d+"))){
            return s2;
        }
        else{
            return s;
        }
    }

}
