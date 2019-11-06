package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.chat.MultiArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.SimpleLogger;

public class PlaySoundForUserProcessor extends
        MultiArgumentChatCommandProcessor {

  public static final SimpleLogger LOG = SimpleLogger.getLog("SoundForUser");

  public PlaySoundForUserProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Sound for User", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    User user = event.getAuthor(), recipient = null;
    if (getArguments().length != 2) {
      pm(event, "This command requires \u2014 a **user** " +
              "and a **sound** to play.");
      return;
    }
    String username = getArguments()[0], filename = getArguments()[1];
    if (username != null) recipient = bot.getUserByName(username);
    if (!bot.isAllowedToPlaySound(user)) {
      pm(event, "You're not allowed to do that.");
      LOG.info(String.format("%s tried to play sound file but is not allowed.",
                             user));
    } else if (bot.getSoundMap().get(filename) == null) {
      String suggestion = "Check your spelling.",
             possibleName = bot.getClosestMatchingSoundName(filename);
      if (possibleName != null) {
        suggestion = "Did you mean `" + possibleName + "`?";
      }
      pm(event, "The sound `" +
         filename + "` was not found. *" + suggestion + "*.");
      LOG.info("Sound was not found.");
    } else if (recipient == null) {
      pm(event, "That user was not found.");
      LOG.info(
        String.format("%s wants to play \"%s\" for %s but user not found.",
                      user.getName(), filename, username));
    } else {
      LOG.info(String.format("%s is playing sound file \"%s\" for user %s.",
                             user.getName(), filename, recipient.getName()));
      try {
        String played = bot.playFileForUser(filename, recipient);
        if (played != null) {
          pm(event, String.format(
               "Played sound `%s` for user **%s** in **%s**.", played,
               recipient.getName(),
               bot.getUsersVoiceChannel(recipient).getGuild().getName()));
        } else pm(event, String.format("Could not play sound `%s` for that user. *Is he/she in a channel?*",
                                        filename));
      } catch (Exception e) {
        e(event, "Exception encountered => " + e.getMessage());
      }
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <username>, <soundfile> - plays a file by name " +
           "for a particular user; will move to them and can be used anywhere";
  }

}
