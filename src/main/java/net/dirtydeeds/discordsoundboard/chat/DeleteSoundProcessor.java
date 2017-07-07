package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class DeleteSoundProcessor extends
		OwnerSingleArgumentChatCommandProcessor {
	
	public DeleteSoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Delete Sound", bot);
	}

	private boolean delete(SoundFile file) {
		try {
			Files.copy(file.getSoundFile(), bot.getTempPath().resolve(file.getSoundFile().getName()).toFile());
		} catch (IOException io) {
			LOG.fatal("Failed to copy file to " + bot.getTempPath());
		}
		return file.getSoundFile().delete();
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String filename = getArgument();
		if (filename != null) {
			SoundFile file = bot.getSoundMap().get(filename);
			if (file == null) {
				pm(event, lookupString(Strings.SOUND_NOT_FOUND));
			} else {
				deleteOriginalMessage(event);
				if (delete(file)) {
					bot.getDispatcher().updateFileList();
					pm(event, formatString(Strings.DELETED_SUCCESS, filename));
				} else {
					pm(event, formatString(Strings.DELETED_FAILURE, filename));
				}
			}
		}
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - remove a sound from the file system";
	}
	
}
