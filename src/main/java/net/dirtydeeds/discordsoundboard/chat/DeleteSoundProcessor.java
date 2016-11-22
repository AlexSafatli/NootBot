package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class DeleteSoundProcessor extends
		OwnerSingleArgumentChatCommandProcessor {

	public DeleteSoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String filename = getArgument();
		if (filename != null) {
			SoundFile file = bot.getSoundMap().get(filename);
			if (file == null) {
				pm(event, "No sound file exists by that name.");
			} else {
				if (file.getSoundFile().delete()) {
					bot.getDispatcher().updateFileList();
					pm(event, "Deleted file `" + filename + "` successfully.");
				} else {
					pm(event, "Failed to delete the file `" + filename + "`!");
				}
			}
		}
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - remove a sound from the file system";
	}
	
}
