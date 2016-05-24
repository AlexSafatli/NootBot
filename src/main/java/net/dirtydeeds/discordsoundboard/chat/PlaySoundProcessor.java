package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class PlaySoundProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("SoundProcessor");
	
	public PlaySoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = event.getAuthor();
        String file = message.substring(1, message.length());
        if (!bot.isAllowedToPlaySound(user)) {
        	pm(event, "You are explicitly forbidden to "
        			+ "play sounds using this bot by its owner **" + bot.getOwner() + "**.");
        	LOG.info(String.format("%s tried to play sound file %s but is not allowed.", user, file));
		} else if (bot.getAvailableSoundFiles().get(file) == null) {
        	event.getChannel().sendMessageAsync("The sound `" + file + "` was not found. "
        			+ "*Check your spelling.* " + user.getAsMention(), null);
        	LOG.info(String.format("%s tried to play sound file %s but it was not found.", user, file));
        } else {
        	LOG.info(String.format("%s is playing sound file %s.", user, file));
	        try {
	            bot.playFileForChatCommand(file, event);
	        } catch (Exception e) {
	            LOG.fatal("Could not play file " + file + " because: " + e.toString());
	        }
        }
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "soundfilename` - plays a file by name";
	}

}
