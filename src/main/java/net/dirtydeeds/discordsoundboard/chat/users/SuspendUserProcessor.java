package net.dirtydeeds.discordsoundboard.chat.users;

import net.dirtydeeds.discordsoundboard.chat.OwnerMultiArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.moderation.ModerationRules;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class SuspendUserProcessor extends
        OwnerMultiArgumentChatCommandProcessor {

  public SuspendUserProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Give User Suspend Role", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (getArguments().length < 1) {
      e(event, "Not enough arguments.");
      return;
    }
    if (event.getGuild() == null) {
      pm(event, "You did not send this command in a server.");
      return;
    }
    String uName = getArguments()[0];
    if (!bot.getRulesForGuild(event.getGuild()).isPermitted()) {
      pm(event, "You haven't given me permission to moderate roles yet.");
      return;
    }
    ModerationRules rules = bot.getRulesForGuild(event.getGuild());
    if (rules.getSuspendedRole() == null) {
      pm(event, "No suspended role defined.");
      return;
    }
    User user = bot.getUserByName(uName);
    if (user != null) {
      Member m = event.getGuild().getMember(user);
      if (m != null) {
        rules.giveSuspendedRole(m).queue(x ->
                pm(event, "Gave role `" + rules.getSuspendedRole().getName() +
                        "` to user " + m.getEffectiveName()));
      }
    } else {
      pm(event, "User not found.");
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <username> (*) - gives a user the server's suspended role";
  }
}