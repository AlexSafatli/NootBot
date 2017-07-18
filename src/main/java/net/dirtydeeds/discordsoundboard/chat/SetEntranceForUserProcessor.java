package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class SetEntranceForUserProcessor extends AuthenticatedMultiArgumentChatCommandProcessor {
	
	public SetEntranceForUserProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Entrance for User", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String[] args = getArguments();
		if (args.length != 2) {
			pm(event, "Need two arguments: a **username** and **sound name**"); return;
		}
		String username = args[0];
		String fileName = args[1];
		if (fileName.isEmpty()) fileName = null; // Too lazy to change logic.
		User user = null;
		if (username != null) user = bot.getUserByName(username);
		if (fileName != null && user != null) {
			if (bot.getSoundMap().get(fileName) != null) {
				bot.setEntranceForUser(user, fileName, event.getAuthor());
				pm(event, "User **" + user.getName() + "** had entrance updated" +
						" to sound `" + fileName + "`.");
			} else {
				pm(event, lookupString(Strings.SOUND_NOT_FOUND));
			}
		} else if (fileName == null) {
			bot.setEntranceForUser(user, null, null);
			pm(event, "User **" + user.getName() + "** had their entrance cleared.");
		} else if (user == null) {
			pm(event, "Asked to change entrance for `" + username + "` but could not "
					+ "find user with that name.");
		}
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " <username>, <soundfile>` (`*`) \u2014 set a sound file for a user "
				+ "as their entrance sound when they join a channel";
	}

}
