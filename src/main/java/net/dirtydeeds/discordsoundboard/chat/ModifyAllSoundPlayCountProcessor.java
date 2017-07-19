package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ModifyAllSoundPlayCountProcessor extends
		OwnerSingleArgumentChatCommandProcessor {

	public ModifyAllSoundPlayCountProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Modify Sound Counts", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		if (getArgument() == null) {
			pm(event, "Need a new **play count**.");
		}
		long count = Long.valueOf(getArgument());
		for (String name : bot.getSoundMap().keySet()) {
			SoundFile sound = bot.getDispatcher().getSoundFileByName(name);
			if (sound != null) {
				sound.setNumberOfPlays(count);
				bot.getDispatcher().saveSound(sound);
			}
		}
		pm(event, "Set all sounds to play count: **" + count + "**.");
	}
	
	@Override
	public String getCommandHelpString() {
		return getPrefix() + " <count> (*) - modify all sound play counts";
	}

}
