import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.*;



public class SyncHandlerRunnable implements Runnable{
    private String remoteIP;
    private Blockchain chain;
    private int remotePort;
    private int size;
    private byte[] hash;
    private simpleStack blocks;
    public SyncHandlerRunnable(String remoteIP,  Blockchain chain){
        this.chain = chain;
        this.remoteIP = remoteIP;
        
    }
    public void catchUp(int remotePort){
        this.remotePort = remotePort;
        syncChain();
    }

    public void handle(String message){

        String[] tokens = message.split("\\|");
        this.remotePort = Integer.parseInt(tokens[1]);
        this.size = Integer.parseInt(tokens[2]);
        this.hash = Base64.getDecoder().decode(tokens[3]);

        //all good
        if(chain.getLength() > size){
            return;
        }
        //request for block, start from the latest block
        else if(chain.getLength() < size){
            new Thread(this).start();
        }
        else if(chain.getLength() == size && Arrays.compare(hash, chain.getHead().calculateHash())<0){
            new Thread(this).start();
        }
        else{
            return;
        }

        //same length or peer has a better hash
    }

    @Override
    public void run(){
        syncChain();
    }

    private synchronized void syncChain(){
        blocks = new simpleStack();
        try{
            Socket peer = new Socket();
            peer.connect(new InetSocketAddress(remoteIP, remotePort), 2000);
           
            InputStream in = peer.getInputStream();
            OutputStream out = peer.getOutputStream();
            
            //as the input is to get object and out is to write signiture
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(in));
            PrintWriter outWriter = new PrintWriter(out, true);

            Block agreePoint = null;


            while(true){
                if(hash != null){
                    outWriter.println(String.format("cu|%s", Base64.getEncoder().encodeToString(hash)));
                    outWriter.flush();
                }
                else{
                    outWriter.println("cu");
                    outWriter.flush();
                }
                
                
                //read the incoming block
                ObjectInputStream Oin = new ObjectInputStream(in);
                Block block = (Block)Oin.readObject();
               
                //empty or the end then stop
                if(block == null){
                    outWriter.println("cc");
                    outWriter.flush();
                    break;
                }
                
                blocks.push(block);
                //update the wanted block's hash
                hash = block.getPreviousHash();
                
                //if the block chain is empty, then pull all blocks from peer
                if(chain.getLength() == 0){
                    continue;
                }
                
                //to find the block that do not need to be replaced in this server
                //start from the head
                agreePoint = chain.getHead();
                
               
                //find whether there is an agree point 
                while(!Arrays.equals(this.hash, agreePoint.calculateHash())){
                    agreePoint = agreePoint.getPreviousBlock();
                    //reach the end
                    if(agreePoint == null){
                        break;
                    }
                    
                }

                //if an agreepoint is not null
                if(agreePoint != null){
                    break;
                }
            
            }

            //there is no need to destruct if the chian is empty
            if(chain.getLength() != 0){
                 //to sync
                //all block before agree point should be desctruct
                while(!Arrays.equals(chain.getHead().calculateHash(), agreePoint.calculateHash())){
                    Block toDestruct = chain.getHead();
                    chain.setHead(toDestruct.getPreviousBlock());
                    //pull the tx back to the pool
                    for(Transaction tx : toDestruct.getTransactions() ){
                        chain.addTransaction("tx|" +tx.getSender() +"|"+tx.getContent());
                    }
                    //reduce length
                    chain.setLength(chain.getLength() - 1);
                }

            }
            
            //update the chain
            while(!blocks.isEmpty()){
                Block toSync = blocks.pop();
                toSync.setPreviousBlock(chain.getHead());
                chain.setHead(toSync);
                chain.setLength(chain.getLength() + 1);
            }

            
            peer.close();

        }catch(Exception e){
            //e.printStackTrace(System.out);
        }
       
    }


    

    public class simpleStack{
        ArrayList<Block> stack = new ArrayList<>();
        int i = -1;
        public void push(Block block){
            stack.add(block);
            i++;
        }
        public Block pop(){
            Block block = stack.remove(i);
            i--;
            return block;
        }
        public boolean isEmpty(){
            if(i == -1){
                return true;
            }
            else{
                return false;
            }
        }
    }
   


    
}