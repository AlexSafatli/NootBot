package net.dirtydeeds.discordsoundboard.chat.sounds;

import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class LastPlayedSoundProcessor extends
        SingleArgumentChatCommandProcessor {

  public LastPlayedSoundProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Last Played Sound", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    User user = event.getAuthor();
    SoundFile file = bot.getLastPlayed();
    String player = bot.getLastPlayedUsername();
    if (file == null) {
      w(event, "No last played sound found " + user.getAsMention());
    } else {
      StyledEmbedMessage em =
        StyledEmbedMessage.forSoundFile(
          bot, file, getTitle(), "This was the last played sound \u2014 " +
          user.getAsMention());
      em.addContent("Sound Was Played By", (player != null) ? player : "\u2014",
                    false);
      embedForUser(event, em);
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - get information for the last played sound";
  }
}