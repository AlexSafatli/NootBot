package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class AuthenticatedMultiArgumentChatCommandProcessor extends
        MultiArgumentChatCommandProcessor {

  public AuthenticatedMultiArgumentChatCommandProcessor(String prefix,
                                                        String title, SoundboardBot bot) {
    super(prefix, title, bot);
  }

  @Override
  public boolean isApplicableCommand(MessageReceivedEvent event) {
    if (super.isApplicableCommand(event)) {
      if (canBeRunBy(event.getAuthor(), event.getGuild()))
        return true;
      else {
        pm(event, "This command is not for you.");
        bot.sendMessageToUser(String.format("User **%s** just tried to run `%s`.",
                event.getAuthor().getName(),
                event.getMessage().getContent()),
                bot.getOwner());
      }
    }
    return false;
  }

  protected abstract void handleEvent(MessageReceivedEvent event,
                                      String message);

  @Override
  public boolean canBeRunByAnyone() {
    return false;
  }

  @Override
  public boolean canBeRunBy(User user, Guild guild) {
    return bot.isAuthenticated(user, guild);
  }

  @Override
  public String getCommandHelpString() {
    return super.getCommandHelpString() + " (*)";
  }
}