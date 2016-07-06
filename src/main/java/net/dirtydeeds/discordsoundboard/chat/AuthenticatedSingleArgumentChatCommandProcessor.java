package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public abstract class AuthenticatedSingleArgumentChatCommandProcessor extends
		SingleArgumentChatCommandProcessor {

	public AuthenticatedSingleArgumentChatCommandProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}
	
	@Override
	public boolean isApplicableCommand(MessageReceivedEvent event) {
		if (super.isApplicableCommand(event)) {
			if (bot.getOwner().equals(event.getAuthor().getUsername())) return true;
			else {
				pm(event, "This message is only intended for bot owners! The bot owner has been notified of your action.");
				bot.sendMessageToUser("User **" + event.getAuthor().getUsername() + "** just ran `" + event.getMessage().getContent() + "` without permission.", bot.getOwner());
				return false;
			}
		}
		else return false;
	}
	
	protected abstract void handleEvent(MessageReceivedEvent event, String message);

	@Override
	public boolean canBeRunByAnyone() {
		return false;
	}
	
	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " (`*`)"; 
	}
	
}
