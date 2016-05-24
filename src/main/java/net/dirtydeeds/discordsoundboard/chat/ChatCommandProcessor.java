package net.dirtydeeds.discordsoundboard.chat;

import net.dv8tion.jda.events.message.MessageReceivedEvent;

public interface ChatCommandProcessor {

	public abstract void process(MessageReceivedEvent event);
	public abstract boolean isApplicableCommand(MessageReceivedEvent event);
	public abstract boolean canBeRunByAnyone();
	public abstract String getCommandHelpString();
	
}