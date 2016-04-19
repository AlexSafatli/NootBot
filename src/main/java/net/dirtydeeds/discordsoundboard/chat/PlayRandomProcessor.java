package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

public class PlayRandomProcessor extends AbstractChatCommandProcessor {

	public PlayRandomProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, soundPlayer);
	}

	protected void handleEvent(GuildMessageReceivedEvent event, String message) {
		String category = null;
		if (!message.endsWith(getPrefix())) {
			// Get argument.
			category = message.substring(getPrefix().length() + 1);
		}
    	try {
    		if (category != null) {
    			SoundFile played;
    			played = bot.playRandomFileForCategory(event.getAuthor().getUsername(), category, event.getGuild());
    			if (played == null) {
    				bot.sendMessageToChannel("No category *" + category + "* was found to play a random file from.", event.getChannel());
    			} else {
        			bot.sendMessageToChannel("Attempted to play random sound file from category **" + played.getCategory() + "** " + event.getAuthor().getAsMention(), event.getChannel());
    			}
    		} else {
    			String fileName = bot.playRandomFile(event);
    			bot.sendMessageToChannel("Attempted to play random sound file `" + fileName + "` " + event.getAuthor().getAsMention(), event.getChannel());
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}

}
