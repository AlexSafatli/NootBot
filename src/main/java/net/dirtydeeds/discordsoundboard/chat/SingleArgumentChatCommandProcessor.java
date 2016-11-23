package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public abstract class SingleArgumentChatCommandProcessor extends AbstractChatCommandProcessor {

	private String arg;
	private String msg;
	
	public SingleArgumentChatCommandProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}
	
	protected abstract void handleEvent(MessageReceivedEvent event, String message);
	
	public void process(MessageReceivedEvent event) {
		String message = event.getMessage().getContent().toLowerCase();
		if (!message.endsWith(getPrefix())) arg = message.substring(getPrefix().length() + 1);
		super.process(event);
		msg = message;
		arg = null; // Clear argument.
	}
	
	public String getArgument() {
		return this.arg;
	}
	
	public String getArgumentCased() {
		return (msg != null && !msg.endsWith(getPrefix())) ? msg.substring(getPrefix().length() + 1) : null;
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " [argument]`"; 
	}
}
