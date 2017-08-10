package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.*;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class PlayUrlProcessor extends OwnerSingleArgumentChatCommandProcessor {

  public static final SimpleLog LOG = SimpleLog.getLog("URL");

  public PlayUrlProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Play URL", bot);
  }

  private boolean play(MessageReceivedEvent event, String url) {
    boolean played = true;
    try {
      bot.playURLForChatCommand(url, event);
    } catch (Exception e) {
      played = false;
      LOG.warn("Did not play sound.");
    }
    return played;
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    User user = event.getAuthor();
    if (!bot.isAllowedToPlaySound(user)) {
      pm(event, lookupString(Strings.NOT_ALLOWED));
      LOG.info(String.format("%s isn't allowed to play sounds.",
                             user.getName()));
    } else {
      play(event, message.substring(getPrefix().length(), message.length()));
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + "url - play a sound by url";
  }
}
