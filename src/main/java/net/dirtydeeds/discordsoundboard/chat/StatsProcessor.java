package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.Version;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class StatsProcessor extends AbstractChatCommandProcessor {

	private static final long MIN_MINUTES_TO_SHOW_AS_HOURS = 120;  // 2 hours
	private static final long MIN_MINUTES_TO_SHOW_AS_DAYS  = 2880; // 2 days
	
	public StatsProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	private String mostPlayedSound() {
		List<SoundFile> files = bot.getDispatcher().getSoundFilesOrderedByNumberOfPlays();
		int i = 0;
		while (files != null && !files.isEmpty()) {
			SoundFile f = files.get(i);
			if (bot.getSoundMap().get(f.getSoundFileId()) != null) {
				return f.getSoundFileId();
			}
			++i;
		}
		return null;
	}
	
	protected void handleEvent(MessageReceivedEvent event, String message) {
		int numberOfSounds = bot.getSoundMap().size();
		StringBuilder sb = new StringBuilder();
		sb.append("There are currently **").append(numberOfSounds);
		sb.append("** sound files and **").append(bot.getSoundCategories().size()).append("** categories.\n");
		if (numberOfSounds > 0) sb.append("The most played sound is `" + mostPlayedSound() + "`.\n");
		sb.append("The bot has been running for **");
		long minutes = bot.getUptimeInMinutes();
		if (minutes >= MIN_MINUTES_TO_SHOW_AS_DAYS) {
			sb.append(minutes/(60*24) + " days");
		} else if (minutes >= MIN_MINUTES_TO_SHOW_AS_HOURS) {
			sb.append(minutes/60 + " hours");
		} else {
			sb.append(minutes + " minutes");
		}
		sb.append("** using version **").append(Version.VERSION).append("** of *" + Version.NAME + "*.\n");
		sb.append("This is a hobby project by **").append(Version.AUTHOR).append("**.");
		event.getChannel().sendMessage(sb.toString());
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` - print some stats related to the bot";
	}

}