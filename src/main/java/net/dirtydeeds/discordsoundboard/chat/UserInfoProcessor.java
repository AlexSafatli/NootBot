package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class UserInfoProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

	public UserInfoProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = (getArgument() != null) ? bot.getUserByName(getArgument()) : event.getAuthor();
		if (user == null && getArgument() != null) {
			pm(event, "Did not find a user with username `" + getArgument() + "`. *Can I see him/her?*");
			return;
		}
		pm(event, String.format("**Username**: %s / **Entrance**: `%s` / **Can Play Sounds**: %b / **Throttled**: %b",
				user.getUsername(), bot.getEntranceForUser(user), bot.isAllowedToPlaySound(user), bot.isThrottled(user)));
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " [username]` (`*`) - get info about a user (or self)";
	}
	
}
