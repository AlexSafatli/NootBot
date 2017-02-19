package net.dirtydeeds.discordsoundboard.utils;

import java.util.Collection;
import java.util.List;
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

	public static <T> String listToString(List<T> list) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); ++i) {
			T t = list.get(i);
			sb.append("\"" + t.toString() + "\"");
			if (i + 1 < list.size() - 1) sb.append(", ");
			else if (i + 1 == list.size() - 1) sb.append(", and ");
		}
		return sb.toString();
	}
	
}