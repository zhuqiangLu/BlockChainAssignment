
import java.util.*;




public class PeriodicHashSenderRunnable implements Runnable{
    private int peerNum;
    private Blockchain chain;
    private Map<ServerInfo, Date> serverStatus;
    private int localPort;
    private boolean running = true;
    
    public PeriodicHashSenderRunnable(Map<ServerInfo, Date> serverStatus, int localPort, Blockchain chain, int peerNum){
        this.serverStatus = serverStatus;
        this.chain = chain;
        this.peerNum = peerNum;
        this.localPort = localPort;
    }

    @Override
    public void run(){
        while(running){
            sendHeadHash(peerNum);

            //sleep for a desire period of time
            try{
                Thread.sleep(2000);
            }catch(InterruptedException e){
                //System.err.println(e);
            }

        }
    }

    private void stop(){
        this.running = false;
    }

    

    private void sendHeadHash(int num){
        Random random = new Random();

        ArrayList<Integer> checklist = new ArrayList<>();
        int mapSize = serverStatus.size();

        for(int i = 0; i < num; i++){
            if(i >= mapSize){
                break;//if there are enough servers
            }
            int candidate = random.nextInt(mapSize);
        
            while(checklist.contains(candidate)){
                candidate = random.nextInt(mapSize);
            }

            checklist.add(candidate);

        }
        //make it an array
        ArrayList<ServerInfo> peers = new ArrayList<>(serverStatus.keySet());
        ArrayList<Thread> threads = new ArrayList<>();
        //get the current head hash
        Block head = chain.getHead();
        if(head == null){
            return;
        }
        
        byte[] hash = head.calculateHash();
       
        
        String digest = Base64.getEncoder().encodeToString(hash);
        //construct message
        String message = String.format("lb|%d|%d|%s", localPort, chain.getLength(), digest);
        for(Integer candicate : checklist){
            ServerInfo peer = peers.get(candicate);
            Thread thread = new Thread(new SenderRunnable(peer, message));
            threads.add(thread);
            thread.start();
        }

        //wait for them to join
        for(Thread thread: threads){
            try{
                thread.join();
            }catch(InterruptedException e){
                //System.err.println(e);
            }
        }

    }
}