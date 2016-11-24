package net.dirtydeeds.discordsoundboard.chat;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.io.Files;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class RecategorizeSoundProcessor extends AuthenticatedMultiArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("RecategorizeSoundProcessor");
	
	public RecategorizeSoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		int numArgs = getArguments().length;
		if (numArgs != 2) {
			pm(event, "You need to provide a sound name and category to move it to. For example: `" + getPrefix() + " holdthedoor, Games`.");
			return;
		}
		String name = getArguments()[0], cat = getArgumentsCased(event)[1];
		if (bot.getSoundMap().get(name) == null) {
			pm(event, lookupString(Strings.SOUND_NOT_FOUND));
			return;
		} else if (!bot.getSoundCategories().contains(cat)) {
			boolean notFound = true;
			for (String _cat : bot.getSoundCategories()) {
				if (cat.equalsIgnoreCase(_cat)) {
					cat = _cat; notFound = false; break;
				}
			}
			if (notFound) {
				pm(event, "The category `" + cat + "` does not exist. *Will make it!*");
				Path newCat = bot.getSoundsPath().resolve(cat);
				LOG.info("Creating directory: " + newCat);
				newCat.toFile().mkdir();
			}
		}
		try {
			File file = bot.getSoundMap().get(name).getSoundFile();
			Path source = Paths.get(file.getPath());
			LOG.info("Identified path of file: " + source);
			int extIndex = file.getName().lastIndexOf(".");
			String ext = (extIndex != -1) ? file.getName().substring(extIndex) : "";
			LOG.info("Identified extension of file: " + ext);
			File destination = bot.getSoundsPath().resolve(cat).resolve(name + ext).toFile();
			LOG.info("Moving file to: " + destination.getPath());
			Files.move(file, destination);
			pm(event, formatString(Strings.SOUND_MOVE_SUCCESS, file.getName(), bot.getSoundMap().get(name).getCategory(), cat));
		} catch (Exception e) {
			e.printStackTrace();
			LOG.fatal("While renaming a file: " + e.toString() + " => " + e.getMessage());
			pm(event, formatString(Strings.SOUND_MOVE_FAILURE, name));
		}
		bot.getDispatcher().updateFileList();
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - change the category for a sound";
	}
	
}
