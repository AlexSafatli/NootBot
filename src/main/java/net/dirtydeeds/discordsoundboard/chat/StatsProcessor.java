package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class StatsProcessor extends AbstractChatCommandProcessor {

	public StatsProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		StringBuilder sb = new StringBuilder();
		sb.append("There are currently ").append(bot.getAvailableSoundFiles().size());
		sb.append(" sound files. The bot has been running for " + bot.getUptime() + " minutes.");
		event.getChannel().sendMessage(sb.toString());
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` - prints a number of stats related to the bot";
	}

}