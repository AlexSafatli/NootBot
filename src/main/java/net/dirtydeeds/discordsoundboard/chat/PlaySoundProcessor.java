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

	public PlaySoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Play Sound", bot);
	}

	private void sendBadSoundMessage(MessageReceivedEvent event, String name,
	                                 String suggestion, User user) {
		StyledEmbedMessage msg = buildStyledEmbedMessage(event);
		msg.setTitle("Sound Not Found");
		msg.addDescription("Sound `" + name + "` not found. " + suggestion + Strings.SEPARATOR + user.getAsMention());
		msg.addContent("Search", lookupString(Strings.USE_SEARCH), true);
		embed(event, msg.isWarning(true));
	}

	private void play(MessageReceivedEvent event, String name) {
		try {
			bot.playFileForChatCommand(name, event);
		} catch (Exception e) {
			LOG.warn("Did not play sound.");
		}
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = event.getAuthor();
		String name = message.substring(1, message.length());
		if (!bot.isAllowedToPlaySound(user)) {
			pm(event, lookupString(Strings.NOT_ALLOWED));
			LOG.info(
			  String.format("%s isn't allowed to play sounds.", user.getName()));
		} else if (StringUtils.containsAny(name, '?')) {
			return;
		} else if (bot.getSoundMap().get(name) == null) {
			LOG.info("Sound was not found.");
			String suggestion = lookupString(Strings.CHECK_SPELLING),
			       possibleName = bot.getClosestMatchingSoundName(name);
			if (name.equals("help")) {
				suggestion = lookupString(Strings.SOUND_NOT_FOUND_HELP);
			} else if (possibleName != null) {
				suggestion = formatString(Strings.DID_YOU_MEAN, possibleName);
			} else {
				// Do a naive search to see if something contains this name. Stop early.
				for (String s : bot.getSoundMap().keySet()) {
					if (s.contains(name)) {
						suggestion = formatString(Strings.DID_YOU_MEAN, s);
						break;
					}
				}
			}
			LOG.info("Suggestion: " + suggestion);
			sendBadSoundMessage(event, name, suggestion, user);
		} else play(event, name);
	}

	@Override
	public String getCommandHelpString() {
		String help = getPrefix() + "soundfile - play a sound by name";
		Set<String> soundFileNames = bot.getSoundMap().keySet();
		if (!soundFileNames.isEmpty()) {
			help += " - e.g., " +
			        getPrefix() + StringUtils.randomString(soundFileNames);
		}
		return help;
	}
}
