package net.dirtydeeds.discordsoundboard.chat;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.io.Files;

import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class RecategorizeSoundProcessor extends AuthenticatedMultiArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("RecategorizeSoundProcessor");
	
	public RecategorizeSoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Recategorize", bot);
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
		} else if (!bot.isASoundCategory(cat)) {
			pm(event, "The category `" + cat + "` does not exist. *Will make it as a new primary category!*");
			Path newCat = bot.getSoundsPath().resolve(cat);
			LOG.info("Creating directory: " + newCat);
			newCat.toFile().mkdir();
			bot.getDispatcher().updateFileList();
		}
		try {
			boolean success = false;
			File file = bot.getSoundMap().get(name).getSoundFile();
			Path source = Paths.get(file.getPath());
			LOG.info("Identified path of file: " + source);
			int extIndex = file.getName().lastIndexOf(".");
			String ext = (extIndex != -1) ? file.getName().substring(extIndex) : "";
			LOG.info("Identified extension of file: " + ext);
			for (Category category : bot.getDispatcher().getCategories()) {
				if (category.getName().equalsIgnoreCase(cat)) {
					File destination = category.getFolderPath().resolve(name + ext).toFile();
					LOG.info("Moving file to: " + destination.getPath());
					Files.move(file, destination);
					success = true;
					pm(event, formatString(Strings.SOUND_MOVE_SUCCESS, file.getName(), bot.getSoundMap().get(name).getCategory(), cat));
				}
			}
			if (!success) pm(event, formatString(Strings.SOUND_MOVE_FAILURE, name));
		} catch (Exception e) {
			e.printStackTrace();
			LOG.fatal("While renaming a file: " + e.toString() + " => " + e.getMessage());
			pm(event, formatString(Strings.SOUND_MOVE_FAILURE, name));
		}
		bot.getDispatcher().updateFileList();
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " \u2014 change the category for a sound";
	}
	
}
