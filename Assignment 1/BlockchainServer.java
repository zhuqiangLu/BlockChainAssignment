import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
public class BlockchainServer {
    private Blockchain blockchain;

    public BlockchainServer() { blockchain = new Blockchain(); }

    // getters and setters
    public void setBlockchain(Blockchain blockchain) { this.blockchain = blockchain; }
    public Blockchain getBlockchain() { return blockchain; }

    public static void main(String[] args) {
        if (args.length != 1) {
            return;
        }
        int portNumber = Integer.parseInt(args[0]);
        BlockchainServer bcs = new BlockchainServer();

        // TODO: implement your code here.
        ServerSocket server;
        try{
          //create a socket
          server = new ServerSocket(portNumber);
          //keep it listen to the given port
          while(true){
            //one client each time
            Socket socket = server.accept();
            bcs.serverHandler(socket.getInputStream(), socket.getOutputStream());
            socket.close();
          }

        }catch(Exception e){ return; }

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
              if(blockchain.addTransaction(input) == 0){
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
