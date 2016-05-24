package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public abstract class MultiArgumentChatCommandProcessor extends AbstractChatCommandProcessor {

	private String[] args = {};
	
	public MultiArgumentChatCommandProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}
	
	protected abstract void handleEvent(MessageReceivedEvent event, String message);
	
	public void process(MessageReceivedEvent event) {
		String message = event.getMessage().getContent().toLowerCase();
		if (!message.endsWith(getPrefix())) {
			// Get arguments. Comma-delimited.
			String noPrefix = message.substring(getPrefix().length() + 1);
			args = noPrefix.split(",\\s?");
		}
		super.process(event);
		args = new String[0]; // Clear arguments.
	}
	
	public String[] getArguments() {
		return this.args;
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " [argument1],[argument2],...`"; 
	}
	
}
