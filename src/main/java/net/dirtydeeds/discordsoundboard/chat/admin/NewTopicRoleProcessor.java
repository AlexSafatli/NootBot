package net.dirtydeeds.discordsoundboard.chat.admin;

import net.dirtydeeds.discordsoundboard.chat.OwnerMultiArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.moderation.ModerationRules;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class NewTopicRoleProcessor extends
        OwnerMultiArgumentChatCommandProcessor {

  public NewTopicRoleProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "New Topic Role", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (getArguments().length < 2) {
      e(event, "Not enough arguments.");
      return;
    }
    if (event.getGuild() == null) {
      pm(event, "You did not send this command in a server.");
      return;
    }
    String name = getArgumentsCased(event)[0], cName = getArguments()[1];
    if (!bot.getRulesForGuild(event.getGuild()).isPermitted()) {
      pm(event, "You haven't given me permission to moderate roles yet.");
      return;
    }
    ModerationRules rules = bot.getRulesForGuild(event.getGuild());
    Role newRole = rules.newTopicRole(name, cName);
    if (newRole != null) {
      pm(event, "Created a new topic role.");
      if (getArguments().length > 2) {
        for (int i = 2; i < getArguments().length; ++i) {
          User user = bot.getUserByName(getArguments()[i]);
          if (user != null) {
            Member m = event.getGuild().getMember(user);
            if (m != null) {
              rules.giveTopicRole(m, newRole);
              pm(event, "Gave role `" + newRole.getName() +
                      "` to user " + m.getEffectiveName());
            }
          }
        }
      }
    } else {
      e(event, "Failed to create a new topic role.");
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <roleName>, <channelName>[, userName1, userName2, ...] (*) " +
            "- create a new text channel and a role that only it can see and write in," +
            " optionally providing a list of user names that you want granted that role";
  }
}