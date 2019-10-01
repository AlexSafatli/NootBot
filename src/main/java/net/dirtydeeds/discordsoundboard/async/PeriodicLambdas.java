package net.dirtydeeds.discordsoundboard.async;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Periodic;
import net.dirtydeeds.discordsoundboard.utils.Reusables;

public class PeriodicLambdas {

  public static PeriodicLambdaJob cleanOldBotMessages() {
    return new PeriodicLambdaJob(SoundboardBot::clearPreviousMessages, Periodic.EVERY_DAY);
  }

  public static PeriodicLambdaJob changeToRandomGame() {
    return new PeriodicLambdaJob(Reusables::setRandomGame, Periodic.EVERY_QUARTER_HOUR);
  }

  public static PeriodicLambdaJob changeBotChannelTopic() {
    return new PeriodicLambdaJob(Reusables::setRandomTopicForPublicChannels, Periodic.EVERY_DAY);
  }

  public static PeriodicLambdaJob updateSayingsCaches() {
    return new PeriodicLambdaJob(Reusables::updateSayingsCache, Periodic.EVERY_WEEK);
  }

}