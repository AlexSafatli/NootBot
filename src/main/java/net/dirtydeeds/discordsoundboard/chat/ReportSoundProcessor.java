package net.dirtydeeds.discordsoundboard.chat;

import java.io.IOException;
import java.util.Set;

import com.google.common.io.Files;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ReportSoundProcessor extends SingleArgumentChatCommandProcessor {
	
	public static final SimpleLog LOG = SimpleLog.getLog("ReportSoundProcessor");
	private static final int NUMBER_OF_REPORTS_FOR_EXCLUDE = 3;
	private static final int NUMBER_OF_REPORTS_FOR_DELETE  = 8;
	
	public ReportSoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Report a Sound", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = event.getAuthor();
		String name = getArgument();
		LOG.info("Received report request for " + name + " from " + user.getName());
		Set<String> soundNames = bot.getSoundMap().keySet();
		if (name == null) {
			pm(event, formatString(Strings.NEED_NAME, getPrefix() + " " + StringUtils.randomString(soundNames)));
		} else if (!soundNames.contains(name)) {
			String suggestion = "Check your spelling.", possibleName = bot.getClosestMatchingSoundName(name);
			if (possibleName != null) {
				suggestion = "Did you mean `" + possibleName + "`?";
			}
        	m(event, formatString(Strings.NOT_FOUND, name) + " *" + suggestion + "* " + user.getAsMention());
		} else {
			// See if already reported it.
			if (bot.getUser(user).getSoundsReported().contains(name)) {
				LOG.info("User already reported on sound.");
				pm(event, "You already reported this sound."); return;
			} else {
				LOG.info("Adding " + name + " to sounds that the user " + user.getName() + " reported on.");
				net.dirtydeeds.discordsoundboard.beans.User u = bot.getUser(user);
				u.addSoundReported(name);
				bot.getDispatcher().saveUser(u);
			}
			// Continue with report.
			SoundFile file = bot.getDispatcher().getSoundFileByName(name);
			file.addOneToNumberOfReports();
			bot.getDispatcher().saveSound(file);
			pm(event, StyledEmbedMessage.forSoundFile(file, "Information for Sound `" + file.getSoundFileId() + "`", 
					"You have successfully reported the sound called `" + file.getSoundFileId() + "`."));
			if (file.getNumberOfReports() >= NUMBER_OF_REPORTS_FOR_DELETE && delete(file)) {
				pm(event, "Your report was enough to have the sound get deleted. You won't have to deal with `" + name + "` anymore.");
				bot.sendMessageToUser("Deleted `" + name + "` because of too many reports but made a copy in temporary directory.", bot.getOwner());
				LOG.info("Deleted file because of number of reports.");
			} else if (file.getNumberOfReports() >= NUMBER_OF_REPORTS_FOR_EXCLUDE && !file.isExcludedFromRandom()) {
				file.setExcludedFromRandom(true);
				bot.getDispatcher().saveSound(file);
				LOG.info("Made file excuded from random because of number of reports.");
			}
			LOG.info("Sound now has " + file.getNumberOfReports() + " reports.");
		}
	}

	private boolean delete(SoundFile file) {
		try {
			Files.copy(file.getSoundFile(), bot.getTempPath().resolve(file.getSoundFile().getName()).toFile());
		} catch (IOException io) {
			LOG.fatal("Failed to copy file to " + bot.getTempPath());
		}
		return file.getSoundFile().delete();
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " <soundfile>` - report a sound file for being inappropriate or annoying";
	}
	
}
