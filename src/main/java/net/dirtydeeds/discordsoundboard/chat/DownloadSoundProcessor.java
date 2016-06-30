package net.dirtydeeds.discordsoundboard.chat;

import java.io.File;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class DownloadSoundProcessor extends
		AuthenticatedSingleArgumentChatCommandProcessor {

	public DownloadSoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String filename = getArgument();
		if (filename != null) {
			SoundFile file = bot.getAvailableSoundFiles().get(filename);
			if (file == null) {
				pm(event, "No sound file exists by that name.");
			} else {
				File f = file.getSoundFile();
				event.getAuthor().getPrivateChannel().sendFileAsync(f, null, null);
			}
		}
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - downloads a sound from the file system";
	}
	
}
