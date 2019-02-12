package net.dirtydeeds.discordsoundboard.chat;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

public interface ChatCommandProcessor {

  void process(MessageReceivedEvent event);
  boolean isApplicableCommand(MessageReceivedEvent event);
  boolean canBeRunByAnyone();
  boolean canBeRunBy(User user, Guild guild);
  String getTitle();
  String getCommandHelpString();

}