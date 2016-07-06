package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class SetEntranceProcessor extends
		SingleArgumentChatCommandProcessor {

	private static final int WARNING_FILE_SIZE_IN_BYTES = 750000;
	
	public SetEntranceProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String fileName = getArgument();
		if (bot.isAllowedToPlaySound(event.getAuthor())) {
			if (fileName == null) {
				bot.setEntranceForUser(event.getAuthor(), null);
				pm(event, "Cleared your entrance.");
			} else if (bot.getSoundMap().get(fileName) != null) {
				bot.setEntranceForUser(event.getAuthor(), fileName);
				pm(event, "Updated your entrance to sound file `" + fileName + "`!");
				if (bot.getSoundMap().get(fileName).getSoundFile().length() 
						> WARNING_FILE_SIZE_IN_BYTES) {
					pm(event, "The file `" + fileName + "` may be a bit large. "
							+ "*Be careful of setting very long entrances!*");
				}
			} else {
				pm(event, "That sound does not exist. *Check your spelling.*");
			}
		}
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " <soundfile>` - set a sound as your entrance for when you join a channel";
	}

}
