package edu.grinnell.csc207.blockchains;

import java.nio.ByteBuffer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Random;

/**
 * Blocks to be stored in blockchains.
 *
 * @author Samuel A. Rebelsky
 */
public class Block {
  // +-----------+---------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * Are we observing what's happening?
   */
  static final boolean VERBOSE = true;

  // +---------------+-----------------------------------------------
  // | Static fields |
  // +---------------+

  /**
   * The message digest used to compute hashes.
   */
  static MessageDigest md = null;

  /**
   * The byte buffer used for ints.
   */
  static ByteBuffer intBuffer = ByteBuffer.allocate(Integer.BYTES);

  /**
   * The byte buffer used for longs.
   */
  static ByteBuffer longBuffer = ByteBuffer.allocate(Long.BYTES);

  /**
   * A random number generator for finding nonces.
   */
  static Random rand = new Random();

  // +----------------+----------------------------------------------
  // | Static methods |
  // +----------------+

  /**
   * Convert an integer into its bytes.
   *
   * @param i
   *   The integer to convert.
   *
   * @return
   *   The bytes of that integer.
   */
  static byte[] intToBytes(int i) {
    intBuffer.clear();
    return intBuffer.putInt(i).array();
  } // intToBytes(int)

  /**
   * Convert a long into its bytes.
   *
   * @param l
   *   The long to convert.
   *
   * @return
   *   The bytes in that long.
   */
  static byte[] longToBytes(long l) {
    longBuffer.clear();
    return longBuffer.putLong(l).array();
  } // longToBytes()

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The number of the block.
   */
  int num;

  /**
   * The transaction.
   */
  Transaction transaction;

  /**
   * The previous hash.
   */
  Hash prevHash;

  /**
   * The nonce.
   */
  long nonce;

  /**
   * The hash.
   */
  Hash hash;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new block from the specified block number, transaction, and
   * previous hash, mining to choose a nonce that meets the requirements
   * of the validator.
   *
   * @param number
   *   The number of the block.
   * @param trans
   *   The transaction for the block.
   * @param ph 
   *   The hash of the previous block.
   * @param check
   *   The validator used to check the block.
   */
  public Block(int number, Transaction trans, Hash ph, HashValidator check) {
    this.num = number;
    this.transaction = trans;
    this.prevHash = ph;
    this.mine(check);
  } // Block(int, Transaction, Hash, HashValidator)

  /**
   * Create a new block, computing the hash for the block.
   *
   * @param number
   *   The number of the block.
   * @param trans
   *   The transaction for the block.
   * @param ph
   *   The hash of the previous block.
   * @param theNonce
   *   The nonce of the block.
   */
  public Block(int number, Transaction trans, Hash ph, long theNonce) {
    this.num = number;
    this.transaction = trans;
    this.prevHash = ph;
    this.nonce = theNonce;
    this.computeHash();
  } // Block(int, Transaction, Hash, long)

  // +---------+-----------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Compute the hash of the block given all the other info already
   * stored in the block.
   */
  void computeHash() {
    // Make sure that we have a message digest.
    if (null == md) {
      try {
        md = MessageDigest.getInstance("sha-256");
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("Cannot load hash algorithm");
      } // try/catch
    } // if

    md.update(intToBytes(this.num));
    md.update(this.transaction.getSource().getBytes());
    md.update(this.transaction.getTarget().getBytes());
    md.update(intToBytes(this.transaction.getAmount()));
    md.update(this.prevHash.getBytes());
    md.update(longToBytes(this.nonce));
    this.hash = new Hash(md.digest());
  } // computeHash()

  /**
   * Mine for a matching hash.
   */
  void mine(HashValidator check) {
    long count = 0;
    long startTime = System.currentTimeMillis();

    do {
      // The real work
      this.nonce = rand.nextLong();
      this.computeHash();
      // Observations
      ++count;
      if (VERBOSE && (0 == (count % 100000))) {
        System.err.printf("Generated %d nonces in %d milliseconds.\n",
            count, System.currentTimeMillis() - startTime);
      } // if
    } while (!check.isValid(this.hash));
  } // mine(HashValidator)

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+

  /**
   * Get the number of the block.
   *
   * @return the number of the block.
   */
  public int getNum() {
    return this.num;
  } // getNum()

  /**
   * Get the transaction stored in this block.
   *
   * @return the transaction.
   */
  public Transaction getTransaction() {
    return this.transaction;
  } // getTransaction()

  /**
   * Get the nonce of this block.
   *
   * @return the nonce.
   */
  public long getNonce() {
    return this.nonce;
  } // getNonce()

  /**
   * Get the hash of the previous block.
   *
   * @return the hash of the previous block.
   */
  Hash getPrevHash() {
    return this.prevHash;
  } // getPrevHash

  /**
   * Get the hash of the current block.
   *
   * @return the hash of the current block.
   */
  Hash getHash() {
    return this.hash;
  } // getHash

  /**
   * Get a string representation of the block.
   *
   * @return a string representation of the block.
   */
  public String toString() {
    return 
        String.format(
            "Block %d (Transaction: %s, Nonce: %d, prevHash: %s, hash: %s)",
            this.num, this.transaction.toString(), this.nonce,
            this.prevHash.toString(), this.hash.toString());
  } // toString()
} // class Block
