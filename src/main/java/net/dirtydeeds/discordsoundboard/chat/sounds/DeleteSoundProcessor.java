package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.OwnerSingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class DeleteSoundProcessor extends
        OwnerSingleArgumentChatCommandProcessor {

  public DeleteSoundProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Delete Sound", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String filename = getArgument();
    if (filename != null) {
      SoundFile file = bot.getSoundMap().get(filename);
      if (file == null) {
        pm(event, "That sound was not found.");
      } else {
        deleteOriginalMessage(event);
        if (file.getSoundFile().delete()) {
          bot.getDispatcher().updateFileList();
          pm(event, String.format("Deleted '%s'", filename));
        } else {
          pm(event, String.format("Could not delete '%s'", filename));
        }
      }
    }
  }

  @Override
  public String getCommandHelpString() {
    return super.getCommandHelpString() +
            " - remove a sound from the file system";
  }
}