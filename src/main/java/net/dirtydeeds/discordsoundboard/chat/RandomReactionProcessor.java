package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.JDALogger;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomReactionProcessor implements ChatCommandProcessor {

  protected SoundboardBot bot;
  private static final int N = 100;
  private static final List<String> REACTIONS = Arrays.asList("👏", "✌", "🤔", "😑", "😓", "😒");
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
      JDALogger.getLog("RandomReaction").info("Random reaction " + reaction + " applied to message " +
              event.getMessage());
      event.getMessage().addReaction(reaction).queue();
    }
  }

  public void processAsSlashCommand(SlashCommandEvent event) {

  }

  public boolean isApplicableCommand(MessageReceivedEvent event) {
    // 1 in N chance
    return (event.getMessage().getAttachments().isEmpty() &&
            rng.nextInt(N) == 0);
  }

  public boolean isApplicableCommand(SlashCommandEvent event) {
    return false;
  }

  public boolean canBeRunByAnyone() {
    return false;
  }

  public boolean canBeRunBy(User user, Guild guild) {
    return false;
  }

  public boolean canBeRunAsSlashCommand() {
    return false;
  }

  public String getCommandHelpString() {
    return "";
  }
}