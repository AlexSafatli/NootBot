package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.Version;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class StatsProcessor extends AbstractChatCommandProcessor {

	public StatsProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "About Me", bot);
	}

	private SoundFile mostPlayedSound() {
		List<SoundFile> files = bot.getDispatcher().getSoundFilesOrderedByNumberOfPlays();
		int i = 0;
		while (files != null && !files.isEmpty() && i < files.size()) {
			SoundFile f = files.get(i++);
			if (bot.getSoundMap().get(f.getSoundFileId()) != null) return f;
		}
		return null;
	}

	private SoundFile longestSound() {
		List<SoundFile> files = bot.getDispatcher().getSoundFilesOrderedByDuration();
		int i = 0;
		while (files != null && !files.isEmpty() && i < files.size()) {
			SoundFile f = files.get(i++);
			if (bot.getSoundMap().get(f.getSoundFileId()) != null) {
				return f;
			}
		}
		return null;
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		StyledEmbedMessage msg = buildStyledEmbedMessage(event);
		msg.addDescription("*Noot noot*.");
		int numberOfSounds = bot.getSoundMap().size();
		int numberOfServers = bot.getGuilds().size();
		msg.addContent("Number of Sounds", "" + numberOfSounds, true);
		msg.addContent("Number of Categories", "" +
		               bot.getDispatcher().getNumberOfCategories(), true);
		if (numberOfSounds > 0) {
			SoundFile mostPlayed = mostPlayedSound(), longest = longestSound();
			msg.addContent("Most Played", "`" +  mostPlayed.getSoundFileId() +
			               "` with **" + mostPlayed.getNumberOfPlays() + "** plays",
			               true);
			msg.addContent("Longest Sound", "`" + longest.getSoundFileId() +
			               "` at **" + longest.getDuration() + "s**", true);
			msg.addContent("Size of Sound Library",
			               bot.getDispatcher().sizeOfLibrary(), true);
		}
		msg.addContent("Bot Uptime", bot.getUptimeAsString(), true);
		if (numberOfServers > 1) {
			msg.addContent("Number of Servers", "" + numberOfServers, true);
		}
		msg.addContent("Developer", Version.getAuthor(bot), false);
		embed(event, msg);
	}

	@Override
	public String getCommandHelpString() {
		return getPrefix() + " - print some stats related to the bot";
	}
}