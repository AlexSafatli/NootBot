package net.dirtydeeds.discordsoundboard.utils;

import java.util.LinkedList;

public class LimitedQueue<T> extends LinkedList<T> {
  private int lim;
  public LimitedQueue(int lim) {
    this.lim = lim;
  }

  @Override
  public boolean add(E o) {
    boolean added = super.add(o);
    while (added && size() > lim) super.remove();
    return added;
  }
}