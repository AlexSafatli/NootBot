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

	public static final SimpleLog LOG = SimpleLog.getLog("Sound");
	private static final long PLAY_COUNT_FOR_ANNOUNCEMENT = 50;

	public PlaySoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Play Sound", bot);
	}

	private void sendBadSoundMessage(MessageReceivedEvent event, String name,
	                                 String suggestion, User user) {
		StyledEmbedMessage msg = buildStyledEmbedMessage(event);
		msg.setTitle("No Sound `" + name + "` Found");
		msg.addDescription((suggestion != null && !suggestion.isEmpty()) ?
		                   suggestion : "What are you looking for?");
		msg.addContent("Search For It",
		               "Use `.search` with a keyword to find sounds.", true);
		embed(event, msg.isWarning(true));
	}

	private boolean play(MessageReceivedEvent event, String name) {
		boolean played = true;
		try {
			bot.playFileForChatCommand(name, event);
		} catch (Exception e) {
			played = false;
			LOG.warn("Did not play sound.");
		}
		return played;
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = event.getAuthor();
		String name = message.substring(1, message.length());
		if (!bot.isAllowedToPlaySound(user)) {
			pm(event, lookupString(Strings.NOT_ALLOWED));
			LOG.info(String.format("%s isn't allowed to play sounds.",
			                       user.getName()));
		} else if (StringUtils.containsAny(name, '?')) {
			return; // File names cannot contain question marks.
		} else if (bot.getSoundMap().get(name) == null) {
			LOG.info("Sound not found.");
			String suggestion = "Check your spelling.",
			       possibleName = bot.getClosestMatchingSoundName(name);
			if (name.equals("help")) {
				suggestion = "Were you trying to access the `.help` command?";
			} else if (possibleName != null) {
				suggestion = "Did you mean `" + possibleName + "`?";
			} else {
				// Do a naive search to see if something contains this name. Stop early.
				for (String s : bot.getSoundMap().keySet()) {
					if (s.contains(name)) {
						suggestion = "Did you mean `" + s + "`?"; break;
					}
				}
			}
			sendBadSoundMessage(event, name, suggestion, user);
		} else {
			SoundFile sound = bot.getDispatcher().getSoundFileByName(name);
			if (play(event, name) &&
			    sound.getNumberOfPlays() % PLAY_COUNT_FOR_ANNOUNCEMENT == 0) {
				// Make an announcement every so many plays.
				m(event, formatString(Strings.SOUND_PLAY_COUNT_ANNOUNCEMENT, name,
				                      sound.getNumberOfPlays()));
			}
		}
	}

	@Override
	public String getCommandHelpString() {
		String help = getPrefix() + "soundfile - play a sound by name";
		Set<String> soundFileNames = bot.getSoundMap().keySet();
		if (!soundFileNames.isEmpty()) {
			help += " - e.g., " + getPrefix() +
			        StringUtils.randomString(soundFileNames);
		}
		return help;
	}
}