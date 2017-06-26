package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class PlayRandomTopProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("RandomTopSoundProcessor");
	
	public PlayRandomTopProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, "Random Top Played Sound", soundPlayer);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String filePlayed = null;
		if (!bot.isAllowedToPlaySound(event.getAuthor())) {
        	pm(event, lookupString(Strings.NOT_ALLOWED));
        	LOG.info(event.getAuthor() + " tried to play a sound file but is not allowed.");
        	return;
		}
    	try {
			filePlayed = bot.getRandomTopPlayedSoundName();
			if (filePlayed != null) {
				bot.playFileForUser(filePlayed, event.getAuthor());
				LOG.info("Played random top sound in channel: \"" + filePlayed + "\".");
				SoundFile file = bot.getDispatcher().getSoundFileByName(filePlayed);
	    		LOG.info("Played \"" + filePlayed + "\" in server " + event.getGuild().getName());
	    		StyledEmbedMessage msg = StyledEmbedMessage.forSoundFile(file, "You've Played a Random Sound",
	    				"Played a random top sound " + event.getAuthor().getAsMention());
	    		msg.addContent("You Can Report It", lookupString(Strings.SOUND_REPORT_INFO), false);
	    		embed(event, msg);
			}
    	} catch (Exception e) {
        ;
    	}
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` - play a random top played sound";
	}

}
