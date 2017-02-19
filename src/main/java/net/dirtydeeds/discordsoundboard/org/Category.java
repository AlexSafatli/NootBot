package net.dirtydeeds.discordsoundboard.org;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

public class Category {

	private Collection<Category> children;
	private String name;
	private Path folderPath;
	
	public Category(String name, Path path) {
		this.name = name;
		this.folderPath = path;
		this.children = new LinkedList<>();
	}

	public Collection<Category> getChildren() {
		return children;
	}

	public void setChildren(Collection<Category> children) {
		this.children = children;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Path getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(Path folderPath) {
		this.folderPath = folderPath;
	}
	
}