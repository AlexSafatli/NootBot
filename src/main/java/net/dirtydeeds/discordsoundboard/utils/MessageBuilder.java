package net.dirtydeeds.discordsoundboard.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MessageBuilder implements Iterable<String> {

	private List<String> messages;
	private StringBuilder builder;
	private boolean codified;
	private int max = MAX_MESSAGE_LENGTH;
	private static final long MAX_MESSAGE_LENGTH = 2000;
	
	public MessageBuilder() {
		messages = new LinkedList<>();
		builder = new StringBuilder();
	}

	public MessageBuilder(boolean codified) {
		this();
		this.codified = codified;
	}

	public MessageBuilder(int max) {
		this.max = Math.max(max, this.max);
	}
	
	public MessageBuilder append(Object o) {
		String s = o.toString();
		if (builder.length() + s.length() > ((codified) ? max - 6 : max)) {
			messages.add(builder.toString());
			builder = new StringBuilder();
		}
		builder.append(s);
		return this;
	}

	public List<String> getStrings() {
		if (builder.length() > 0) messages.add(builder.toString());
		if (codified) {
			List<String> coded = new LinkedList<>();
			for (String s : messages) {
				coded.add("```" + s + "```");
			}
			return coded;
		}
		builder = new StringBuilder();
		return messages;
	}
	
	public Iterator<String> iterator() {
		return getStrings().iterator();
	}
	
	public void clear() {
		messages = new LinkedList<>();
		builder = new StringBuilder();
	}
	
}
