package net.dirtydeeds.discordsoundboard.utils;

import java.util.Random;

public class RandomUtils {

  public static Object chooseOne(Object... args) {
    Random rng = new Random();
    return args[rng.nextInt(args.length)];
  }
  
}