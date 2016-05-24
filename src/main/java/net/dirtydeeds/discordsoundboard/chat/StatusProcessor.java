package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class StatusProcessor extends SingleArgumentChatCommandProcessor {

	public StatusProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = (getArgument() != null) ? bot.getUserByName(getArgument()) : event.getAuthor();
		if (user == null && getArgument() != null) {
			pm(event, "No user with username `" + getArgument() + "`.");
			return;
		}
		pm(event, String.format("**Username**: %s / **Entrance**: `%s` / **Can Play Sounds**: %b / **Is Throttled**: %b",
				user.getUsername(), bot.getEntranceForUser(user), bot.isAllowedToPlaySound(user), bot.isThrottled(user)));
		if (user.getUsername().equalsIgnoreCase(bot.getOwner())) {
			pm(event, "This user is the bot's owner.");
		}
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` [username] - privately tell you what this bot knows about you (or about someone else if specified)";
	}
	
}
