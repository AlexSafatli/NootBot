package net.dirtydeeds.discordsoundboard.chat.users;

import net.dirtydeeds.discordsoundboard.chat.AuthenticatedSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.SimpleLogger;

public class LimitUserProcessor extends
        AuthenticatedSingleArgumentChatCommandProcessor {

  public static final SimpleLogger LOG = SimpleLogger.getLog("LimitUser");

  public LimitUserProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Throttle User", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (getArgument() != null) {
      String username = getArgument();
      if (username.equals(event.getAuthor().getName())) {
        pm(event, "You cannot do this to yourself.");
      } else if (bot.throttleUser(username)) {
        pm(event, String.format("Throttled %s.", username));
        LOG.info("Throttled username " + username);
      } else {
        pm(event, String.format("User %s was not found.", username));
        LOG.warn("Failed to throttle username " + username);
      }
    }
  }

  @Override
  public String getCommandHelpString() {
    return super.getCommandHelpString() +
            " - throttle a user from using bot too often";
  }
}