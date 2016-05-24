package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public abstract class SingleArgumentChatCommandProcessor extends AbstractChatCommandProcessor {

	private String arg;
	
	public SingleArgumentChatCommandProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}
	
	protected abstract void handleEvent(MessageReceivedEvent event, String message);
	
	public void process(MessageReceivedEvent event) {
		String message = event.getMessage().getContent().toLowerCase();
		if (!message.endsWith(getPrefix())) arg = message.substring(getPrefix().length() + 1);
		super.process(event);
		arg = null; // Clear argument.
	}
	
	public String getArgument() {
		return this.arg;
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " [argument]`"; 
	}
}
