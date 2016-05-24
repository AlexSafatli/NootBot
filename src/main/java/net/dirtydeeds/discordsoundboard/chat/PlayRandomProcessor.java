package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class PlayRandomProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("RandomSoundProcessor");
	
	public PlayRandomProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, soundPlayer);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String category = getArgument();
		if (!bot.isAllowedToPlaySound(event.getAuthor())) {
        	pm(event, "You have been explicitly disallowed to play sounds using this bot by its owner.");
        	LOG.info(event.getAuthor() + " tried to play a sound file but is not allowed.");
        	return;
		}
    	try {
    		if (category != null) {
    			String played = bot.playRandomFileForCategory(event.getAuthor(), category);
    			if (played == null) {
    				event.getChannel().sendMessageAsync("No category *" + category + "* was found to play a random file from " + 
    						event.getAuthor().getAsMention(), null);
    			} else {
    				event.getChannel().sendMessageAsync("Played random sound file `" +  played + "` from **" + category + "**.", null);
    			}
    		} else {
    			String fileName = bot.playRandomFile(event.getAuthor());
    			event.getChannel().sendMessageAsync("Played random sound file `" + fileName + "`.", null);
    		}
    	} catch (Exception e) {
    		LOG.warn("Unable to play a random sound file because: " + e.toString());
    	}
	}
	
	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - plays a random file from all files or from a category";
	}

}
