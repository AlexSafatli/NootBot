package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class MuteSoundProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

  public MuteSoundProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Mute Bot", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (event.getGuild() == null) {
      pm(event, "You need to do this in a server.");
      return;
    }
    bot.muteSound(event.getGuild());
  }

  @Override
  public String getCommandHelpString() {
    return "`" + getPrefix() + "` \u2014 mute the bot";
  }
  
}
