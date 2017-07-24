package net.dirtydeeds.discordsoundboard.chat;

import java.util.regex.Pattern;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public abstract class AbstractFilterChatProcessor implements ChatCommandProcessor {

	private final Pattern regexp;
	private final String channelname;
	protected SoundboardBot bot;
	
	public AbstractFilterChatProcessor(Pattern regexp, String channelname, SoundboardBot bot) {
		this.regexp = regexp;
		this.channelname = channelname;
		this.bot = bot;
	}
	
	public void process(MessageReceivedEvent event) {
		if (!isApplicableCommand(event)) return;
		String message = event.getMessage().getContent().toLowerCase();
		try {
			copyToChannel(event, message);
			deleteOriginalMessage(event);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void copyToChannel(MessageReceivedEvent event, String message) {
		//TODO
	}

	protected void deleteOriginalMessage(MessageReceivedEvent event) {
		if (!event.isFromType(ChannelType.PRIVATE)) delete(event.getMessage());
	}

	protected abstract void handleEvent(MessageReceivedEvent event, String message);
	
	protected abstract boolean isApplicableCommand(String msg);
	
	public boolean isApplicableCommand(MessageReceivedEvent event) {
		return isApplicableCommand(event.getMessage().getContent());
	}
	
	public boolean canBeRunByAnyone() {
		return true;
	}
	
	public boolean canBeRunBy(User user, Guild guild) {
		return true;
	}
	
	private void delete(Message m) {
		if (bot.hasPermissionInChannel(m.getTextChannel(), Permission.MESSAGE_MANAGE))
			m.deleteMessage().queue();
	}
	
	protected String lookupString(String key) {
		String value = bot.getDispatcher().getStringService().lookup(key);
		return (value != null) ? value : "<String Not Found: " + key + ">";
	}
	
	protected String formatString(String key, Object... args) {
		return String.format(lookupString(key),args);
	}
	
	public String getCommandHelpString() {
		return ""; 
	}

}
