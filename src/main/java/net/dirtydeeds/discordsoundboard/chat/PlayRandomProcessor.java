package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class PlayRandomProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("RandomSoundProcessor");
	
	public PlayRandomProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, "Random Sound", soundPlayer);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String category = getArgument(), filePlayed = null;
		if (!bot.isAllowedToPlaySound(event.getAuthor())) {
        	pm(event, lookupString(Strings.NOT_ALLOWED));
        	LOG.info(event.getAuthor() + " tried to play a sound file but is not allowed.");
        	return;
		}
    	try {
    		if (category != null) {
    			filePlayed = bot.playRandomFileForCategory(event.getAuthor(), category);
    		} else {
    			filePlayed = bot.playRandomFile(event.getAuthor());
    		}
    		if (filePlayed != null && bot.getUsersVoiceChannel(event.getAuthor()) != null) {
    			SoundFile file = bot.getDispatcher().getSoundFileByName(filePlayed);
	    		String codedFileName = "`" + filePlayed + "`";
	    		LOG.info("Played \"" + filePlayed + "\" in server " + event.getGuild().getName());
	    		StyledEmbedMessage msg = StyledEmbedMessage.forSoundFile(file, "You've Played a Random Sound", "Played random sound " + codedFileName);
	    		msg.addContent("You Can Report It", lookupString(Strings.SOUND_REPORT_INFO), false);
	    		embed(event, msg);
    		}
    	} catch (Exception e) {
    		LOG.warn("When playing random sound: " + e.toString() + " => " + e.getMessage());
    	}
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " [category]` - play a random sound (from `category` if specified)";
	}

}
