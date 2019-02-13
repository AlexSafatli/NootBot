package net.dirtydeeds.discordsoundboard.games;

import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;

public interface GameUpdateProcessor {

  boolean isApplicableUpdateEvent(UserGameUpdateEvent event, User user);

  void process(UserGameUpdateEvent event);

  boolean isMutuallyExclusive();

}