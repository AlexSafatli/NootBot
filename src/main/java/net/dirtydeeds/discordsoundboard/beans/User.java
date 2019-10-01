package net.dirtydeeds.discordsoundboard.beans;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String userid;
  private String username;
  private String entrancefilename;
  private Boolean disallowed;
  private Boolean throttled;

  /* Privilege Levels
   * 0 - No privileges
   * 1 - Can upload sounds and perform all typical authenticated command even if not a moderator of a server
   * 2 - Can upload sounds without restriction of length, duration, or size
   */
  private Integer privilegeLevel;

  protected User() { }

  public User(String userid, String username) {
    this.userid = userid;
    this.username = username;
    this.entrancefilename = null;
    this.disallowed = false;
    this.throttled = false;
    this.privilegeLevel = 0;
  }

  public User(String userid, String username, String entrancefilename) {
    this.userid = userid;
    this.username = username;
    this.entrancefilename = entrancefilename;
    this.disallowed = false;
    this.throttled = false;
    this.privilegeLevel = 0;
  }

  public User(String userid, String username, String entrancefilename, boolean disallowed, boolean throttled) {
    this.userid = userid;
    this.username = username;
    this.entrancefilename = entrancefilename;
    this.disallowed = disallowed;
    this.throttled = throttled;
    this.privilegeLevel = 0;
  }

  public User(String userid, String username, String entrancefilename, boolean disallowed, boolean throttled, int privilegeLevel) {
    this.userid = userid;
    this.username = username;
    this.entrancefilename = entrancefilename;
    this.disallowed = disallowed;
    this.throttled = throttled;
    this.privilegeLevel = privilegeLevel;
  }

  public String getId() {
    return userid;
  }

  public void setId(String id) {
    this.userid = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEntrance() {
    return entrancefilename;
  }

  public void setEntrance(String soundFile) {
    this.entrancefilename = soundFile;
  }

  public boolean isDisallowed() {
    return disallowed;
  }

  public void setDisallowed(boolean disallowed) {
    this.disallowed = disallowed;
  }

  public boolean isThrottled() {
    if (this.throttled == null) {
      this.throttled = false;
    }
    return throttled;
  }

  public void setThrottled(boolean throttled) {
    this.throttled = throttled;
  }

  public void setPrivilegeLevel(Integer level) {
    if (level >= 0) this.privilegeLevel = level;
    else this.privilegeLevel = 0;
  }

  public Integer getPrivilegeLevel() {
    return (this.privilegeLevel != null) ? this.privilegeLevel : 0;
  }

  public boolean isPrivileged() {
    return this.privilegeLevel != null && this.privilegeLevel > 0;
  }

  public String toString() {
    return username;
  }

}
