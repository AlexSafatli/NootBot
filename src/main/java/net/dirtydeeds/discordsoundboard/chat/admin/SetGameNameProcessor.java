package net.dirtydeeds.discordsoundboard.chat.admin;

import net.dirtydeeds.discordsoundboard.chat.OwnerSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.utils.*;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.entities.Activity;

public class SetGameNameProcessor extends
        OwnerSingleArgumentChatCommandProcessor {

  public SetGameNameProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Set Game Name", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    int len = getPrefix().length() + 1;
    String m = event.getMessage().getContentRaw(),
           name = (m.length() > len) ? m.substring(len).trim() : null;
    if (name == null) {
      Reusables.setRandomGame(bot);
      Activity g = bot.getAPI().getPresence().getActivity();
      String r = "Set random game name" + ((g != null) ?  " **" + g.getName() + "**": "") + "!";
      if (event.isFromType(ChannelType.PRIVATE)) {
        pm(event, r);
      } else {
        m(event, r);
      }
    } else {
      bot.getAPI().getPresence().setActivity(Activity.playing(name));
      StringUtils.cacheWords(name);
      String r = "Set game name to `" + name + "`!";
      if (event.isFromType(ChannelType.PRIVATE)) {
        pm(event, r);
      } else {
        m(event, r);
      }
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " [name] (*) - set a game name or randomize";
  }

}
