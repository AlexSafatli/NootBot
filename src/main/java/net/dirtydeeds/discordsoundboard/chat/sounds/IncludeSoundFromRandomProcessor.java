package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.AuthenticatedSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Set;

public class IncludeSoundFromRandomProcessor extends
        AuthenticatedSingleArgumentChatCommandProcessor {

  public IncludeSoundFromRandomProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Include Sound For Being Randomed", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String name = getArgument();
    Set<String> soundNames = bot.getSoundMap().keySet();
    if (name == null) {
      pm(event, String.format("You need to provide a name. For example: `%s`.", getPrefix() + " " +
                             StringUtils.randomString(soundNames)));
    } else if (!soundNames.contains(name)) {
      String suggestion = "Check your spelling.",
             possibleName = bot.getClosestMatchingSoundName(name);
      if (possibleName != null) {
        suggestion = "Did you mean `" + possibleName + "`?";
      }
      pm(event, "Not found." + " *" + suggestion + "* ");
    } else {
      SoundFile file = bot.getDispatcher().getSoundFileByName(name);
      if (!file.isExcludedFromRandom()) {
        pm(event, "That sound was *not excluded*!");
      } else {
        file.setExcludedFromRandom(false);
        pm(event, "Sound `" + name + "` is no longer been excluded.");
      }
      bot.getDispatcher().saveSound(file);
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() +
           " <soundfile> (*) - include a sound file again for random events";
  }
}