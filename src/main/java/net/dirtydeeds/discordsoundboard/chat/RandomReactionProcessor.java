package net.dirtydeeds.discordsoundboard.chat;

import java.util.Random;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class RandomReactionProcessor implements ChatCommandProcessor {

  protected SoundboardBot bot;
  public static final SimpleLog LOG = SimpleLog.getLog("RandomReactionProcessor");
  private static final String TEASE = "Sushiman777";
  private static final int N = 100;
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
      if (event.getAuthor().getName().equals(TEASE)) {
        bot.sendMessageToUser("If this was League of Legends, I would have crit you.", TEASE);
      }
      LOG.info("Random reaction 👏 applied to message " + event.getMessage());
      event.getMessage().addReaction("👏").queue();
    }
  }
  
  public boolean isApplicableCommand(MessageReceivedEvent event) {
    // 1 in N chance
    return (event.getMessage().getAttachments().isEmpty() && rng.nextInt(N) == 0);
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
