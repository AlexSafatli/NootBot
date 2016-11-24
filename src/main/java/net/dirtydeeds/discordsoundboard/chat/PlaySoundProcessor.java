package net.dirtydeeds.discordsoundboard.chat;

import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class PlaySoundProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("SoundProcessor");
	private static final long PLAY_COUNT_FOR_ANNOUNCEMENT = 100;
	
	public PlaySoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = event.getAuthor();
        String name = message.substring(1, message.length());
    	LOG.info(String.format("%s wants to play \"%s\".", user.getUsername(), name));
        if (!bot.isAllowedToPlaySound(user)) {
        	pm(event, lookupString(Strings.NOT_ALLOWED));
        	LOG.info(String.format("%s isn't allowed to play sounds.", user.getUsername()));
        } else if (StringUtils.containsAny(name, '?')) {
        	return; // File names cannot contain question marks.
        } else if (bot.getSoundMap().get(name) == null) {
			String suggestion = "Check your spelling.", possibleName = bot.getClosestMatchingSoundName(name);
			if (possibleName != null) {
				LOG.info("Closest matching sound name is: " + possibleName);
				suggestion = "Did you mean `" + possibleName + "`?";
			}
			event.getChannel().sendMessageAsync(formatString(Strings.SOUND_NOT_FOUND_SUGGESTION,
					name, suggestion, user.getAsMention()), null);
        	LOG.info("Sound was not found.");
        } else {
	        try {
	            bot.playFileForChatCommand(name, event);
	            SoundFile sound = bot.getDispatcher().getSoundFileByName(name);
	            if (sound.getNumberOfPlays() % PLAY_COUNT_FOR_ANNOUNCEMENT == 0) {
	            	// Make an announcement every 100 plays.
	            	event.getChannel().sendMessageAsync(formatString(Strings.SOUND_PLAY_COUNT_ANNOUNCEMENT,
	            			name, sound.getNumberOfPlays()), null);
	            }
	        } catch (Exception e) {
	            LOG.fatal("Could not play file => " + e.toString());
	        }
        }
	}
	
	@Override
	public String getCommandHelpString() {
		String help = "`" + getPrefix() + "soundfile` - play a sound by name";
		Set<String> soundFileNames = bot.getSoundMap().keySet();
		if (!soundFileNames.isEmpty()) {
			help += " - e.g., `" + getPrefix() + StringUtils.randomString(soundFileNames) + "`";
		}
		return help;
	}

}
