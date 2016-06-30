package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class HelpProcessor extends AbstractChatCommandProcessor {
	
	public static final SimpleLog LOG = SimpleLog.getLog("HelpProcessor");
	
	List<ChatCommandProcessor> processors;
	
	public HelpProcessor(String prefix, SoundboardBot bot, List<ChatCommandProcessor> processors) {
		super(prefix, bot);
		this.processors = processors;
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		if (processors == null) return;
		boolean isOwner = (event.getAuthor().getUsername().equalsIgnoreCase(bot.getOwner()));
		StringBuilder sb = new StringBuilder();
		sb.append("Type **any of the following commands** in this channel, another one, or in a PM to me:\n\n");
		for (ChatCommandProcessor processor : processors) {
			if (processor.canBeRunByAnyone() || (!processor.canBeRunByAnyone() && isOwner))
				sb.append(processor.getCommandHelpString()).append("\n");
		}
		if (isOwner) {
			sb.append("\nAn `*` symbol indicates a command only available to the bot owner (**");
			sb.append(bot.getOwner()).append("**).");
		}
		event.getChannel().sendMessageAsync(sb.toString(), null);
		LOG.info("Responded to help command from " + event.getAuthor().getUsername() + 
				" ** isOwner: " + isOwner);
	}
	
	@Override
	public String getCommandHelpString() {
		LOG.warn("This processor was queried for a command help string but has none.");
		return null;
	} 

}
