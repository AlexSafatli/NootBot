package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class RestartBotProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

	public RestartBotProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	@Override
	public void process(MessageReceivedEvent event) {
		if (!event.isPrivate() && bot.hasPermissionInChannel(event.getTextChannel(), Permission.MESSAGE_MANAGE))
			event.getMessage().deleteMessage();
		handleEvent(event, null);
	}
	
	protected void handleEvent(MessageReceivedEvent event, String message) {
		pm(event, "Restarting this bot instance.");
		bot.getDispatcher().restartBot(bot);
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` (`*`) - restart this bot";
	}
	
}
