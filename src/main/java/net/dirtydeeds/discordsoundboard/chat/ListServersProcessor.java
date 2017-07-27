package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;
import java.util.LinkedList;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ListServersProcessor extends
  AuthenticatedSingleArgumentChatCommandProcessor {

  public ListServersProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Servers", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    List<Guild> guilds = bot.getGuilds();
    List<String> names = new LinkedList<>();
    if (guilds.size() > 0) {
      for (Guild guild : guilds) {
        names.append("**" + guild.getName() + "**");
      }
      m(event, "I am connected to **" + guilds.size() + " servers**:\n\n" +
        StringUtils.listToString(names));
    } else {
      w(event, "I am connected to **no servers**.");
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - list all servers connected to";
  }


}
