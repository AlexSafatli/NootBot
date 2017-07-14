package net.dirtydeeds.discordsoundboard.async;

import java.awt.Color;

import net.dirtydeeds.discordsoundboard.Icons;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;

public class PeriodicLambdas {

  private static final String DONATION_TITLE = "Heya! I'm free but I'm not easy.";
  private static final String DONATION_MSG = "I am hosted on a personally owned computer who is always on and consuming power. That's not cheap! If you wanna help out, you can.";

  // TODO donation link

  private static final Color DONATION_COLOR = new Color(255, 153, 255);

  private static final int EVERY_HOUR = 3600;
  private static final int EVERY_SIX_HOURS = 6 * EVERY_HOUR;
  private static final int EVERY_TWELVE_HOURS = 12 * EVERY_HOUR;
  private static final int EVERY_TWO_DAYS = 48 * EVERY_HOUR;

  public static PeriodicLambdaJob askForDonation() {
    return new PeriodicLambdaJob((SoundboardBot b) -> {
      StyledEmbedMessage msg = new StyledEmbedMessage(DONATION_TITLE, b);
      msg.setColor(DONATION_COLOR);
      msg.addDescription(DONATION_MSG);
      b.sendMessageToAllGuilds(msg.getMessage());
    }, EVERY_TWO_DAYS);
  }

}
