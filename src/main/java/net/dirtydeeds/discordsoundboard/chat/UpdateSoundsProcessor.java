package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class UpdateSoundsProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

	public UpdateSoundsProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Update Sounds", bot);
	}
	
	protected void handleEvent(MessageReceivedEvent event, String message) {
		pm(event, "Updating file list.");
		bot.getDispatcher().updateFileList();
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` (`*`) \u2014 refresh the sound/file list";
	}
	
}
