package net.dirtydeeds.discordsoundboard.games;

import net.dv8tion.jda.api.entities.User;

public class GameContext {

  private User user;
  private String gameName;
  private boolean ingame = false;

  public GameContext(User user, String gameName) {
    this.user = user;
    this.gameName = gameName;
  }

  public GameContext(User user, String gameName, boolean ingame) {
    this(user, gameName);
    this.ingame = ingame;
  }

  public User getUser() {
    return this.user;
  }

  public String getName() {
    return this.gameName;
  }

  public boolean isInGame() {
    return this.ingame;
  }

}
