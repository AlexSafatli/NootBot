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
		String category = getArgument(), filePlayed = null, desc = "Played a random sound ";
		if (!bot.isAllowedToPlaySound(event.getAuthor())) {
    	pm(event, lookupString(Strings.NOT_ALLOWED));
    	LOG.info(event.getAuthor() + " tried to play a sound file but is not allowed.");
    	return;
		}
  	try {
  		if (category != null && bot.isASoundCategory(category)) {
        desc += "from category **" + category + "** ";
  			filePlayed = bot.playRandomFileForCategory(event.getAuthor(), category);
  		} else {
  			filePlayed = bot.playRandomFile(event.getAuthor());
  		}
  		if (filePlayed != null && bot.getUsersVoiceChannel(event.getAuthor()) != null) {
  			SoundFile file = bot.getDispatcher().getSoundFileByName(filePlayed);
    		LOG.info("Played \"" + filePlayed + "\" in server " + event.getGuild().getName());
    		StyledEmbedMessage msg = StyledEmbedMessage.forSoundFile(file, "You've Played a Random Sound",
    				desc + event.getAuthor().getAsMention());
    		embed(event, msg);
  		}
  	} catch (Exception e) {
  		;
  	}
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " [category]` - play a random sound (from `category` if specified)";
	}

}
