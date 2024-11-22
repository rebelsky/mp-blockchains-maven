package edu.grinnell.csc207.blockchains;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A full blockchain.
 *
 * @author Your Name Here
 */
public class BlockChain implements Iterable<Transaction> {
  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The number of blocks in the chain.
   */
  int size;

  /**
   * The front of the chain.
   */
  Node front;

  /**
   * The back of the chain.
   */
  Node back;

  /**
   * The validator.
   */
  HashValidator validator;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new blockchain using a validator to check elements.
   *
   * @param check
   *   The validator used to check elements.
   */
  public BlockChain(HashValidator check) {
    this.validator = check;
    Block b = 
        new Block(0, 
            new Transaction("", "", 0), 
            new Hash(new byte[] {}), 
            validator);
    this.front = new Node(b);
    this.back = this.front;
    this.size = 1;
  } // BlockChain(HashValidator)

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * An alias for "throw new Exception".
   *
   * @param message
   *   The message to include in the exception.
   *
   * @throws Exception
   */
  void fail(String message) throws Exception {
    throw new Exception(message);
  } // fail(String)

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+

  /**
   * Mine for a new valid block for the end of the chain, returning that
   * block.
   *
   * @param t
   *   The transaction that goes in the block.
   *
   * @return a new block with correct number, hashes, and such.
   */
  public Block mine(Transaction t) {
    return new Block(this.size, t, this.back.block.getHash(), validator);
  } // mine(Transaction)

  /**
   * Get the number of blocks curently in the chain.
   *
   * @return the number of blocks in the chain, including the initial block.
   */
  public int getSize() {
    return this.size;
  } // getSize()

  /**
   * Add a block to the end of the chain.
   *
   * @param block
   *   The block to add to the end of the chain.
   *
   * @throws IllegalArgumentException if (a) the hash is not valid, (b)
   *   the hash is not appropriate for the contents, or (c) the previous
   *   hash is incorrect.
   */
  public void append(Block block) {
    Hash hash = block.getHash();
    if (!validator.isValid(hash)) {
      throw new IllegalArgumentException("Invalid hash in appended block: " 
          + hash);
    }  // if
    Block alt = new Block(block.getNum(), block.getTransaction(),
        block.getPrevHash(), block.getNonce());
    if (!alt.getHash().equals(hash)) {
      throw new IllegalArgumentException("Incorrect hash in appended block: " 
          + hash + " (expected " + alt.getHash() + ")");
    } // if
    if (!this.back.block.getHash().equals(block.getPrevHash())) {
      throw new IllegalArgumentException("Invalid prevHash in appended block "
          + block.getPrevHash());
    } // if
    this.back.next = new Node(block);
    this.back = this.back.next;
    this.size++;
  } // append()

  /**
   * Attempt to remove the last block from the chain.
   *
   * @return false if the chain has only one block (in which case it's
   *   not removed) or true otherwise (in which case the last block
   *   is removed).
   */
  public boolean removeLast() {
    if (this.front == this.back) {
      return false;
    } else {
      Node current = this.front;
      while (current.next != this.back) {
        current = current.next;
      } // while
      this.back = current;
      this.back.next = null;
      this.size--;
      return true;
    } // if/else
  } // removeLast()

  /**
   * Get the hash of the last block in the chain.
   *
   * @return the hash of the last sblock in the chain.
   */
  public Hash getHash() {
    return this.back.block.getHash();
  } // getHash()

  /**
   * Determine if the blockchain is correct in that (a) the balances are
   * legal/correct at every step, (b) that every block has a correct
   * previous hash field, (c) that every block has a hash that is correct
   * for its contents, and (d) that every block has a valid hash.
   *
   * @return true if the blockchain is correct and false otherwise.
   */
  public boolean isCorrect() {
    try { 
      this.check();
      return true;
    } catch (Exception e) {
      return false;
    } // try/catch
  } // isCorrect()

