package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.Version;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class StatsProcessor extends AbstractChatCommandProcessor {

	private static final long MIN_MINUTES_TO_SHOW_AS_HOURS = 120;  // 2 hours
	private static final long MIN_MINUTES_TO_SHOW_AS_DAYS  = 2880; // 2 days
	
	public StatsProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "About Me", bot);
	}

	private SoundFile mostPlayedSound() {
		List<SoundFile> files = bot.getDispatcher().getSoundFilesOrderedByNumberOfPlays();
		int i = 0;
		while (files != null && !files.isEmpty() && i < files.size()) {
			SoundFile f = files.get(i);
			if (bot.getSoundMap().get(f.getSoundFileId()) != null) {
				return f;
			}
			++i;
		}
		return null;
	}
	
	private SoundFile longestSound() {
		List<SoundFile> files = bot.getDispatcher().getSoundFilesOrderedByDuration();
		int i = 0;
		while (files != null && !files.isEmpty() && i < files.size()) {
			SoundFile f = files.get(i);
			if (bot.getSoundMap().get(f.getSoundFileId()) != null) {
				return f;
			}
			++i;
		}
		return null;
	}
	
	protected void handleEvent(MessageReceivedEvent event, String message) {
		StyledEmbedMessage msg = buildStyledEmbedMessage();
		msg.addDescription("*Noot noot*.");
		int numberOfSounds = bot.getSoundMap().size();
		msg.addContent("Number of Sounds", "" + numberOfSounds, true);
		msg.addContent("Number of Categories", "" + bot.getDispatcher().getNumberOfCategories(), true);
		if (numberOfSounds > 0) {
			SoundFile mostPlayed = mostPlayedSound(), longest = longestSound();
			msg.addContent("Most Played", "`" +  mostPlayed.getSoundFileId() + "` with **" + mostPlayed.getNumberOfPlays() + "** plays", true);
			msg.addContent("Longest Sound", "`" + longest.getSoundFileId() + "` (" + longest.getDuration() + "s)", true);
		}
		String uptime = null;
		long minutes = bot.getUptimeInMinutes();
		if (minutes >= MIN_MINUTES_TO_SHOW_AS_DAYS) {
			uptime = (minutes/(60*24) + " days");
		} else if (minutes >= MIN_MINUTES_TO_SHOW_AS_HOURS) {
			uptime = (minutes/60 + " hours");
		} else {
			uptime = (minutes + " minutes");
		}
		msg.addContent("Bot Uptime", uptime, true);
		msg.addContent("Number of Servers", "" + bot.getGuilds().size(), true);
		msg.addContent("Developer", Version.getAuthor(bot), true);
		embed(event, msg);
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` - print some stats related to the bot";
	}

}