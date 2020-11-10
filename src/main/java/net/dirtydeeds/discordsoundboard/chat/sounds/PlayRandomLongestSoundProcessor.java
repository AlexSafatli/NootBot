package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.JDALogger;

public class PlayRandomLongestSoundProcessor extends
        SingleArgumentChatCommandProcessor {

  public PlayRandomLongestSoundProcessor(String prefix, SoundboardBot soundPlayer) {
    super(prefix, "Random Long Sound", soundPlayer);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String desc = "See `.longest` for **longest** duration sounds ";
    if (!bot.isAllowedToPlaySound(event.getAuthor())) {
      pm(event, "You're not allowed to do that.");
      return;
    }
    String filePlayed = bot.getRandomLongestSoundName();
    if (filePlayed != null) {
      bot.playFileForUser(filePlayed, event.getAuthor());
      SoundFile file = bot.getDispatcher().getSoundFileByName(filePlayed);
      JDALogger.getLog("Sound").info("Played \"" + filePlayed + "\" in server " +
               event.getGuild().getName());
      StyledEmbedMessage em = StyledEmbedMessage.forSoundFile(bot, file,
                              "Played Random Long Sound",
                              desc + " \u2014 " +
                              event.getAuthor().getAsMention());
      embedForUser(event, em);
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - play a random long duration (>10 seconds) sound";
  }

}
