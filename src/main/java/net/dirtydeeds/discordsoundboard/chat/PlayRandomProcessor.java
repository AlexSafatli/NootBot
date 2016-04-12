package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundPlayerImpl;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

public class PlayRandomProcessor extends ChatSoundBoardProcessor {

	public PlayRandomProcessor(String prefix, SoundPlayerImpl soundPlayer) {
		super(prefix, soundPlayer);
	}

	protected void handleEvent(GuildMessageReceivedEvent event, String message) {
    	try {
    		String fileName = soundPlayer.playRandomFile(event);
    		event.getChannel().sendMessage("Played random sound file `" + fileName + "`.");
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}

}
