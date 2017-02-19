package net.dirtydeeds.discordsoundboard.chat;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

public interface ChatCommandProcessor {

	public abstract void process(MessageReceivedEvent event);
	public abstract boolean isApplicableCommand(MessageReceivedEvent event);
	public abstract boolean canBeRunByAnyone();
	public abstract boolean canBeRunBy(User user, Guild guild);
	public abstract String getTitle();
	public abstract String getCommandHelpString();
	
}