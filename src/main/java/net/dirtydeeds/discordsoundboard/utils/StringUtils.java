package net.dirtydeeds.discordsoundboard.utils;

import java.text.SimpleDateFormat;
import java.text.BreakIterator;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.awt.Color;

public class StringUtils {

	private static final int MAX_NUMBER_OF_CACHED_WORDS = 250;
	private static final int MIN_NUMBER_OF_CHARS_FOR_WORD_PAIR = 3;
	private static final String STARTING_CACHE_WORD = "Noot";
	private static final List<String> PREPOSITIONS = Arrays.asList(
	      new String[] {
	        "of", "by", "as", "at", "for", "in", "into", "on", "plus", "with"
	      }
	    );
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
			words[i] = (i > 0) ?
			           (String) RandomUtils.chooseOne(capitalize(randomWord()),
			               randomPreposition()) :
			           capitalize(randomWord());
		}
		return String.join(" ", words);
	}

	public static String randomPhrase() {
		return randomPhrase(RandomUtils.smallNumber());
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

	public static String dayTimeStamp(Date date) {
		if (date == null) return "";
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		String f = formatter.format(date);
		return (f != null) ? f : "";
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
			if (!containsDigit(word)) {
				cacheString(word);
			}
		}
	}

	private static LimitedQueue<String> initializeCache() {
		LimitedQueue<String> cache = new LimitedQueue<>(MAX_NUMBER_OF_CACHED_WORDS);
		for (int i = 0; i < MAX_NUMBER_OF_CACHED_WORDS; ++i)
			cache.add(STARTING_CACHE_WORD);
		return cache;
	}

}
