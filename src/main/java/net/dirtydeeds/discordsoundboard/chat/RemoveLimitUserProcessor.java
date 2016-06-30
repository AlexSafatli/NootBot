package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class RemoveLimitUserProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("RemoveLimitUserProcessor");
	
	public RemoveLimitUserProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		if (getArgument() != null) {
			String username = getArgument();
			if (bot.unthrottleUser(username)) {
				pm(event, "Stopped throttling user `" + username + "`.");
			} else {
				pm(event, "No throttled user found to stop throttling with username `" + username + "`.");
				LOG.info("No throttled user to unthrottle with username " + username);
			}
		}
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - stop throttling a user";
	}
	
}
