package net.dirtydeeds.discordsoundboard.async;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.*;

public class PeriodicLambdas {

  public static PeriodicLambdaJob askForDonation() {
    return new PeriodicLambdaJob(Reusables::sendDonationMessage, Periodic.EVERY_TWO_DAYS);
  }

  public static PeriodicLambdaJob cleanOldBotMessages() {
    return new PeriodicLambdaJob(SoundboardBot::clearPreviousMessages, Periodic.EVERY_SIX_HOURS);
  }

  public static PeriodicLambdaJob changeToRandomGame() {
    return new PeriodicLambdaJob(Reusables::setRandomGame, Periodic.EVERY_TWO_MINUTES);
  }

}