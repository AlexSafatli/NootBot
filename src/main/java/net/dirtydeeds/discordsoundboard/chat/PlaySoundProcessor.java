package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class PlaySoundProcessor extends AbstractChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("PlaySoundProcessor");
	
	public PlaySoundProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, soundPlayer);
	}

	protected void handleEvent(GuildMessageReceivedEvent event, String message) {
        try {
            String fileNameRequested = message.substring(1, message.length());
            LOG.info("Attempting to play file: " + fileNameRequested + " for " + 
            		event.getAuthor().getUsername() + " in " + event.getChannel().getName() + 
            		"#" + event.getGuild().getName() + ".");
            soundPlayer.playFileForChatCommand(fileNameRequested, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
