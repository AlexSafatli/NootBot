package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class PlayUrlProcessor extends SingleArgumentChatCommandProcessor {

  public static final SimpleLog LOG = SimpleLog.getLog("URL");

  public PlayUrlProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Play URL", bot);
  }

  private void play(MessageReceivedEvent event, String url) {
    try {
      bot.playURLForChatCommand(url, event);
      m(event, "Playing `" + url + "`.");
    } catch (Exception e) {
      e(event, "Could not play => " + e.getMessage());
    }
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    User user = event.getAuthor();
    if (!bot.isAllowedToPlaySound(user)) {
      pm(event, "You're not allowed to do that.");
      LOG.info(String.format("%s not allowed to play sounds.", user.getName()));
    } else {
      play(event, message.substring(getPrefix().length()));
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + "url - play a sound by url";
  }
}
