package net.dirtydeeds.discordsoundboard.chat.users;

import net.dirtydeeds.discordsoundboard.chat.AuthenticatedSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class AllowUserProcessor extends
        AuthenticatedSingleArgumentChatCommandProcessor {

  public AllowUserProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Allow User", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (getArgument() != null) {
      String username = getArgument();
      if (bot.allowUser(username))
        pm(event, "Success.");
      else pm(event, "User not found.");
    }
  }

  @Override
  public String getCommandHelpString() {
    return super.getCommandHelpString() +
            " - allow a disallowed user to play sounds again";
  }
}