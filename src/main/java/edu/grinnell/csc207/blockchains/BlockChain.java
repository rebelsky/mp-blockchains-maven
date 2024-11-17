package edu.grinnell.csc207.blockchains;

import java.util.HashMap;
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
      throw new IllegalArgumentException("Invalid hash in appened block: " 
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
    Iterator<Block> blocks = this.blocks();
    blocks.next();      // Skip the first
    HashMap<String,Integer> balances = new HashMap<String,Integer>(); 
  } // isCorrect()

  /**
   * Return an iterator of all the people who participated in the
   * system.
   *
   * @return an iterator of all the people in the system.
   */
  public Iterator<String> users() {
    return new Iterator<String>() {
      public boolean hasNext() {
        return false;   // STUB
      } // hasNext()

      public String next() {
        throw new NoSuchElementException();     // STUB
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
