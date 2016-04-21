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
        String fileNameRequested = message.substring(1, message.length());
        if (bot.getAvailableSoundFiles().get(fileNameRequested) == null) {
        	bot.sendMessageToChannel("No sound file to play with name `" + 
        			fileNameRequested + "` " + event.getAuthor().getAsMention() + ".",
        			event.getChannel());
        	LOG.info("No sound file found for given string " + fileNameRequested);
        } else {
        	LOG.info("Playing: " + fileNameRequested + " for " + 
            		event.getAuthor().getUsername() + " in " + 
            		event.getGuild().getName() + ".");
	        try {
	            bot.playFileForChatCommand(fileNameRequested, event);
	        } catch (Exception e) {
	            LOG.fatal("Could not play file " + fileNameRequested + ": " + e.toString());
	        }
        }
	}

}
