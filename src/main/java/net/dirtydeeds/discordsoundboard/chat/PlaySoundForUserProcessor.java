package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class PlaySoundForUserProcessor extends
		MultiArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("SoundForUserProcessor");
	
	public PlaySoundForUserProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = event.getAuthor(), recipient = null;
		if (getArguments().length != 2) {
			pm(event, "This command requires two arguments - a **user** and a **sound** to play.");
			return;
		}
        String username = getArguments()[0], filename = getArguments()[1];
        if (username != null) recipient = bot.getUserByName(username);
        if (!bot.isAllowedToPlaySound(user)) {
        	pm(event, lookupString(Strings.NOT_ALLOWED));
        	LOG.info(String.format("%s tried to play sound file \"%s\" but is not allowed.", user, filename));
		} else if (bot.getSoundMap().get(filename) == null) {
			String suggestion = "Check your spelling.", possibleName = bot.getClosestMatchingSoundName(filename);
			if (possibleName != null) {
				LOG.info("Closest matching sound name is: " + possibleName);
				suggestion = "Did you mean `" + possibleName + "`?";
			}
        	pm(event, "The sound `" + filename + "` was not found. *" + suggestion + "* " + user.getAsMention());
        	LOG.info(String.format("%s tried to play sound file \"%s\" but it was not found.", 
        			user.getUsername(), filename));
		} else if (recipient == null) {
        	pm(event, lookupString(Strings.USER_NOT_FOUND));
        	LOG.info(String.format("%s tried to play sound file \"%s\" for username %s but user not found.", 
        			user.getUsername(), filename, username));			
		} else {
        	LOG.info(String.format("%s is playing sound file \"%s\" for user %s.", user.getUsername(), 
        			filename, recipient.getUsername()));
	        try {
	            String played = bot.playFileForUser(filename, recipient);
	            if (played != null)
	            	pm(event, formatString(Strings.USER_PLAY_SOUND_SUCCESS, played, recipient.getUsername(), 
	            			bot.getUsersVoiceChannel(recipient).getGuild().getName()));
	            else pm(event, formatString(Strings.USER_PLAY_SOUND_FAILURE, filename));
	        } catch (Exception e) {
	        	LOG.fatal("Could not play file " + filename + " because: " + e.toString());
	        }
        }
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " <username>, <soundfile>` - plays a file by name "
				+ "for a particular user; will move to their channel";
	}

}
