package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.utils.*;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;
import net.dv8tion.jda.core.entities.Game;

public class SetGameNameProcessor extends
  OwnerSingleArgumentChatCommandProcessor {

  public SetGameNameProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Set Game Name", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    int len = getPrefix().length() + 1;
    String m = event.getMessage().getContent(),
           name = (m.length() > len) ?
                  m.substring(getPrefix().length() + 1).trim() : null;
    if (name == null || name.isEmpty()) {
      Reusables.setRandomGame(bot);
      Game g = bot.getAPI().getPresence().getGame();
      m(event, "Set random game name" + ((g != null) ?  " **" + g.getName() + "**": "") + "!");
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
