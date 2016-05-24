package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public abstract class AbstractChatCommandProcessor implements ChatCommandProcessor {

	private final String prefix;
	protected SoundboardBot bot;
	
	public AbstractChatCommandProcessor(String prefix, SoundboardBot bot) {
		this.prefix = prefix;
		this.bot = bot;
	}
	
	public void process(MessageReceivedEvent event) {
		if (!isApplicableCommand(event)) return;
		String message = event.getMessage().getContent().toLowerCase();
		handleEvent(event, message);
		if (!event.isPrivate() && bot.hasPermissionInChannel(event.getTextChannel(), Permission.MESSAGE_MANAGE))
			event.getMessage().deleteMessage();
	}

	protected abstract void handleEvent(MessageReceivedEvent event, String message);
	
	private boolean isApplicableCommand(String cmd) {
		return (cmd.toLowerCase().startsWith(prefix) && cmd.length() > 1);
	}
	
	public boolean isApplicableCommand(MessageReceivedEvent event) {
		return isApplicableCommand(event.getMessage().getContent());
	}
	
	public boolean canBeRunByAnyone() {
		return true;
	}
	
	public String getPrefix() {
		return this.prefix;
	}
	
	public void pm(MessageReceivedEvent event, String message) {
		bot.sendMessageToUser(message, event.getAuthor());
	}
	
	public String getCommandHelpString() {
		return "`" + getPrefix() + "`"; 
	}

}
