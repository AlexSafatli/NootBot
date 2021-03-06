package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.chat.AuthenticatedSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UnmuteSoundProcessor extends
        AuthenticatedSingleArgumentChatCommandProcessor {

  public UnmuteSoundProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Unmute Bot", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (event.isFromType(ChannelType.PRIVATE)) {
      pm(event, "You need to do this in a server.");
      return;
    }
    bot.unmuteSound(event.getGuild());
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " - unmute the bot";
  }
}