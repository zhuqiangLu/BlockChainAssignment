import java.util.ArrayList;

public class Blockchain {
    private Block head;
    private ArrayList<Transaction> pool;
    private int length;

    private final int poolLimit = 3;

    public Blockchain() {
        pool = new ArrayList<>();
        length = 0;
    }

    // getters and setters
    public Block getHead() { return head; }
    public ArrayList<Transaction> getPool() { return pool; }
    public int getLength() { return length; }
    public void setHead(Block head) { this.head = head; }
    public void setPool(ArrayList<Transaction> pool) { this.pool = pool; }
    public void setLength(int length) { this.length = length; }

    // add a transaction
    public synchronized boolean addTransaction(String txString) {
        //check txString
        if(!checkTxString(txString)){ return false; }

        Transaction t = new Transaction();
        //partition txString to sender and content and add transaction to the pool
        t.setSender(getSender(txString));
        t.setContent(getContent(txString));
        pool.add(t);

        return true;

    }

    public synchronized String toString() {
        String cutOffRule = new String(new char[81]).replace("\0", "-") + "\n";
        String poolString = "";
        for (Transaction tx : pool) {
            poolString += tx.toString();
        }

        String blockString = "";
        Block bl = head;
        while (bl != null) {
            blockString += bl.toString();
            bl = bl.getPreviousBlock();
        }

        return "Pool:\n"
                + cutOffRule
                + poolString
                + cutOffRule
                + blockString;
    }

    //helpers


    public synchronized void commit(int nonce){
        //not special rule applied for the moment, nonce is not used

       //first check if there are enough transcation in the pool
       if(pool.size() < poolLimit){
           return;
       }


       Block block = new Block();
      //see if this is the first commit
      if(length == 0){
        byte[] g = new byte[32];
        block.setPreviousHash(g);
        block.setPreviousBlock(null);
    }
      else{
        block.setPreviousHash(head.calculateHash());
        block.setPreviousBlock(head);
      }
      block.setTransactions(pool);
      this.setHead(block);
      this.setPool(new ArrayList<>());
      length++;
      System.out.println("committed");
    }
    private boolean checkTxString(String txString){
      //partition the string into 3 parts
      String pattern = "tx\\|[a-z]{4}[0-9]{4}\\|(\\w|\\s){0,70}";
      return(txString.matches(pattern));

    }

    private String getSender(String txString){
      return txString.substring(3,11);

    }
    private String getContent(String txString){
      return txString.substring(12, txString.length());
    }
    // implement helper functions here if you need any.
}
