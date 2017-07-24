package net.dirtydeeds.discordsoundboard.trie;

public interface Vocabulary {

  public abstract boolean add(String str);
  public abstract boolean isPrefix(String prefix);
  public abstract boolean contains(String str);

}