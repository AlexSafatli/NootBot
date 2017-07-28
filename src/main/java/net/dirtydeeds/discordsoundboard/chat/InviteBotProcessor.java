package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class InviteBotProcessor extends
  OwnerSingleArgumentChatCommandProcessor {

  private static final String LINK_PREFIX =
    "https://discordapp.com/oauth2/authorize?client_id=";
  private static final String LINK_SUFFIX = "&scope=bot&permissions=339209287";

  public InviteBotProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "I'm Honored!", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    pm(event, LINK_PREFIX + bot.getAPI().getSelfUser().getId() + LINK_SUFFIX);
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() +
           " (*) - get a link that allows this bot to be added to your server";
  }
}