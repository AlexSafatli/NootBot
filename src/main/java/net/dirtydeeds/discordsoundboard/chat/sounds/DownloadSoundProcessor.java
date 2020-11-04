package net.dirtydeeds.discordsoundboard.chat.sounds;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.chat.SingleArgumentChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DownloadSoundProcessor extends SingleArgumentChatCommandProcessor {

  public DownloadSoundProcessor(String prefix, SoundboardBot bot) {
    super(prefix, "Download Sound", bot);
  }

  protected void handleEvent(MessageReceivedEvent event, String message) {
    String filename = getArgument();
    if (filename != null) {
      SoundFile file = bot.getSoundMap().get(filename);
      if (file == null) {
        pm(event, "That sound was not found.");
      } else {
        event.getAuthor().openPrivateChannel().queue(
                (PrivateChannel c) -> c.sendFile(
                        file.getSoundFile(), filename).queue());
      }
    }
  }

  @Override
  public String getCommandHelpString() {
    return getPrefix() + " <soundfile> - download a sound by name";
  }
}