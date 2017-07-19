package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class RestartBotProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

	public RestartBotProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Restart Bot", bot);
	}
	
	protected void handleEvent(MessageReceivedEvent event, String message) {
		deleteOriginalMessage(event);
		pm(event, "Restarting this bot instance. *This is a soft restart. This might break things. Hard restart if it does!*");
		bot.getDispatcher().restartBot(bot);
		bot.getDispatcher().updateFileList();
	}

	@Override
	public String getCommandHelpString() {
		return getPrefix() + " (*) - restart this bot";
	}
	
}
