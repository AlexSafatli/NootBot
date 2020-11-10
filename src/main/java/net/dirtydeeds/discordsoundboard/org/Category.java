package net.dirtydeeds.discordsoundboard.org;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  public List<String> getFiles() {
    try (Stream<Path> walk = Files.walk(folderPath)) {
      return walk.filter(Files::isRegularFile).map(
              Path::toString).collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new LinkedList<>();
  }

  public String toString() {
    return getName();
  }

}