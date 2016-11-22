package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public abstract class OwnerMultiArgumentChatCommandProcessor extends
		MultiArgumentChatCommandProcessor {

	public OwnerMultiArgumentChatCommandProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}
	
	@Override
	public boolean isApplicableCommand(MessageReceivedEvent event) {
		if (super.isApplicableCommand(event)) {
			if (canBeRunBy(event.getAuthor(), event.getGuild())) return true;
			else {
				pm(event, "This command is only intended for the bot owner! The owner has been notified of your action.");
				bot.sendMessageToUser("User **" + event.getAuthor().getUsername() + "** just ran `" + 
						event.getMessage().getContent() + "` without permission.", bot.getOwner());
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
	public boolean canBeRunBy(User user, Guild guild) {
		return bot.getOwner().equals(user.getUsername());
	}
	
	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " (`*`)"; 
	}
	
}
