package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.JDALogger;

public class PlayRandomTopSoundProcessor extends
        SingleArgumentChatCommandProcessor {

  public PlayRandomTopSoundProcessor(String prefix, SoundboardBot soundPlayer) {
    super(prefix, "Random Top Sound", soundPlayer);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String desc = "Played a random **top played** sound (see `.top`) ";
    if (!bot.isAllowedToPlaySound(event.getAuthor())) {
      pm(event, "You're not allowed to do that.");
      return;
    }
      String filePlayed = bot.getRandomTopPlayedSoundName();
      if (filePlayed != null) {
        bot.playFileForUser(filePlayed, event.getAuthor());
        SoundFile file = bot.getDispatcher().getSoundFileByName(filePlayed);
        JDALogger.getLog("Sound").info("Played \"" + filePlayed + "\" in server " +
                 event.getGuild().getName());
        StyledEmbedMessage em = StyledEmbedMessage.forSoundFile(bot, file,
                                "You've Played a Random Top Sound",
                                desc + " \u2014 " +
                                event.getAuthor().getAsMention());
        embedForUser(event, em);
      }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - play a random top played sound";
  }

}
