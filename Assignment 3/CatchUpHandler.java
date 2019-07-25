import java.io.ObjectOutputStream;
import java.util.*;
import java.net.Socket;

public class CatchUpHandler{
    /*
        this class is to handle the cu message
    */
    private ObjectOutputStream writer;
    private Blockchain chain;
    private byte[] hash;

    public CatchUpHandler(ObjectOutputStream out, Blockchain chain, String message){
        this.chain = chain;
        this.writer = out;
        parse(message);

    }

    private void parse(String message){
        if(message.equals("cu")){
            this.hash = null;
        }
        else{
            String[] tokens = message.split("\\|");
            
            this.hash = Base64.getDecoder().decode(tokens[1]);
        }
    }
    public void handle(){
        try{
            
            //we assume that the requested block always exists
            Block block = chain.getHead();
            while(block != null){
                if(hash == null){
                    
                    writer.writeObject(copyBlock(chain.getHead()));
                    writer.flush();
                    return;
                }
                //if we found the wanted block, then we give that block only
                if(Arrays.equals(block.calculateHash(), hash)){
                    
                    writer.writeObject(copyBlock(block));
                    writer.flush();
                    return;
                }
                block = block.getPreviousBlock();
            }
            writer.writeObject(null);
            writer.flush();
            
        }catch(Exception e){
            e.printStackTrace(System.out);
        }
        
    }


    private Block copyBlock(Block block){
        Block replica = new Block();
        replica.setCurrentHash(block.calculateHash());
        replica.setPreviousBlock(null);//set to null to increase efficiency
        replica.setPreviousHash(block.getPreviousHash());
        replica.setTransactions(block.getTransactions());
        return replica;
    }
}