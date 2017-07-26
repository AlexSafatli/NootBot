package net.dirtydeeds.discordsoundboard.chat;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class RollDiceProcessor extends SingleArgumentChatCommandProcessor {

  private static final int MIN_DICE_HEADS = 1;
  private static final int MAX_DICE_HEADS = 100;
  private static final int MIN_DICE = 1;
  private static final int MAX_DICE = 100;
  private static final String NULL = "null";

  private final Random rng;
  private final Pattern dice;

  public RollDiceProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Roll Dice", bot);
    rng = new Random();
    dice = Pattern.compile("(\\d+)d(\\d+)\\+?(\\d+)?");
  }

  @Override
  protected void clearBuffer() {
    buffer.clear(); // Do not delete past messages.
  }

  private String roll(int dice, int sides, int add) {
    String out = "";
    int total = 0;
    for (int i = 0; i < dice; ++i) {
      int roll = rng.nextInt(sides) + 1;
      out += " `" + roll + "`";
      total += roll;
    }
    if (add > 0) {
      out += " and adding `" + add + "`";
      total += add;
    }
    return out + "\nResult: **" + total + "**";
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    Matcher m = dice.matcher(event.getMessage().getContent().trim());
    if (m.find()) {
      int dice = Math.max(Integer.valueOf(m.group(1)), MIN_DICE);
      int sides = Math.max(Integer.valueOf(m.group(2)), MIN_DICE_HEADS);
      int add = 0;
      if (dice > MAX_DICE) {
        w(event, "Max number of dice that can be rolled is **" + MAX_DICE + "**.");
        return;
      } else if (sides > MAX_DICE_HEADS) {
        w(event, "Max number of dice heads that can be rolled is **" + MAX_DICE_HEADS + "**.");
        return;
      }
      if (m.group(3) != null && !NULL.equals(m.group(3))) {
        add = Math.max(Integer.valueOf(m.group(3)), 0);
      }
      m(event, "Rolling `" + getArgument() + "` and got:\n" + roll(dice, sides, add));
    } else {
      w(event, "This command needs syntax in the form `xdy+z` \u2014 " +
        "e.g., `2d5+2` rolls 2 dice with possible sides ranging from 1 " +
        "to 5 and returns the result plus 2.");
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <dice> - roll dice that is in xdy syntax";
  }

}
