package net.dirtydeeds.discordsoundboard.chat.users;

import net.dirtydeeds.discordsoundboard.chat.AuthenticatedSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DisallowUserProcessor extends
        AuthenticatedSingleArgumentChatCommandProcessor {

  public DisallowUserProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "I Smite Thee", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (getArgument() != null) {
      String username = getArgument();
      if (username.equals(event.getAuthor().getName())) {
        pm(event, "You cannot do this to yourself.");
      } else if (bot.disallowUser(username)) {
        pm(event, String.format("Disallowing user **%s** from playing sounds.", username));
        m(event, "**" + bot.getUserByName(username).getName() +
                "** has had his sound playing privileges removed. **Rekt**.");
      } else {
        pm(event, String.format("User %s was not found.", username));
      }
    }
  }

  @Override
  public String getCommandHelpString() {
    return super.getCommandHelpString() +
            " - disallow user from playing sounds";
  }
}