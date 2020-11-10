package net.dirtydeeds.discordsoundboard.chat.admin;

import net.dirtydeeds.discordsoundboard.chat.AuthenticatedSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UserInfoProcessor extends
        AuthenticatedSingleArgumentChatCommandProcessor {

  public UserInfoProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "User", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    User user = (getArgument() != null) ?
            bot.getUserByName(getArgument()) : event.getAuthor();
    if (user == null && getArgument() != null) {
      pm(event, "Did not find a user with username `" + getArgument() +
              "`. *Can I see him/her?*");
      return;
    }
    pm(event, String.format(
            "%s\nEntrance: %s\nCan Play Sounds: %b\nThrottled: %b\nModerator: %b\nPrivilege Level: %d",
            user.getName(),
            bot.getEntranceForUser(user),
            bot.isAllowedToPlaySound(user),
            bot.isThrottled(user),
            bot.isAuthenticated(user, event.getGuild()),
            bot.getUser(user).getPrivilegeLevel()));
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " [username] (*) - get info about a user (self if " +
            "no username specified)";
  }
}