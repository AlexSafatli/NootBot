package net.dirtydeeds.discordsoundboard.beans;

import net.dv8tion.jda.api.entities.Guild;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Setting {

  @Id
  private final String id;
  private final String key;
  private String value;
  private Long guildId;

  protected Setting() {
    this.id = null;
    this.key = "";
    this.value = "";
    this.guildId = 0L;
  }

  public Setting(String key, String value, Guild guild) {
    this.key = key;
    this.value = value;
    this.guildId = guild.getIdLong();
    this.id = key + "_" + guildId;
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

  public String getKey() {
    return key;
  }

  public Long getGuildId() {
    return guildId;
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