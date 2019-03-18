package net.dirtydeeds.discordsoundboard.chat.users;

import net.dirtydeeds.discordsoundboard.chat.AuthenticatedSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class DisallowUserProcessor extends
        AuthenticatedSingleArgumentChatCommandProcessor {

  public DisallowUserProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "I Smite Thee", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (getArgument() != null) {
      String username = getArgument();
      if (username.equals(event.getAuthor().getName())) {
        pm(event, lookupString(Strings.NOT_TO_SELF));
      } else if (bot.disallowUser(username)) {
        pm(event, formatString(Strings.USER_DISALLOW_ALLOWED, username));
        m(event, "**" + bot.getUserByName(username).getName() +
                "** has had his sound playing privileges removed. **Rekt**.");
      } else {
        pm(event, formatString(Strings.USER_NOT_FOUND_ALLOWED, username));
      }
    }
  }

  @Override
  public String getCommandHelpString() {
    return super.getCommandHelpString() +
            " - disallow user from playing sounds";
  }
}