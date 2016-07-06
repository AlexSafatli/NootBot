package net.dirtydeeds.discordsoundboard.chat;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.io.Files;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class RenameSoundProcessor extends AuthenticatedMultiArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("RenameSoundProcessor");
	
	public RenameSoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		int numArgs = getArguments().length;
		if (numArgs != 2) {
			pm(event, "You need to provide an old and new name. For example: `" + getPrefix() + " holdthedoor, door`.");
			return;
		}
		String oldName = getArguments()[0], newName = getArguments()[1];
		if (bot.getSoundMap().get(oldName) == null) {
			pm(event, "The sound file `" + oldName + "` does not exist.");
			return;
		}
		try {
			File oldFile = bot.getSoundMap().get(oldName).getSoundFile();
			Path source = Paths.get(oldFile.getPath());
			LOG.info("Identified path of file: " + source);
			int extIndex = oldFile.getName().lastIndexOf(".");
			String ext = (extIndex != -1) ? oldFile.getName().substring(extIndex) : "";
			LOG.info("Identified extension of file: " + ext);
			File newFile = source.resolveSibling(newName + ext).toFile();
			LOG.info("Moving file to: " + newFile.getPath());
			Files.move(oldFile, newFile);
			pm(event, "File renamed from `" + oldName + "` to `" + newName + "`.");
		} catch (Exception e) {
			e.printStackTrace();
			LOG.fatal("While renaming a file: " + e.toString() + " => " + e.getMessage());
			pm(event, "While renaming this file, I ran into a problem.");
		}
		bot.getDispatcher().updateFileList();
	}

	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - rename a sound";
	}
	
}
