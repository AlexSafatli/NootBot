package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundPlayerImpl;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

public abstract class ChatSoundBoardProcessor {

	private final String prefix;
	protected SoundPlayerImpl soundPlayer;
	
	public ChatSoundBoardProcessor(String prefix, SoundPlayerImpl soundPlayer) {
		this.prefix = prefix;
		this.soundPlayer = soundPlayer;
	}
	
	protected abstract void handleEvent(GuildMessageReceivedEvent event, String message);
	
	public void process(GuildMessageReceivedEvent event) {
		if (!isApplicableCommand(event)) return;
		handleEvent(event, event.getMessage().getContent().toLowerCase());
		event.getMessage().deleteMessage();
	}
	
	public boolean isApplicableCommand(String cmd) {
		String cmp = cmd.toLowerCase();
		return (cmp.startsWith(prefix) && cmp.length() > 1);
	}
	
	public boolean isApplicableCommand(GuildMessageReceivedEvent event) {
		return isApplicableCommand(event.getMessage().getContent());
	}
	
	public String getPrefix() {
		return prefix;
	}
	
}
