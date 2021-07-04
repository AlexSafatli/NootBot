package net.dirtydeeds.discordsoundboard.chat.users;

import net.dirtydeeds.discordsoundboard.beans.User;
import net.dirtydeeds.discordsoundboard.chat.OwnerMultiArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class AuthenticateUserProcessor extends
        OwnerMultiArgumentChatCommandProcessor {

  public AuthenticateUserProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Authenticate User", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (getArguments().length == 2) {
      String username = getArguments()[0];
      Integer level = Integer.valueOf(getArguments()[1]);
      level = (level >= 0) ? level : 0;
      if (bot.getUserByName(username) != null) {
        User u = bot.getUser(bot.getUserByName(username));
        u.setPrivilegeLevel(level);
        bot.getDispatcher().saveUser(u);
        pm(event, "Access level changed to ||" + level + "|| for that user.");
      } else
        pm(event, "User not found.");
    }
  }

  @Override
  public String getCommandHelpString() {
    return super.getCommandHelpString() +
            " - set a user's privilege level to the bot (0-2)";
  }
}