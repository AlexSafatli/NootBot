package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class DisallowUserProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

	public DisallowUserProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		if (getArgument() != null) {
			String username = getArgument();
			if (username.equalsIgnoreCase(bot.getOwner()))
				pm(event, "You cannot disallow yourself!");
			else if (bot.disallowUser(username))
				pm(event, "User `" + username + "` can no longer play sounds.");
			else
				pm(event, "No user to disallow found with username `" + username + "`. " 
						+ "*Have you already disallowed this user?*");
		}
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - disallows user from playing sounds";
	}
	
}
