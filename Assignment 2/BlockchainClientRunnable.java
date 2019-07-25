import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class BlockchainClientRunnable implements Runnable {

    private String reply = "";
    private String header;
    Socket socket= null;
    String mes = null;
    public BlockchainClientRunnable(int serverNumber, String serverName, int portNumber, String message) {
        this.header = "Server" + serverNumber + ": " + serverName + " " + portNumber + "\n"; // header string
        mes = message;
        try{

            socket = new Socket(serverName,portNumber);
            socket.setSoTimeout(2000);
        }catch(IOException e){
            reply  = "Server is not available\n\n";
        }


    }

    public void run() {
        // implement your code here
        try{
            clientHandler(socket.getInputStream(), socket.getOutputStream(), mes);
        }catch(Exception e){
            reply =  "Server is not available\n\n";
        }
    }

    public String getReply() {
        return header + reply;
    }

    // implement any helper method here if you need any

    public void clientHandler(InputStream serverInputStream, OutputStream serverOutputStream, String message) {
        BufferedReader inputReader = new BufferedReader(
                new InputStreamReader(serverInputStream));
        PrintWriter outWriter = new PrintWriter(serverOutputStream, true);
        //send message to server
        outWriter.print(message + "\n");
        outWriter.flush();
        String serverMsg;
        try{
            if(message.equals("cc")){
                inputReader.close();
                outWriter.close();
                socket.close();
                reply = "";
            }
            while((serverMsg = inputReader.readLine())!= null ){
                System.out.println(serverMsg);
                reply = reply+""+ serverMsg + "\n";
                if(!inputReader.ready()){ break;}
            }
            inputReader.close();
            outWriter.close();
            socket.close();

        }catch(Exception e){
            reply =  "Server is not available\n\n";
        }


    }
}
