import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class DummyServer implements Runnable {
    private Blockchain dummyChain;
    private ServerSocket server;
    private Socket client;
    private String line = "default message";
    private String clientMes = null;
    private boolean dumb = false;
    private boolean deaf = false;
    private boolean running = true;

    public DummyServer(int port){
        dummyChain = new Blockchain();

        try{
            server = new ServerSocket(port);

        }catch(IOException e){
            System.out.println("cannot create a server socket");
        }


    }


    public void run(){

            try{
                client = server.accept();
                BufferedReader inReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter outWriter = new PrintWriter(client.getOutputStream(), true);
                if(dumb){
                    //do nothing
                    //meant to trigger timeout
                    System.out.println("pikachu!");
                    while(running){
                        //stub
                    }
                    System.out.println("!pikachu!");

                }
                else if(deaf){
                    //doesnt read
                    while(inReader.readLine()!=null){}
                    outWriter.print(line);
                    outWriter.flush();
                }
                else {

                    String message = "";
                    while ((message = inReader.readLine()) != null) {
                        if (clientMes == null) {
                            clientMes = "";
                        }
                        clientMes += message;
                    }
                    outWriter.print(line);
                    outWriter.flush();

                }
                outWriter.close();
                inReader.close();
                client.close();

            }catch(Exception e){
                System.err.println(e);
            }


    }

    public ServerSocket getSocket(){ return server; }

    public String whatClientSays(){
        return clientMes;
    }

    public void dumbMode(){
        dumb = true;
    }
    public void notDumb(){
        dumb = false;
    }
    public void deafMode(){
        deaf = true;
    }
    public void notDeaf(){
        deaf = false;
    }
    public void stop(){
        running = false;
    }



    public void sayToClient(String line){
        this.line = line;

    }

    public void closeServer(){
        try {
            this.server.close();
        }catch(Exception e){
            return;
        }
    }


}
