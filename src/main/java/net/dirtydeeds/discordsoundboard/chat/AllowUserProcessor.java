package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class AllowUserProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

	public AllowUserProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		if (getArgument() != null) {
			String username = getArgument();
			if (bot.allowUser(username))
				pm(event, "Request processed to allow user `" + username + "` to play sounds.");
			else pm(event, "No disallowed user found to allow with that username.");
		}
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - allows a disallowed user to play sounds again";
	}
	
}
