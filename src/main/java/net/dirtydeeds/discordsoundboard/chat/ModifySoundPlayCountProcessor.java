package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class ModifySoundPlayCountProcessor extends
		OwnerMultiArgumentChatCommandProcessor {

	public ModifySoundPlayCountProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		if (getArguments() == null || getArguments().length != 2) {
			pm(event, "Need a **sound name** and a new **play count**.");
		}
		String name = getArguments()[0];
		long count = Long.valueOf(getArguments()[1]);
		SoundFile sound = bot.getDispatcher().getSoundFileByName(name);
		if (sound != null) {
			sound.setNumberOfPlays(count);
			bot.getDispatcher().saveSound(sound);
			pm(event, "Set `" + name + "` to play count: **" + count + "**.");
		}
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " <soundfile>, <count>` (`*`) - modify a sound play count";
	}

}
