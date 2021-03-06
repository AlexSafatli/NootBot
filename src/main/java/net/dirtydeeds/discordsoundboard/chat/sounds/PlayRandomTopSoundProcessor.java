package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.utils.JDALogger;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class PlayRandomTopSoundProcessor extends
        SingleArgumentChatCommandProcessor {

  public PlayRandomTopSoundProcessor(String prefix, SoundboardBot bot, CommandListUpdateAction commands) {
    super(prefix, "randomtop", "Random Top Sound", bot);
    commands.addCommands(new CommandData("randomtop", "Plays a random top played sound in the current channel."));
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String desc = "See `.top` for **top played** sounds ";
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
                                "Played Random Top Sound",
                                desc + " \u2014 " +
                                event.getAuthor().getAsMention());
        embedForUser(event, em);
      }
  }

  protected void handleEvent(SlashCommandEvent event) {
    String desc = "See `.top` for **top played** sounds ";
    if (!bot.isAllowedToPlaySound(event.getUser())) {
      pm(event, "You're not allowed to do that.");
      return;
    }
    String filePlayed = bot.getRandomTopPlayedSoundName();
    if (filePlayed != null) {
      bot.playFileForUser(filePlayed, event.getUser());
      SoundFile file = bot.getDispatcher().getSoundFileByName(filePlayed);
      JDALogger.getLog("Sound").info("Played \"" + filePlayed + "\" in server " +
              event.getGuild().getName());
      StyledEmbedMessage em = StyledEmbedMessage.forSoundFile(bot, file,
              "Played Random Top Sound",
              desc + " \u2014 " +
                      event.getUser().getAsMention());
      embedForUser(event, em);
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - play a random top played sound";
  }

}
