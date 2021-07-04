package net.dirtydeeds.discordsoundboard.chat.users;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.Objects;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class SetEntranceProcessor extends SingleArgumentChatCommandProcessor {

  private static final int WARNING_DURATION_IN_SECONDS = 5;
  private static final int MAX_DURATION_IN_SECONDS = 8;

  public SetEntranceProcessor(String prefix, SoundboardBot bot, CommandListUpdateAction commands) {
    super(prefix, "entrance", "Entrance", true, bot);
    commands.addCommands(new CommandData("entrance", "Set your entrance sound when joining a voice channel (or clear it if nothing is specified).")
            .addOptions(new OptionData(STRING, "sound", "The sound file to play")));
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String fileName = getArgument();
    if (bot.isAllowedToPlaySound(event.getAuthor())) {
      if (fileName == null) {
        bot.setEntranceForUser(event.getAuthor(), null, null);
        pm(event, "Your entrance has been cleared.");
        return;
      }
      SoundFile file = bot.getSoundMap().get(fileName);
      if (file == null) {
        pm(event, "That sound was not found.");
        return;
      }
      boolean authenticated = event.isFromGuild() && bot.isAuthenticated(
              event.getAuthor(), event.getGuild());
      if (!authenticated && file.getDuration() != null &&
              file.getDuration() > MAX_DURATION_IN_SECONDS) {
        pm(event, "The file `" + fileName +
                "` is far too **long**! *Pick something shorter*.");
        return;
      }
      bot.setEntranceForUser(event.getAuthor(), fileName, null);
      pm(event, "Updated your entrance to sound file `" + fileName + "`!");
      if (file.getDuration() > WARNING_DURATION_IN_SECONDS) {
        String mightBeLong = String.format("The file `%s` (%ds) may be a bit **long**.",
                fileName, file.getDuration());
        pm(event, mightBeLong +
                " *Be careful of setting very long entrances!*");
      }
    }
  }

  protected void handleEvent(SlashCommandEvent event) {
    OptionMapping opt = event.getOption("sound");
    String fileName = opt != null ? opt.getAsString() : null;
    if (bot.isAllowedToPlaySound(event.getUser())) {
      if (fileName == null) {
        bot.setEntranceForUser(event.getUser(), null, null);
        pm(event, "Your entrance has been cleared.");
        return;
      }
      SoundFile file = bot.getSoundMap().get(fileName);
      if (file == null) {
        pm(event, "That sound was not found.");
        return;
      }
      boolean authenticated = event.isFromGuild() && bot.isAuthenticated(
              event.getUser(), event.getGuild());
      if (!authenticated && file.getDuration() != null &&
              file.getDuration() > MAX_DURATION_IN_SECONDS) {
        pm(event, "The file `" + fileName +
                "` is far too **long**! *Pick something shorter*.");
        return;
      }
      bot.setEntranceForUser(event.getUser(), fileName, null);
      pm(event, "Updated your entrance to sound file `" + fileName + "`!");
      if (file.getDuration() > WARNING_DURATION_IN_SECONDS) {
        String mightBeLong = String.format("The file `%s` (%ds) may be a bit **long**.",
                fileName, file.getDuration());
        pm(event, mightBeLong +
                " *Be careful of setting very long entrances!*");
      }
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <soundfile> - set a sound as your entrance for " +
            "when you join a channel";
  }
}