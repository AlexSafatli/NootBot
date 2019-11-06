package net.dirtydeeds.discordsoundboard.chat.users;

import net.dirtydeeds.discordsoundboard.chat.AuthenticatedMultiArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetEntranceForUserProcessor extends
        AuthenticatedMultiArgumentChatCommandProcessor {

  public SetEntranceForUserProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Entrance for User", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String[] args = getArguments();
    if (args.length < 2) {
      pm(event, "Need a **username** and **sound name**.");
      return;
    }
    String username = args[0], filename = args[1];
    if (filename.isEmpty()) filename = null; // Too lazy to change logic.
    User user = null;
    if (username != null) user = bot.getUserByName(username);

    if (filename != null && user != null) {
      if (bot.getSoundMap().get(filename) != null) {
        bot.setEntranceForUser(user, filename, event.getAuthor());
        pm(event, "User **" + user.getName() + "** had entrance updated" +
                " to sound `" + filename + "`.");
      } else {
        pm(event, "That sound was not found.");
      }
    } else if (filename == null) {
      assert user != null;
      bot.setEntranceForUser(user, null, null);
      pm(event, "User **" + user.getName() + "** had their entrance cleared.");
    } else {
      pm(event, "Asked to change entrance for `" + username + "` but could not "
              + "find user with that name.");
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <username>, <soundfile> (*) - set a sound file" +
            " for a user as their entrance sound when they join a channel";
  }
}