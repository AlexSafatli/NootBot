package net.dirtydeeds.discordsoundboard.utils;

import java.awt.*;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class StringUtils {

  private static final int MAX_NUMBER_OF_CACHED_WORDS = 32;
  private static final int MIN_WORD_SIZE = 3;
  private static final String STARTING_CACHE_WORD = "Noot";
  private static final List<String> PREPOSITIONS = Arrays.asList(
          "of", "by", "as", "at", "for", "in", "into", "on", "with", "in the",
          "of the", "for the", "at the", "with the", "on the", "the", "under",
          "between", "after", "before", "without");
  public static List<String> wordCache = initializeCache();

  public static String truncate(String str) {
    return truncate(str, 10);
  }

  public static String truncate(String str, int len) {
    String truncated = str.substring(0, Math.min(str.length(), len));
    if (str.length() > len) truncated += "...";
    return truncated;
  }

  public static boolean containsOnly(String str, char c) {
    for (char s : str.toCharArray()) {
      if (s != c) return false;
    }
    return true;
  }

  public static boolean containsAny(String str, char c) {
    if (str == null) return false;
    for (char s : str.toCharArray()) {
      if (s == c) return true;
    }
    return false;
  }

  public static String randomString(Collection<String> strings) {
    if (strings.isEmpty()) return "";
    Random rng = new Random();
    int i = rng.nextInt(strings.size()), k = 0;
    for (String string : strings) {
      if (k == i) return string;
      ++k;
    }
    return null;
  }

  public static String randomWord() {
    return randomString(wordCache);
  }

  public static String randomPreposition() {
    return randomString(PREPOSITIONS);
  }

  public static String randomPhrase(int numWords) {
    String[] words = new String[numWords];
    for (int i = 0; i < numWords; ++i) {
      words[i] = (i % 2 == 1 && i != numWords - 1) ?
              (String) RandomUtils.chooseOne(
                      capitalizeIfNotPreposition(randomWord()),
                      randomPreposition()) :
              capitalizeIfNotPreposition(randomWord());
    }
    return String.join(" ", words);
  }

  public static String randomPhrase() {
    return randomPhrase(RandomUtils.smallNumber() + 1);
  }

  public static <T> String listToString(List<T> list) {
    if (list.size() == 1) {
      return list.get(0).toString();
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.size(); ++i) {
      sb.append(list.get(i).toString());
      if (i + 1 < list.size() - 1) sb.append(", ");
      else if (i + 1 == list.size() - 1) sb.append(", and ");
    }
    return sb.toString();
  }

  public static String humanize(String s) {
    if (s.length() == 1) return s.toUpperCase();
    return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
  }

  public static String capitalize(String s) {
    if (s.length() == 1) return s.toUpperCase();
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  public static String capitalizeIfNotPreposition(String s) {
    if (PREPOSITIONS.contains(s.toLowerCase())) return s;
    return capitalize(s);
  }

  public static String dayTimeStamp(Date date) {
    if (date == null) return "";
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    return formatter.format(date);
  }

  public static Color toColor(String s) {
    int hash = s.hashCode(),
            r = (hash & 0x0000FF),
            g = (hash & 0x00FF00) >> 8,
            b = (hash & 0xFF0000) >> 16;
    return new Color(r, g, b);
  }

  public static void cacheString(String s) {
    if (!wordCache.contains(s)) wordCache.add(s);
  }

  public static boolean containsDigit(String s) {
    for (int i = 0; i < s.length(); ++i) {
      if (Character.isDigit(s.charAt(i))) return true;
    }
    return false;
  }

  public static void cacheWords(String message) {
    BreakIterator iter = BreakIterator.getWordInstance();
    iter.setText(message);
    int last = iter.first();
    while (BreakIterator.DONE != last) {
      int first = last;
      last = iter.next();
      if (last == BreakIterator.DONE) continue;
      String word = message.substring(first, last);
      if (!containsDigit(word) && word.length() >= MIN_WORD_SIZE) {
        cacheString(word);
      }
    }
  }

  private static LimitedQueue<String> initializeCache() {
    LimitedQueue<String> cache = new LimitedQueue<>(MAX_NUMBER_OF_CACHED_WORDS);
    cache.add(STARTING_CACHE_WORD);
    return cache;
  }

}