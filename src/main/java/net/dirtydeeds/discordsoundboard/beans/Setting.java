package net.dirtydeeds.discordsoundboard.beans;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Setting {

  @Id
  private final String id;
  private String value;

  protected Setting() {
    this.id = null;
    this.value = "";
  }

  public Setting(String id, String value) {
    this.id = id;
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Setting s = (Setting) o;
    return id.equals(s.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  public String toString() {
    return "`" + id + "`";
  }
}