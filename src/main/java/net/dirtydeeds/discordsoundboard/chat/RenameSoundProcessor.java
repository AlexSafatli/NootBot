package net.dirtydeeds.discordsoundboard.chat;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.io.Files;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class RenameSoundProcessor extends AuthenticatedMultiArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("RenameSoundProcessor");

	public RenameSoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Rename Sound", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		int numArgs = getArguments().length;
		if (numArgs != 2) {
			pm(event, "You need to provide an old and new name. For example: `" + getPrefix() + " holdthedoor, door`.");
			return;
		}
		String oldName = getArguments()[0], newName = getArguments()[1];
		if (bot.getSoundMap().get(oldName) == null) {
			pm(event, lookupString(Strings.SOUND_NOT_FOUND));
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
			pm(event, formatString(Strings.SOUND_RENAME_SUCCESS, oldName, newName));
		} catch (Exception e) {
			e.printStackTrace();
			LOG.fatal("While renaming a file: " + e.toString() + " => " + e.getMessage());
			pm(event, formatString(Strings.SOUND_RENAME_FAILURE, oldName, newName));
		}
		bot.getDispatcher().updateFileList();
	}

	@Override
	public String getCommandHelpString() {
		return getPrefix() + " <soundfile>, <newname> - rename a sound";
	}

}
