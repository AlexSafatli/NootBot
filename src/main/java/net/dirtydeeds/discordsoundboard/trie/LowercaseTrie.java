package net.dirtydeeds.discordsoundboard.trie;

import java.util.Collection;

public class LowercaseTrie implements Vocabulary {

	private boolean isWord = false;
	private LowercaseTrie[] children = new LowercaseTrie[Alphabet.LOWERCASE.size()];
	private int numChildren = 0;

	public LowercaseTrie() {
	}

	public LowercaseTrie(Collection<String> set) {
		for (String str : set) {
			add(str);
		}
	}

	public boolean add(String s) {
		char first = s.charAt(0);
		int index = Alphabet.LOWERCASE.getIndex(first);
		if (index < 0) {
			return false;
		}
		LowercaseTrie child = children[index];
		if (child == null) {
			child = new LowercaseTrie();
			children[index] = child;
			numChildren++;
		}
		if (s.length() == 1) {
			if (child.isWord) {
				// The word is already in the trie
				return false;
			}
			child.isWord = true;
			return true;
		} else {
			// Recurse into sub-trie
			return child.add(s.substring(1));
		}
	}

	public boolean isPrefix(String s) {
		LowercaseTrie n = getNode(s);
		return n != null && n.numChildren > 0;
	}

	public String getWordWithPrefix(String s) {
		String out = s;
		LowercaseTrie n = getNode(s);
		if (n == null || n.numChildren <= 0) return null;
		while (n.numChildren > 0) {
			LowercaseTrie largest = null;
			char l = ' ';
			for (int i = 0; i < Alphabet.LOWERCASE.size(); i++) {
				char c = Alphabet.LOWERCASE.alphabet.charAt(i);
				LowercaseTrie child = n.children[i];
				if (child != null && child.isWord) return out + c;
				if (child != null && (largest == null || largest.numChildren < child.numChildren)) {
					largest = child;
					l = c;
				}
			}
			if (largest == null) return null;
			n = largest;
			out += l;
		}
		return null;
	}

	public LowercaseTrie getNode(String s) {
		LowercaseTrie node = this;
		for (int i = 0; i < s.length(); i++) {
			int index = Alphabet.LOWERCASE.getIndex(s.charAt(i));
			if (index == -1) return null; // Bad character, not lowercase.
			LowercaseTrie child = node.children[index];
			if (child == null) {
				// There is no such word
				return null;
			}
			node = child;
		}
		return node;
	}

	public boolean contains(String s) {
		LowercaseTrie n = getNode(s);
		return n != null && n.isWord;
	}

	public boolean isWord() {
		return isWord;
	}

	public boolean hasChildren() {
		return numChildren > 0;
	}

}
