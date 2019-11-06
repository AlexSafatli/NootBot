package net.dirtydeeds.discordsoundboard.chat.users;

import net.dirtydeeds.discordsoundboard.chat.AuthenticatedSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.JDALogger;

public class RemoveLimitUserProcessor extends
        AuthenticatedSingleArgumentChatCommandProcessor {

  public RemoveLimitUserProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Unthrottle User", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (getArgument() != null) {
      String username = getArgument();
      if (bot.unthrottleUser(username)) {
        pm(event, "Success.");
      } else {
        pm(event, "User not found.");
      }
    }
  }

  @Override
  public String getCommandHelpString() {
    return super.getCommandHelpString() + " - stop throttling a user";
  }
}