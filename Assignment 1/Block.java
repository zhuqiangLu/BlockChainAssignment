import java.util.ArrayList;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Block {
    private Block previousBlock;
    private byte[] previousHash;
    private ArrayList<Transaction> transactions;

    public Block() { transactions = new ArrayList<>(); }

    // getters and setters
    public Block getPreviousBlock() { return previousBlock; }
    public byte[] getPreviousHash() { return previousHash; }
    public ArrayList<Transaction> getTransactions() { return transactions; }
    public void setPreviousBlock(Block previousBlock) { this.previousBlock = previousBlock; }
    public void setPreviousHash(byte[] previousHash) { this.previousHash = previousHash; }
    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public String toString() {
        String cutOffRule = new String(new char[81]).replace("\0", "-") + "\n";
        String prevHashString = String.format("|PreviousHash:|%65s|\n",
                Base64.getEncoder().encodeToString(previousHash));
        String hashString = String.format("|CurrentHash:|%66s|\n",
                Base64.getEncoder().encodeToString(calculateHash()));
        String transactionsString = "";
        for (Transaction tx : transactions) {
            transactionsString += tx.toString();
        }
        return "Block:\n"
                + cutOffRule
                + hashString
                + cutOffRule
                + transactionsString
                + cutOffRule
                + prevHashString
                + cutOffRule;
    }

    // to calculate the hash of current block.
    public byte[] calculateHash() {
        // TODO: implement your code here.

        byte[] hash = new byte[32];

        try{
          //set up
          MessageDigest digest = MessageDigest.getInstance("SHA-256");
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          DataOutputStream dos = new DataOutputStream(baos);
          //write previous hash to DataOutputStream
          dos.write(previousHash);
          //write transaction message to DataOutputStream
          
          for(Transaction t:transactions){
            String sender = t.getSender();
            String content = t.getContent();

            dos.writeUTF("tx|"+sender +"|"+content);
          }

          byte[] bytes = baos.toByteArray();
          //digest them
          hash = digest.digest(bytes);

    }catch(NoSuchAlgorithmException e){
      return null;
    }catch(IOException e){
      return null;
    }
    return hash;
  }
    // implement helper functions here if you need any.
}
