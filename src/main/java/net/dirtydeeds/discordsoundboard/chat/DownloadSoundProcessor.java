package net.dirtydeeds.discordsoundboard.chat;

import java.io.File;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class DownloadSoundProcessor extends
		AuthenticatedSingleArgumentChatCommandProcessor {

	public DownloadSoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String filename = getArgument();
		if (filename != null) {
			SoundFile file = bot.getSoundMap().get(filename);
			if (file == null) {
				pm(event, Strings.SOUND_NOT_FOUND);
			} else {
				File f = file.getSoundFile();
				event.getAuthor().getPrivateChannel().sendFileAsync(f, null, null);
			}
		}
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - download a sound";
	}
	
}
