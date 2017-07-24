package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.async.PlaySoundsJob;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class PlaySoundSequenceProcessor extends MultiArgumentChatCommandProcessor {

  public static final int MAX_NUMBER_OF_PLAYS = 24;

  public PlaySoundSequenceProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Sound Sequencer", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    User user = event.getAuthor();
    final String[] sounds = getArguments();
    if (!bot.isAllowedToPlaySound(user)) {
      pm(event, lookupString(Strings.NOT_ALLOWED));
    } else if (sounds.length <= 0 || sounds.length > MAX_NUMBER_OF_PLAYS) {
      pm(event, "Need to be <= **" + MAX_NUMBER_OF_PLAYS + "** for number of sounds to play."); return;
    } else {
      bot.getDispatcher().getAsyncService().runJob(new PlaySoundsJob(sounds, bot, user));
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " soundfile, [soundfile2], ..., [soundfileX] - play a sequence of sound files where X <= " + MAX_NUMBER_OF_PLAYS;
  }

}
