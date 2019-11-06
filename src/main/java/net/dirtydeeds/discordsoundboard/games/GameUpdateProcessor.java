package net.dirtydeeds.discordsoundboard.games;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;

public interface GameUpdateProcessor {

  boolean isApplicableUpdateEvent(UserActivityStartEvent event, User user);

  void process(UserActivityStartEvent event);

  boolean isMutuallyExclusive();

}