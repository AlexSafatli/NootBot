package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.beans.User;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class OptOutOfMentionsProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("OptOutProcessor");
	
	public OptOutOfMentionsProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		List<User> users = bot.getDispatcher().getUserById(event.getAuthor().getId());
		if (users != null && !users.isEmpty()) {
			User u = users.get(0);
			u.setOptedOutOfMentions(true);
			bot.getDispatcher().saveUser(u);
			LOG.info(u.getUsername() + " has opted out of mentions.");
		}
		pm(event, "You will no longer receive *@mentions* from me when I make periodic announcements.");
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` - opt out of *@mentions* from periodic announcements";
	}
	
}
