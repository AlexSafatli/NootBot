package net.dirtydeeds.discordsoundboard.chat;

import java.util.Random;
import java.util.Arrays;
import java.util.List;

import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class RandomReactionProcessor implements ChatCommandProcessor {

  protected SoundboardBot bot;
  public static final SimpleLog LOG = SimpleLog.getLog("RandomReaction");
  private static final int N = 100;
  private static final List<String> REACTIONS = Arrays.asList(new
          String[]{"👏", "✌", "🤔", "😑", "😓", "😒"});
  private Random rng;

  public RandomReactionProcessor(SoundboardBot bot) {
    this.bot = bot;
    this.rng = new Random();
  }

  public String getTitle() {
    return "Random Reaction";
  }

  public void process(MessageReceivedEvent event) {
    if (isApplicableCommand(event)) {
      String reaction = StringUtils.randomString(REACTIONS);
      LOG.info("Random reaction " + reaction + " applied to message " +
              event.getMessage());
      event.getMessage().addReaction(reaction).queue();
    }
  }

  public boolean isApplicableCommand(MessageReceivedEvent event) {
    // 1 in N chance
    return (event.getMessage().getAttachments().isEmpty() &&
            rng.nextInt(N) == 0);
  }

  public boolean canBeRunByAnyone() {
    return false;
  }

  public boolean canBeRunBy(User user, Guild guild) {
    return false;
  }

  public String getCommandHelpString() {
    return "";
  }
}