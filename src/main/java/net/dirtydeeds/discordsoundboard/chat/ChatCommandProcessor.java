package net.dirtydeeds.discordsoundboard.chat;

import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

public interface ChatCommandProcessor {

	public abstract void process(GuildMessageReceivedEvent event);
	public abstract boolean isApplicableCommand(GuildMessageReceivedEvent event);
	
}