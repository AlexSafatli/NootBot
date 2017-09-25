package net.dirtydeeds.discordsoundboard.async;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.*;

public class PeriodicLambdas {

  private static final int EVERY_HOUR = 3600;
  private static final int EVERY_SIX_HOURS = 6 * EVERY_HOUR;
  private static final int EVERY_TWELVE_HOURS = 12 * EVERY_HOUR;
  private static final int EVERY_TWO_DAYS = 48 * EVERY_HOUR;

  public static PeriodicLambdaJob askForDonation() {
    return new PeriodicLambdaJob((SoundboardBot b) -> {
      Reusables.sendDonationMessage(b);
    }, EVERY_TWO_DAYS);
  }

  public static PeriodicLambdaJob cleanOldBotMessages() {
    return new PeriodicLambdaJob((SoundboardBot b) -> {
      b.clearPreviousMessages();
    }, EVERY_SIX_HOURS);
  }

}