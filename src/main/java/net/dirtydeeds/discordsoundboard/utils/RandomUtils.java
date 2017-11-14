package net.dirtydeeds.discordsoundboard.utils;

import java.util.Random;

public class RandomUtils {

  private static final int BIGGEST_SMALL_NUMBER = 5;

  public static Object chooseOne(Object... args) {
    Random rng = new Random();
    return args[rng.nextInt(args.length)];
  }

  public static int smallNumber() {
    Random rng = new Random();
    return rng.nextInt(BIGGEST_SMALL_NUMBER) + 1;
  }
  
}