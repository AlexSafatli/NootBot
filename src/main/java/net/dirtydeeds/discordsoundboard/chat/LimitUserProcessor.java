package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class LimitUserProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("LimitUserProcessor");
	
	public LimitUserProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		if (getArgument() != null) {
			String username = getArgument();
			if (username.equalsIgnoreCase(bot.getOwner())) {
				pm(event, "You cannot throttle a bot owner/yourself!");
			} else if (bot.throttleUser(username)) {
				pm(event, "Successfully throttled user `" + username + "`.");
				LOG.info("Throttled username " + username);
			} else {
				pm(event, "No user found to throttle with username `" + username + "`.");
				LOG.info("Failed to throttle username " + username);
			}
		}
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - throttle a user from using bot every **5** minutes";
	}
	
}
