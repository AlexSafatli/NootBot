package net.dirtydeeds.discordsoundboard.trie;

public class Alphabet {

  public final String alphabet;

  public Alphabet(String alphabet) {
    this.alphabet = alphabet;
  }

  public final static Alphabet LOWERCASE = new Alphabet("abcdefghijklmonpqrstuvwxyz");

  public byte[] toInt(String str) {
    byte[] r = new byte[str.length()];
    for (int i = 0; i < r.length; i++) {
      r[i] = getIndex(str.charAt(i));
    }
    return r;
  }

  public byte getIndex(char c) {
    return (byte) alphabet.indexOf(c);
  }

  public int size() {
    return alphabet.length();
  }

}
