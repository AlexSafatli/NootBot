package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

public abstract class AbstractChatCommandProcessor implements ChatCommandProcessor {

	private final String prefix;
	protected SoundboardBot soundPlayer;
	
	public AbstractChatCommandProcessor(String prefix, SoundboardBot soundPlayer) {
		this.prefix = prefix;
		this.soundPlayer = soundPlayer;
	}
	
	protected abstract void handleEvent(GuildMessageReceivedEvent event, String message);
	
	public void process(GuildMessageReceivedEvent event) {
		if (!isApplicableCommand(event)) return;
		handleEvent(event, event.getMessage().getContent().toLowerCase());
		if (soundPlayer.hasPermissionInChannel(event.getChannel(), Permission.MESSAGE_MANAGE))
			event.getMessage().deleteMessage();
	}
	
	public boolean isApplicableCommand(String cmd) {
		return (cmd.toLowerCase().startsWith(prefix) && cmd.length() > 1);
	}
	
	public boolean isApplicableCommand(GuildMessageReceivedEvent event) {
		return isApplicableCommand(event.getMessage().getContent());
	}
	
	public String getPrefix() {
		return prefix;
	}
	
}
