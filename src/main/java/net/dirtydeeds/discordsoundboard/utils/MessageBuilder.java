package net.dirtydeeds.discordsoundboard.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MessageBuilder implements Iterable<String> {

	private List<String> messages;
	private StringBuilder builder;
	private static final long MAX_MESSAGE_LENGTH = 2000;
	
	public MessageBuilder() {
		messages = new LinkedList<>();
		builder = new StringBuilder();
	}
	
	public MessageBuilder append(Object o) {
		String s = o.toString();
		if (builder.length() + s.length() > MAX_MESSAGE_LENGTH) {
			messages.add(builder.toString());
			builder = new StringBuilder();
		}
		builder.append(s);
		return this;
	}

	public List<String> getStrings() {
		if (builder.length() > 0) messages.add(builder.toString());
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