package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.chat.AuthenticatedSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UpdateSoundsProcessor extends
        AuthenticatedSingleArgumentChatCommandProcessor {

  public UpdateSoundsProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Update Sounds", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    pm(event, "Updating file list.");
    bot.getDispatcher().updateFileList();
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " (*) - refresh the sound/file list";
  }
}