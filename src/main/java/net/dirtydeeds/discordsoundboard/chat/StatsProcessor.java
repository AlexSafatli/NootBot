package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class StatsProcessor extends AbstractChatCommandProcessor {

	private static final long MIN_MINUTES_TO_SHOW_AS_HOURS = 120;  // 2 hours
	private static final long MIN_MINUTES_TO_SHOW_AS_DAYS  = 2880; // 2 days
	
	public StatsProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		StringBuilder sb = new StringBuilder();
		sb.append("There are currently **").append(bot.getAvailableSoundFiles().size());
		sb.append("** sound files and **").append(bot.getSoundCategories().size()).append("** categories. ");
		sb.append("The bot has been running for **");
		long minutes = bot.getUptimeInMinutes();
		if (minutes >= MIN_MINUTES_TO_SHOW_AS_DAYS) {
			sb.append(minutes/(60*24) + " days");
		} else if (minutes >= MIN_MINUTES_TO_SHOW_AS_HOURS) {
			sb.append(minutes/60 + " hours");
		} else {
			sb.append(minutes + " minutes");
		}
		sb.append("**.");
		event.getChannel().sendMessage(sb.toString());
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` - prints some stats related to the bot";
	}

}