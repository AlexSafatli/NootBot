package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
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
		MessageBuilder mb = new MessageBuilder();
		mb.append("Type **any of these commands** in a channel or a PM to me:\n\n");
		for (ChatCommandProcessor processor : processors) {
			if (processor.canBeRunByAnyone() || (!processor.canBeRunByAnyone() && isOwner)) {
				// Print command help for this processor.
				String cmdHelp = processor.getCommandHelpString();
				mb.append(cmdHelp + "\n");
			}
		}
		if (isOwner) {
			mb.append("\nAn `*` symbol indicates a command only available to the bot owner (**" + 
					bot.getOwner() + "**).");
		}
		// Send all buffered data.
		for (String s : mb) {
			event.getChannel().sendMessageAsync(s, null);
		}
		LOG.info("Responded to help command from " + event.getAuthor().getUsername());
	}
	
	@Override
	public String getCommandHelpString() {
		LOG.warn("This processor was queried for a command help string but has none.");
		return null;
	} 

}
