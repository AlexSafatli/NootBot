package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class StopSoundProcessor extends SingleArgumentChatCommandProcessor {

	public StopSoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Stop Bot", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		if (event.getGuild() == null) {
			pm(event, "You need to do this in a server.");
			return;
		}
		bot.stopPlayingSound(event.getGuild());
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` (`*`) - stop the bot from playing its current sound and any queued sounds";
	}
	
}
