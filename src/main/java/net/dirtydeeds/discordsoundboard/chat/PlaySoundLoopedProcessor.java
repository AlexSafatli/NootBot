package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.async.PlaySoundsJob;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class PlaySoundLoopedProcessor extends MultiArgumentChatCommandProcessor {

  public static final SimpleLog LOG = SimpleLog.getLog("SoundLoopProcessor");
  public static final int MAX_NUMBER_OF_LOOPED_PLAYS = 12;

  public PlaySoundLoopedProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Loop Sound", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    User user = event.getAuthor();
    if (getArguments().length != 2) {
      e(event, "Need **sound name** and **number of times** to play that sound.");
      return;
    }
    boolean privileged = bot.getUser(user).isPrivileged();
    String name = getArguments()[0];
    int numTimesToPlay = Integer.valueOf(getArguments()[1]);
    LOG.info(String.format("%s wants to play \"%s\".", user.getName(), name));
    if (!bot.isAllowedToPlaySound(user)) {
      pm(event, lookupString(Strings.NOT_ALLOWED));
      LOG.info(String.format("%s isn't allowed to play sounds.", user.getName()));
    } else if (numTimesToPlay <= 0 || (numTimesToPlay > MAX_NUMBER_OF_LOOPED_PLAYS && !privileged)) {
      e(event, "Need to be <= **" + MAX_NUMBER_OF_LOOPED_PLAYS + "** for number of plays."); return;
    } else if (StringUtils.containsAny(name, '?')) {
      return; // File names cannot contain question marks.
    } else if (bot.getSoundMap().get(name) == null) {
      String suggestion = "Check your spelling.", possibleName = bot.getClosestMatchingSoundName(name);
      if (possibleName != null) {
        LOG.info("Closest matching sound name is: " + possibleName);
        suggestion = "Did you mean `" + possibleName + "`?";
      }
      w(event, formatString(Strings.SOUND_NOT_FOUND_SUGGESTION, name, suggestion, user.getAsMention()));
      LOG.info("Sound was not found.");
    } else {
      String[] sounds = new String[numTimesToPlay];
      for (int i = 0; i < numTimesToPlay; ++i) sounds[i] = name;
      bot.getDispatcher().getAsyncService().runJob(new PlaySoundsJob(sounds, bot, user));
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <soundfile>, X - play a sound by name X number of times where X <= " + MAX_NUMBER_OF_LOOPED_PLAYS;
  }

}
