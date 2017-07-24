package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ServerMessageProcessor extends OwnerSingleArgumentChatCommandProcessor {

	public ServerMessageProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Server Message", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String msgToSend = (getArgument() != null) ? event.getMessage().getContent().substring(getPrefix().length() + 1) : "";
		if (!msgToSend.isEmpty()) {
			bot.sendMessageToAllGuilds("**Yo!** " + msgToSend + " \u2014 sent by " + event.getAuthor().getAsMention());
		} else pm(event, "You need to provide a message!");
	}

	@Override
	public String getCommandHelpString() {
		return getPrefix() + " <message> (*) - send a message to all "
		       + "servers this bot is in";
	}

}
