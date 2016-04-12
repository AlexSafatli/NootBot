package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundPlayerImpl;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class PlaySoundProcessor extends ChatSoundBoardProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("PlaySoundProcessor");
	
	public PlaySoundProcessor(String prefix, SoundPlayerImpl soundPlayer) {
		super(prefix, soundPlayer);
	}

	protected void handleEvent(GuildMessageReceivedEvent event, String message) {
        try {
            String fileNameRequested = message.substring(1, message.length());
            LOG.info("Attempting to play file: " + fileNameRequested + ".");
            soundPlayer.playFileForChatCommand(fileNameRequested, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
