package net.dirtydeeds.discordsoundboard.beans;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Phrase {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String value;

  protected Phrase() {
    this.value = "";
  }

  public Phrase(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Phrase s = (Phrase) o;
    return value.equals(s.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  public String toString() {
    return "`" + value + "`";
  }
}