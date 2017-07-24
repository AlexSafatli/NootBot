package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

public abstract class OwnerSingleArgumentChatCommandProcessor extends
  AuthenticatedSingleArgumentChatCommandProcessor {

  public OwnerSingleArgumentChatCommandProcessor(String prefix, String title, SoundboardBot bot) {
    super(prefix, title, bot);
  }

  @Override
  public boolean canBeRunBy(User user, Guild guild) {
    return bot.getOwner().equals(user.getName());
  }

}
