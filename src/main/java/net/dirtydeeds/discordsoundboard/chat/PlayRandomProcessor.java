package net.dirtydeeds.discordsoundboard.chat;

import java.util.HashMap;
import java.util.Map;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class PlayRandomProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("RandomSoundProcessor");
	private static final String MESSAGE_PREFIX = "Played random sound file ";
	private Map<MessageChannel,Message> pastMessages;
	
	public PlayRandomProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, soundPlayer);
		this.pastMessages = new HashMap<>();
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String category = getArgument(), filePlayed = null;
		if (!bot.isAllowedToPlaySound(event.getAuthor())) {
        	pm(event, "You are not allowed to play sounds using this bot.");
        	LOG.info(event.getAuthor() + " tried to play a sound file but is not allowed.");
        	return;
		}
    	try {
    		MessageChannel channel = event.getChannel();
    		if (!event.isPrivate() && channel != null) {
    			Message past = pastMessages.get(channel);
    			if (past != null && bot.hasPermissionInChannel((TextChannel)channel,
    					Permission.MESSAGE_MANAGE))
    				past.deleteMessage();
    		}
    		if (category != null) {
    			filePlayed = bot.playRandomFileForCategory(event.getAuthor(), category);
    		} else {
    			filePlayed = bot.playRandomFile(event.getAuthor());
    		}
    		if (filePlayed != null && bot.getUsersVoiceChannel(event.getAuthor()) != null) {
    			// Build an output string and send it to the channel.
    			SoundFile file = bot.getDispatcher().getSoundFileByName(filePlayed);
	    		String codedFileName = "`" + filePlayed + "`", desc = file.getDescription();
	    		String output = MESSAGE_PREFIX + codedFileName + " ";
	    		long numPlays = file.getNumberOfPlays() - 1; // Just got played.
	    		if (category != null) output += "from **" + file.getCategory() + "** ";
	    		if (desc != null && !desc.isEmpty()) output += "(" + desc + ") ";
	    		if (numPlays > 0)
	    			output += "which has been played **" + numPlays + "** times already ";
	    		output += event.getAuthor().getAsMention();
	    		channel.sendMessageAsync(output, (Message m)-> pastMessages.put(channel, m));
    		}
    	} catch (Exception e) {
    		LOG.warn("When playing random sound: " + e.toString() + " => " + e.getMessage());
    	}
	}
	
	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - play a random sound (from a category, if one is given)";
	}

}
