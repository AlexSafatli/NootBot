package net.dirtydeeds.discordsoundboard.chat;

import java.io.IOException;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class DownloadSoundProcessor extends SingleArgumentChatCommandProcessor {

	public DownloadSoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Download Sound", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String filename = getArgument();
		if (filename != null) {
			SoundFile file = bot.getSoundMap().get(filename);
			if (file == null) {
				pm(event, lookupString(Strings.SOUND_NOT_FOUND));
			} else {
				event.getAuthor().openPrivateChannel().queue(Channel c-> {
					c.sendFile(file.getSoundFile(), null).queue();
				});
			}
		}
	}

	@Override
	public String getCommandHelpString() {
		return getPrefix() + " <soundfile> - download a sound by name";
	}
}