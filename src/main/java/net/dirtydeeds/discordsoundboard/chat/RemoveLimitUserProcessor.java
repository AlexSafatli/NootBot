package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class RemoveLimitUserProcessor extends
        AuthenticatedSingleArgumentChatCommandProcessor {

  public static final SimpleLog LOG = SimpleLog.getLog("RemoveLimitUser");

  public RemoveLimitUserProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Unthrottle User", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (getArgument() != null) {
      String username = getArgument();
      if (bot.unthrottleUser(username)) {
        pm(event, formatString(Strings.USER_UNTHROTTLE_THROTTLED, username));
      } else {
        pm(event, formatString(Strings.USER_NOT_FOUND_THROTTLED, username));
        LOG.info("No throttled user to unthrottle with username " + username);
      }
    }
  }

  @Override
  public String getCommandHelpString() {
    return super.getCommandHelpString() + " - stop throttling a user";
  }
}