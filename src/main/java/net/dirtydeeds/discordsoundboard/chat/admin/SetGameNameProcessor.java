package net.dirtydeeds.discordsoundboard.chat.admin;

import java.util.List;

import net.dirtydeeds.discordsoundboard.chat.OwnerSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.utils.*;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.dv8tion.jda.api.entities.Activity;

public class SetGameNameProcessor extends
        OwnerSingleArgumentChatCommandProcessor {

  public SetGameNameProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Set Game Name", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    int len = getPrefix().length() + 1;
    String m = event.getMessage().getContentRaw(),
           name = (m.length() > len) ?
                  m.substring(getPrefix().length() + 1).trim() : null;
    if (name == null || name.isEmpty()) {
      Reusables.setRandomGame(bot);
      Activity g = bot.getAPI().getPresence().getActivity();
      m(event, "Set random game name" + ((g != null) ?  " **" + g.getName() + "**": "") + "!");
    } else {
      bot.getAPI().getPresence().setActivity(Activity.playing(name));
      m(event, "Set game name to `" + name + "`!");
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " [name] (*) - set a game name or randomize";
  }

}
