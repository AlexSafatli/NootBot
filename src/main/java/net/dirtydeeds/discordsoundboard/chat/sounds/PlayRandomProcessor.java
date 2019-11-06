package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.JDALogger;

public class PlayRandomProcessor extends SingleArgumentChatCommandProcessor {

  public PlayRandomProcessor(String prefix, SoundboardBot soundPlayer) {
    super(prefix, "Random Sound", soundPlayer);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String category = getArgument(), filePlayed = null, desc = "Did you like it?";
    if (!bot.isAllowedToPlaySound(event.getAuthor())) {
      pm(event, "You're not allowed to do that.");
      return;
    }
    try {
      if (category != null) {
        if (bot.isASoundCategory(category)) {
          desc = "Played from category **" + bot.getSoundCategory(category).getName() +
                  "**.";
          filePlayed = bot.playRandomFileForCategory(event.getAuthor(),
                       category);
        } else {
          w(event, String.format("Category '%s' not found", category));
        }
      } else {
        filePlayed = bot.playRandomFile(event.getAuthor());
      }
      if (filePlayed != null &&
          bot.getUsersVoiceChannel(event.getAuthor()) != null) {
        SoundFile file = bot.getDispatcher().getSoundFileByName(filePlayed);
        JDALogger.getLog("Sound").info("Played \"" + filePlayed + "\" in server " +
                 event.getGuild().getName());
        StyledEmbedMessage em = StyledEmbedMessage.forSoundFile(bot, file,
                                "Played Random Sound `" + filePlayed + "`",
                                desc + " \u2014 " +
                                event.getAuthor().getAsMention());
        embedForUser(event, em);
      }
    } catch (Exception e) {
      e(event, e.toString());
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " [category] - play a random sound (from " +
           "category if specified)";
  }
}