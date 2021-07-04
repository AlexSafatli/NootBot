package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.moderation.ModerationRules;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.utils.JDALogger;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class PlaySoundProcessor extends SingleArgumentChatCommandProcessor {

  public PlaySoundProcessor(String prefix, SoundboardBot bot, CommandListUpdateAction commands) {
    super(prefix, "sound", "Play Sound", true, bot);
    commands.addCommands(new CommandData("sound", "Plays a sound file in the current channel.")
            .addOptions(new OptionData(STRING, "sound", "The sound file to play")
                    .setRequired(true)));
  }

  private void sendBadSoundMessage(MessageReceivedEvent event, String name,
                                   String suggestion, User user) {
    StyledEmbedMessage msg = buildStyledEmbedMessage(event);
    msg.setTitle("Sound Not Found");
    msg.addDescription("Sound `" + name + "` not found. " + suggestion + Strings.SEPARATOR + user.getAsMention());
    msg.addContent("Search", "You can use `.search` with a keyword.", true);
    embed(event, msg.isWarning(true));
  }

  private void sendBadSoundMessage(SlashCommandEvent event, String name,
                                   String suggestion, User user) {
    StyledEmbedMessage msg = buildStyledEmbedMessage(event);
    msg.setTitle("Sound Not Found");
    msg.addDescription("Sound `" + name + "` not found. " + suggestion + Strings.SEPARATOR + user.getAsMention());
    msg.addContent("Search", "You can use `.search` with a keyword.", true);
    embed(event, msg.isWarning(true));
  }

  private String play(User user, String name) throws Exception {
    if (StringUtils.containsAny(name, '?')) {
      return null;
    } else if (bot.getSoundMap().get(name) == null) {
      JDALogger.getLog("Sound").info("Sound was not found.");
      String suggestion = "Check your spelling.",
              possibleName = bot.getClosestMatchingSoundName(name);
      if (name.equals("help")) {
        suggestion = "Were you trying to access the `.help` command?";
      } else if (possibleName != null) {
        suggestion = String.format("Did you mean `%s`?", possibleName);
      } else {
        // Do a naive search to see if something contains this name. Stop early.
        for (String s : bot.getSoundMap().keySet()) {
          if (s.contains(name)) {
            suggestion = String.format("Did you mean `%s`?", s);
            break;
          }
        }
      }
      JDALogger.getLog("Sound").info("Suggestion: " + suggestion);
      return suggestion;
    } else bot.playFileForChatCommand(name, user);
    return null;
  }

  @Override
  public void process(MessageReceivedEvent event) {
    String message = event.getMessage().getContentRaw().toLowerCase();
    if (StringUtils.containsOnly(message, '?')) return;
    super.process(event);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    User user = event.getAuthor();
    String name = message.substring(1);
    if (!bot.isAllowedToPlaySound(user)) {
      pm(event, "You're not allowed to do that.");
      JDALogger.getLog("Sound").info(
              String.format("%s isn't allowed to play sounds.", user.getName()));
    }
    String suggestion = null;
    try {
      suggestion = play(user, name);
    } catch (Exception e) {
      e(event, e.getMessage());
    }
    if (suggestion != null) {
      sendBadSoundMessage(event, name, suggestion, user);
    }
  }

  protected void handleEvent(SlashCommandEvent event) {
    User user = event.getUser();
    OptionMapping opt = event.getOption("sound");
    String name = opt != null ? opt.getAsString() : "";
    if (!bot.isAllowedToPlaySound(user)) {
      pm(event, "You're not allowed to do that.");
      JDALogger.getLog("Sound").info(
              String.format("%s isn't allowed to play sounds.", user.getName()));
    }
    String suggestion = null;
    try {
      suggestion = play(user, name);
    } catch (Exception e) {
      e(event, e.getMessage());
    }
    if (suggestion != null) {
      sendBadSoundMessage(event, name, suggestion, user);
    } else {
      m(event, "Played sound " + name);
    }
  }

  @Override
  public String getCommandHelpString() {
    String help = getPrefix() + "soundfile - play a sound by name";
    Set<String> soundFileNames = bot.getSoundMap().keySet();
    if (!soundFileNames.isEmpty()) {
      help += " - e.g., " +
              getPrefix() + StringUtils.randomString(soundFileNames);
    }
    return help;
  }
}
