package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class ServerMessageProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("ServerMessage");
	
	public ServerMessageProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String msgToSend = (getArgument() != null) ? event.getMessage().getContent().substring(getPrefix().length() + 1) : "";
		if (!msgToSend.isEmpty()) {
			LOG.info("Bot " + bot.getBotName() + ": \"" + msgToSend + "\".");
			pm(event, "Sending this message: `" + msgToSend + "`.");
			bot.sendMessageToAllGuilds("**FYI** " + msgToSend + " " + event.getAuthor().getAsMention());
		} else pm(event, "You need to provide a message!");
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " <message>` (`*`) - send a message to all "
				+ "servers this bot is in";
	}

}
