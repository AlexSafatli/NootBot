package net.dirtydeeds.discordsoundboard.chat;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dirtydeeds.discordsoundboard.beans.User;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class RollDiceProcessor extends SingleArgumentChatCommandProcessor {

  private static final int MIN_DICE_HEADS = 1;
  private static final int MAX_DICE_HEADS = 100;
  private static final int MIN_DICE = 1;
  private static final int MAX_DICE = 100;

  private final Random rng;
  private final Pattern dice;

  public RollDiceProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Roll Dice", bot);
    rng = new Random();
    dice = Pattern.compile("(\\d+)d(\\d+)\\+?(\\d+)?");
  }

  private String roll(int dice, int sides, int add) {
    String out = String.format("Rolling %d %d-sided dice and got:\n", dice, sides);
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
    String msg = event.getMessage().getContent().trim();
    User u = bot.getUser(event.getAuthor());
    Matcher m = dice.matcher(msg);
    if (m.find()) {
      int dice = Math.max(Integer.valueOf(match.group(1)), MIN_DICE);
      int sides = Math.max(Integer.valueOf(match.group(2)), MIN_DICE_HEADS);
      int add = 0;
      if (dice > MAX_DICE) {
        pm(event, "Max number of dice that can be rolled is **" + MAX_DICE + "**.");
        return;
      } else if (sides > MAX_DICE_HEADS) {
        pm(event, "Max number of dice heads that can be rolled is **" + MAX_DICE_HEADS + "**.");
        return;
      }
      if (match.group(3) != null && !"null".equals(match.group(3))) {
        add = Math.max(Integer.valueOf(match.group(3)), 0);
      }
      m(event, roll(dice, sides, add));
    } else {
      pm(event, "Want syntax in the form XdY+z \u2014 e.g., 2d5+2 rolls 2 dice of 1-5 and returns the sum plus 2.");
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <dice> - roll dice that is in XdY syntax";
  }

}
