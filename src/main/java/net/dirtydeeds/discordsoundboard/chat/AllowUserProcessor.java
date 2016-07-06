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
				pm(event, "Allowing user `" + username + "` to play sounds (again).");
			else pm(event, "No user found disallowed with username **" + username + "**.");
		}
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - allow a disallowed user to play sounds again";
	}
	
}
