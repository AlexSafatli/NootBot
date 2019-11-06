package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NoOpProcessor extends SingleArgumentChatCommandProcessor {

  public NoOpProcessor(SoundboardBot b) {
    super("", "", b);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    ;
  }
}