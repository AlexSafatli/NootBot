package net.dirtydeeds.discordsoundboard.chat;

import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class PlaySoundProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("SoundProcessor");
	private static final long PLAY_COUNT_FOR_ANNOUNCEMENT = 50;
	
	public PlaySoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Play Sound", bot);
	}

	private void sendFailureMessage(MessageReceivedEvent event, String name, String suggestion, User user) {
		StyledEmbedMessage msg = new StyledEmbedMessage("Could Not Find Sound `" + name + "`");
		if (suggestion != null && !suggestion.isEmpty()) {
			msg.addDescription(suggestion);
		} else {
			msg.addDescription("Do you even know what you're looking for?");
		}
		msg.addContent("You Could Search For It", "*Use `.search` with a keyword to find sounds.*", false);
		embed(event, msg.isWarning(true));
	}
	
	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = event.getAuthor();
        String name = message.substring(1, message.length());
        if (!bot.isAllowedToPlaySound(user)) {
        	pm(event, lookupString(Strings.NOT_ALLOWED));
        	LOG.info(String.format("%s isn't allowed to play sounds.", user.getName()));
        } else if (StringUtils.containsAny(name, '?')) {
        	return; // File names cannot contain question marks.
        } else if (bot.getSoundMap().get(name) == null) {
        	LOG.info("Sound was not found.");
			String suggestion = "Check your spelling.", possibleName = bot.getClosestMatchingSoundName(name);
			if (name.equals("help")) {
				suggestion = "Were you trying to access the `.help` command?";
			} else if (possibleName != null) {
				LOG.info("Closest matching sound name is: " + possibleName);
				suggestion = "Did you mean `" + possibleName + "`?";
			} else {
				// Do a naive search to see if something contains this name. Take first match.
				for (String s : bot.getSoundMap().keySet()) {
					if (s.contains(name)) {
						suggestion = "Did you mean `" + s + "`?"; break;
					}
				}
			}
			sendFailureMessage(event, name, suggestion, user);
        } else {
	        try {
	            bot.playFileForChatCommand(name, event);
	            SoundFile sound = bot.getDispatcher().getSoundFileByName(name);
	            LOG.info("Played sound for " + user.getName());
	            if (sound.getNumberOfPlays() % PLAY_COUNT_FOR_ANNOUNCEMENT == 0) {
	            	// Make an announcement every so many plays.
	            	m(event, formatString(Strings.SOUND_PLAY_COUNT_ANNOUNCEMENT, name, sound.getNumberOfPlays()));
	            }
	        } catch (Exception e) {
	        	e.printStackTrace();
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
