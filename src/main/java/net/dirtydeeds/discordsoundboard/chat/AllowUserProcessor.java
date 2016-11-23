package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class AllowUserProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

	private final String NO_USER_FOUND = "NO_USER_FOUND_DISALLOWED_WITH_USERNAME";
	
	public AllowUserProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		if (getArgument() != null) {
			String username = getArgument();
			if (bot.allowUser(username))
				pm(event, "Allowing user `" + username + "` to play sounds again.");
			else pm(event, formatString(NO_USER_FOUND, username));
		}
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - allow a disallowed user to play sounds again";
	}
	
}
