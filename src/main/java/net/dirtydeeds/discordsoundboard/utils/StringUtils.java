package net.dirtydeeds.discordsoundboard.utils;

import java.util.Collection;
import java.util.Random;

public class StringUtils {

	public static String truncate(String str) {
		return truncate(str, 10);
	}
	
	public static String truncate(String str, int len) {
		String truncated = str.substring(0,Math.min(str.length(), len));
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
		for (char s : str.toCharArray()) {
			if (s == c) return true;
		}
		return false;
	}
	
	public static String randomString(Collection<String> strings) {
		Random rng = new Random();
		int i = rng.nextInt(strings.size()), k = 0;
		for (String string : strings) {
			if (k == i) return string;
			++k;
		}
		return null;
	}
	
}