  /**
   * Determine if the blockchain is correct in that (a) the balances are
   * legal/correct at every step, (b) that every block has a correct
   * previous hash field, (c) that every block has a hash that is correct
   * for its contents, and (d) that every block has a valid hash.
   *
   * @throws Exception
   *   If things are wrong at any block.
   */
  public void check() throws Exception {
    Iterator<Block> blocks = this.blocks();

    // Grab the first block and check it out.
    Block prev = blocks.next();      // Get the first block.
    Transaction t = prev.getTransaction();
    if (!"".equals(t.getSource())) {
      fail(String.format("Initial block has invalid source: \"%s\"",
          t.getSource()));
    } // if
    if (!"".equals(t.getTarget())) {
      fail(String.format("Initial block has invalid target: \"%s\"",
          t.getTarget()));
    } // if
    if (0 != t.getAmount()) {
      fail(String.format("Initial block has invalid amount: %d", 
          t.getAmount()));
    } // if

    HashMap<String,Integer> balances = new HashMap<String,Integer>(); 
    while (blocks.hasNext()) {
      // Gather basic information.
      Block block = blocks.next();
      Hash hash = block.getHash();
      int num = block.getNum();

      // Make sure the hash is valid.
      if (!validator.isValid(hash)) {
        fail(String.format("Invalid hash in block %d: $s" ,
            num, hash.toString()));
      }  // if

      // Make sure the hash is correct.
      Block alt = new Block(block.getNum(), block.getTransaction(),
          block.getPrevHash(), block.getNonce());
      if (!alt.getHash().equals(hash)) {
        fail(String.format("Incorrect hash in block %d: %s (expected %s)",
            num, hash.toString(), alt.getHash().toString()));
      } // if
    
      // Make sure the previous hash is correct.
      if (!prev.getHash().equals(block.getPrevHash())) {
        fail(String.format("Invalid prevHash in block %d: %s (expected %s)",
            num, block.getPrevHash(), prev.getHash()));
      } // if

      // Prepare to check the transaction.
      t = block.getTransaction();

      // Check for valid source.
      String source = t.getSource();
      if (!"".equals(source) && !balances.containsKey(source)) {
        fail(String.format("Unknown source in block %d: \"%s\"", num, source));
      } // if

      // Check for valid target.
      String target = t.getTarget();
      if ("".equals(target)) {
        fail(String.format("Invalid target in block %d: empty string", num));
      } // if

      // Check for valid amount.
      int amount = t.getAmount();
      if (amount < 0) {
        fail(String.format("Negative amount in block %d: %d", num, amount));
      } // if (amount < 0)
      if ((!"".equals(source)) && (amount > balances.get(source))) {
        fail(String.format("Insufficient balance for %s in block %d:"
            + " Has %d, needs %d",
            source, num, balances.get(source), amount));
      } // if

      // Update the balances
      if (!"".equals(source)) {
        balances.put(source, balances.get(source) - amount);
      } // if
      if (!balances.containsKey(target)) {
        balances.put(target, amount);
      } else {
        balances.put(target, balances.get(target) + amount);
      } // if/else

      // Update the previous block
      prev = block;
    } // while
  } // check()

  /**
   * Return an iterator of all the people who participated in the
   * system.
   *
   * @return an iterator of all the people in the system.
   */
  public Iterator<String> users() {
    return new Iterator<String>() {
      HashSet<String> returned = new HashSet<String>();
      Node current = BlockChain.this.front;
      {
        current = current.next;
      } // current
      public boolean hasNext() {
        return current != null;
      } // hasNext()

      public String next() {
        String user = current.block.getTransaction().getTarget();
        returned.add(user);
        String nextUser = "";
        do {
          current = current.next;
          if (current != null) {
            nextUser = current.block.getTransaction().getTarget();
          } // if
        } while ((current != null) && (returned.contains(nextUser)));
        return user;
      } // next()
    };
  } // users()

  /**
   * Find one user's balance.
   *
   * @param user
   *   The user whose balance we want to find.
   *
   * @return that user's balance (or 0, if the user is not in the system).
   */
  public int balance(String user) {
    int balance = 0;
    for (Transaction t : this) {
      if (user.equals(t.getSource())) {
        balance -= t.getAmount();
      } else if (user.equals(t.getTarget())) {
        balance += t.getAmount();
      } // if/else
    } // for
    return balance;
  } // balance(String)

  /**
   * Get an interator for all the blocks in the chain.
   *
   * @return an iterator for all the blocks in the chain.
   */
  public Iterator<Block> blocks() {
    return new Iterator<Block>() {
      Node current = BlockChain.this.front;

      public boolean hasNext() {
        return this.current != null;
      } // hasNext()

      public Block next() {
        Block block = this.current.block;
        current = this.current.next;
        return block;
      } // next()
    };
  } // blocks()

  /**
   * Get an interator for all the transactions in the chain.
   *
   * @return an iterator for all the blocks in the chain.
   */
  public Iterator<Transaction> iterator() {
    return new Iterator<Transaction>() {
      Iterator<Block> blocks = BlockChain.this.blocks();
      {
        blocks.next();
      } // init
      public boolean hasNext() {
        return this.blocks.hasNext();
      } // hasNext()

      public Transaction next() {
        return this.blocks.next().getTransaction();
      } // next()
    };
  } // iterator()

} // class BlockChain

/**
 * Nodes in the BlockChain.
 */
class Node {
  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The block.
   */
  Block block;

  /**
   * The next node.
   */
  Node next;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+
  
  /**
   * Create a node given both block and next.
   *
   * @param b
   *   The block.
   * @param n
   *   The next node.
   */
  Node(Block b, Node n) {
    this.block = b;
    this.next = n;
  } // Node(Block, Node)

  /**
   * Create a node given just the block.
   *
   * @param b
   *   The block.
   */
  Node(Block b) {
    this(b, null);
  } // Node(Block)
} // class Node
