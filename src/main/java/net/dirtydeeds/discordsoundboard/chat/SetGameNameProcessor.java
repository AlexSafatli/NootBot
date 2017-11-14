package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.utils.*;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class SetGameNameProcessor extends
  OwnerSingleArgumentChatCommandProcessor {

  public SetGameNameProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Set Game Name", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String m = event.getMessage().getContent(),
           name = m.substring(getPrefix().length() + 1).trim();
    if (name == null || name.isEmpty()) {
      Reusables.setRandomGame(bot);
      m(event, "Set random game name!");
    } else {
      bot.getAPI().getPresence().setGame(Game.of(name));
      m(event, "Set game name to `" + name + "`!");
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " [name] (*) - set a game name or randomize";
  }

}
