package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

public class PlayRandomProcessor extends AbstractChatCommandProcessor {

	public PlayRandomProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, soundPlayer);
	}

	protected void handleEvent(GuildMessageReceivedEvent event, String message) {
    	try {
    		String fileName = soundPlayer.playRandomFile(event);
    		soundPlayer.sendMessageToChannel("Attempted to play random sound file `" + fileName + "` " + event.getAuthor().getAsMention(), event.getChannel());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}

}
