package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
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
			if (username.equals(event.getAuthor().getUsername())) {
				pm(event, lookupString(Strings.NOT_TO_SELF));
			} else if (bot.throttleUser(username)) {
				pm(event, formatString(Strings.USER_THROTTLE_UNTHROTTLED, username));
				LOG.info("Throttled username " + username);
			} else {
				pm(event, formatString(Strings.USER_NOT_FOUND_UNTHROTTLED, username));
				LOG.info("Failed to throttle username " + username);
			}
		}
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - throttle a user from using bot every **5** minutes";
	}
	
}
