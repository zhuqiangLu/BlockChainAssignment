import java.io.*;
import java.net.Socket;
public class BlockchainServerRunnable implements Runnable {
    private Blockchain blockchain;
    private Socket clientSocket;
    public BlockchainServerRunnable(Socket clientSocket, Blockchain blockchain) {
        this.clientSocket = clientSocket;
        this.blockchain = blockchain;
    }
    @Override
    public void run(){
        try{
            this.serverHandler(clientSocket.getInputStream(), clientSocket.getOutputStream());
            clientSocket.close();
        }catch(IOException e){
            System.err.println(e);
        }
    }

    public void serverHandler(InputStream clientInputStream, OutputStream clientOutputStream) {

        BufferedReader inputReader = new BufferedReader(
                new InputStreamReader(clientInputStream));
        PrintWriter outWriter = new PrintWriter(clientOutputStream, true);

        String input;
        try{

            while((input = inputReader.readLine()) != null ){
                //give blockchain if pb
                if(input.equals("pb")){
                    outWriter.print(blockchain.toString() + '\n');
                    outWriter.flush();
                }
                //see if it a txstring
                else if(checkTx(input)){
                    //check if the input a valid transaction
                    if(!blockchain.addTransaction(input)){
                        outWriter.print("Rejected\n\n");
                        outWriter.flush();

                    }
                    else{
                        outWriter.print("Accepted\n\n");
                        outWriter.flush();

                    }
                }
                //quit if cc
                else if(input.equals("cc")){
                    outWriter.flush();
                    inputReader.close();
                    outWriter.close();
                    return;
                }
                else{
                    outWriter.print("Error\n\n");
                    outWriter.flush();
                }

            }

        }catch(Exception e){
            return;
        }

        // TODO: implement your code here.

    }

    // implement helper functions here if you need any.
    private boolean checkTx(String t){
        String pattern = "tx.*";
        return(t.matches(pattern));
    }
}
