package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.async.PlaySoundsJob;
import net.dirtydeeds.discordsoundboard.async.PlaySoundsStaggeredJob;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class PlayRandomSoundStaggerLoopedProcessor extends
  MultiArgumentChatCommandProcessor {

  public static final int MAX_NUMBER_OF_LOOPED_PLAYS = 12;

  public PlayRandomSoundStaggerLoopedProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Random Stagger Loop", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    User user = event.getAuthor();
    String cat = (getArguments().length > 1) ? getArguments()[1] : null;
    int numTimesToPlay = (getArguments().length > 0) ?
                         Integer.valueOf(getArguments()[0]) : 0;
    boolean privileged = bot.getUser(user).isPrivileged();
    if (cat != null && !cat.isEmpty() && !bot.isASoundCategory(cat)) {
      w(event, formatString(Strings.NOT_FOUND, cat));
      cat = null;
    }
    if (!bot.isAllowedToPlaySound(user)) {
      pm(event, lookupString(Strings.NOT_ALLOWED));
    } else if (numTimesToPlay <= 0 || (numTimesToPlay >
                                       MAX_NUMBER_OF_LOOPED_PLAYS &&
                                       !privileged)) {
      e(event, "Number of plays needs to be positive and less than " +
              "or equal to **" + MAX_NUMBER_OF_LOOPED_PLAYS + "**.");
    } else {
      String[] sounds = new String[numTimesToPlay];
      PlaySoundsStaggeredJob later = new PlaySoundsStaggeredJob(sounds, bot, user, cat);
      bot.getDispatcher().getAsyncService().runJob(later);
      pm(event, "You'll hear a sound played next at " + later.runsAt());
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() +
           " X, <category> - play a random sound X number of times, staggered, where X <= "
           + MAX_NUMBER_OF_LOOPED_PLAYS;
  }
}