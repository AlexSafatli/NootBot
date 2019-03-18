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

public class DescribeSoundProcessor extends
        SingleArgumentChatCommandProcessor {

  private static final String DEFAULT_SUGGESTION = "Check your spelling";

  public DescribeSoundProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Sound Info", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    User user = event.getAuthor();
    String name = getArgument();
    Set<String> soundNames = bot.getSoundMap().keySet();
    if (name == null) {
      e(event, formatString(Strings.NEED_NAME, getPrefix() +
              " " + StringUtils.randomString(soundNames)));
    } else if (!soundNames.contains(name)) {
      String possibleName = bot.getClosestMatchingSoundName(name),
              suggestion = (possibleName != null) ?
                      "Did you mean `" + possibleName + "`?" :
                      DEFAULT_SUGGESTION;
      w(event, formatString(Strings.NOT_FOUND, name) +
              " *" + suggestion + "* " + user.getAsMention());
    } else {
      SoundFile file = bot.getDispatcher().getSoundFileByName(name);
      StyledEmbedMessage em = StyledEmbedMessage.forSoundFile(
              bot, file, getTitle(),
              "You requested information for a sound " +
                      user.getAsMention());
      embedForUser(event, em);
      if (bot.getOwner().equals(user.getName())) {
        pm(event, "Sound `" + name + "` is" +
                (file.isExcludedFromRandom() ?
                        "" : " not") + " excluded from random.");
      }
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <soundfile> - get information for a sound file";
  }
}