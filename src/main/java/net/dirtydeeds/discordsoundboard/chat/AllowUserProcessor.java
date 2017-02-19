package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class AllowUserProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {
	
	public AllowUserProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Allow User", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		if (getArgument() != null) {
			String username = getArgument();
			if (bot.allowUser(username)) pm(event, formatString(Strings.USER_ALLOW_DISALLOWED, username));
			else pm(event, formatString(Strings.USER_NOT_FOUND_DISALLOWED, username));
		}
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - allow a disallowed user to play sounds again";
	}
	
}
