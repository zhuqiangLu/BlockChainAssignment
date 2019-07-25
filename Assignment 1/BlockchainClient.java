import java.io.*;
import java.util.Scanner;
import java.net.Socket;
import java.net.SocketTimeoutException;
public class BlockchainClient {
    public static void main(String[] args) {

        if (args.length != 2) {
            return;
        }
        String serverName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        BlockchainClient bcc = new BlockchainClient();

        try{
          Socket socket = new Socket(serverName,portNumber);
          socket.setSoTimeout(500);
          bcc.clientHandler(socket.getInputStream(), socket.getOutputStream());

          socket.close();
        }
        catch(Exception e){

          return;
         }

        // TODO: implement your code here.
    }

    public void clientHandler(InputStream serverInputStream, OutputStream serverOutputStream) {
        BufferedReader inputReader = new BufferedReader(
                new InputStreamReader(serverInputStream));
        PrintWriter outWriter = new PrintWriter(serverOutputStream, true);

        Scanner sc = new Scanner(System.in);
        try{
          while (sc.hasNextLine()) {
              // TODO: implement your code here

              String in = sc.nextLine();

              outWriter.print(in+"\n");
              outWriter.flush();

              String serverMsg;
              try{
                while((serverMsg = inputReader.readLine())!= null ){
                  System.out.println(serverMsg);
                  if(!inputReader.ready()){ break;}
                }
              }catch(SocketTimeoutException e){
                break;
              }


              if(in.equals("cc")){
                inputReader.close();
                outWriter.close();
                return;
              }





          }
        }catch(Exception e){
          return;
        }

    }

    // implement helper functions here if you need any.
}
