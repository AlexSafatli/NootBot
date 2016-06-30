package net.dirtydeeds.discordsoundboard.chat;

import java.util.HashMap;
import java.util.Map;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class PlayRandomProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("RandomSoundProcessor");
	private Map<MessageChannel,Message> pastMessages;
	
	public PlayRandomProcessor(String prefix, SoundboardBot soundPlayer) {
		super(prefix, soundPlayer);
		this.pastMessages = new HashMap<>();
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String category = getArgument();
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
    			String played = bot.playRandomFileForCategory(event.getAuthor(), category);
    			if (played == null && bot.getUsersVoiceChannel(event.getAuthor()) != null) {
    				channel.sendMessageAsync("No category `" + category + 
    						"` was found to play a random file from " + 
    						event.getAuthor().getAsMention(),
    						(Message m)-> pastMessages.put(channel, m));
    			} else if (played != null) {
    				channel.sendMessageAsync("Played random sound file `" + 
    						played + "` from **" + category + "** " + event.getAuthor().getAsMention(),
    						(Message m)-> pastMessages.put(channel, m));
    			}
    		} else {
    			String fileName = bot.playRandomFile(event.getAuthor());
    			channel.sendMessageAsync("Played random sound file `" + 
    					fileName + "` " + event.getAuthor().getAsMention(),
    					(Message m)-> pastMessages.put(channel, m));
    		}
    	} catch (Exception e) {
    		LOG.warn("Unable to play a random sound file because: " + e.toString());
    	}
	}
	
	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - plays a random file (if given, from a category)";
	}

}
