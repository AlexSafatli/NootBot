package net.dirtydeeds.discordsoundboard.chat;

import java.io.File;
import java.util.List;
import java.text.DecimalFormat;

import net.dirtydeeds.discordsoundboard.Version;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class StatsProcessor extends AbstractChatCommandProcessor {

	private static final String[] UNITS = new String[] {
	  "B", "KB", "MB", "GB", "TB"
	};
	private static final String LIBRARY_TOO_BIG = "**TOO BIG**";

	public StatsProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "About Me", bot);
	}

	private SoundFile mostPlayedSound() {
		List<SoundFile> files = bot.getDispatcher().getSoundFilesOrderedByNumberOfPlays();
		int i = 0;
		while (files != null && !files.isEmpty() && i < files.size()) {
			SoundFile f = files.get(i++);
			if (bot.getSoundMap().get(f.getSoundFileId()) != null) {
				return f;
			}
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

	private long getFolderSize(File target) {
		long len = 0;
		File[] files = target.listFiles();
		for (int i = 0; i < files.length; ++i) {
			if (files[i].isFile()) len += files[i].length();
			else len += getFolderSize(files[i]);
		}
		return len;
	}

	private String sizeOfLibrary() {
		long size = getFolderSize(bot.getSoundsPath().toFile());
		int index = (int) (Math.log10(size) / 3);
		if (index >= UNITS.length) {
			return LIBRARY_TOO_BIG;
		}
		double val = 1 << (index * 10);
		return new DecimalFormat("#,##0.#").format(size / val) + " " + UNITS[index];
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		StyledEmbedMessage msg = buildStyledEmbedMessage(event);
		msg.addDescription("*Noot noot*.");
		int numberOfSounds = bot.getSoundMap().size();
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
			msg.addContent("Size of Sound Library", sizeOfLibrary(), true);
		}
		msg.addContent("Bot Uptime", bot.getUptimeAsString(), true);
		msg.addContent("Number of Servers", "" + bot.getGuilds().size(), true);
		msg.addContent("Developer", Version.getAuthor(bot), true);
		embed(event, msg);
	}

	@Override
	public String getCommandHelpString() {
		return getPrefix() + " - print some stats related to the bot";
	}
}