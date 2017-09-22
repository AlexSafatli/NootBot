package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.async.DeleteInactiveChannelJob;
import net.dirtydeeds.discordsoundboard.utils.*;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class TempVoiceChannelProcessor extends
  AuthenticatedSingleArgumentChatCommandProcessor {

  public TempVoiceChannelProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Create Temporary Voice Channel", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    if (event.getGuild() == null) {
      pm(event, "You need to do this in a server.");
      return;
    }
    ServerUtils.addVoiceChannel(event.getGuild(), null, (Channel c) -> {
      m(event, "Created channel with name *" + c.getName() + "*.");
      bot.getDispatcher().getAsyncService().runJob(
        new DeleteInactiveChannelJob(c));
    });
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " (*) - create a temporary channel with a random name " +
           "that lasts until inactive for 30min";
  }
}