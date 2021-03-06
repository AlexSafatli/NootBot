package net.dirtydeeds.discordsoundboard.games;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;

public class SpecificGameStartProcessor extends GenericGameStartProcessor {

  private final String gameName;

  public SpecificGameStartProcessor(SoundboardBot bot, String game) {
    super(bot);
    gameName = game;
  }

  public SpecificGameStartProcessor(SoundboardBot bot, String game, String thumb) {
    super(bot, thumb);
    gameName = game;
  }

  public boolean isApplicableUpdateEvent(UserActivityStartEvent event, User user) {
    Activity currentGame = event.getNewActivity();
    if (currentGame == null) return false;
    return super.isApplicableUpdateEvent(event, user) && currentGame.getName().equals(gameName);
  }

  @Override
  public String toString() {
    return super.toString() + "[" + gameName + "]";
  }

}
