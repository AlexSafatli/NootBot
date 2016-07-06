package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class SetEntranceForUserProcessor extends AuthenticatedMultiArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("SetEntranceForUserProcessor");
	
	public SetEntranceForUserProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String username = getArguments()[0];
		String fileName = getArguments()[1];
		User user = null;
		if (username != null) user = bot.getUserByName(username);
		if (fileName != null && user != null) {
			if (bot.getSoundMap().get(fileName) != null) {
				bot.setEntranceForUser(user, fileName);
				pm(event, "User **" + user.getUsername() + "** had entrance updated" +
						" to sound `" + fileName + "`.");
			} else {
				pm(event, "That sound file does not exist. *Check your spelling.*");
			}
		} else if (fileName == null) {
			bot.setEntranceForUser(user, null);
			pm(event, "User **" + user.getUsername() + "** had their entrance updated" +
					" to no longer have a sound file.");
		} else if (user == null) {
			pm(event, "Asked to change entrance for `" + username + "` but could not "
					+ "find user with that name.");
		} else {
			pm(event, "Need two arguments: a **username** and a **filename**.");			
		}
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " <username>, <soundfile>` (`*`) - set a sound file for a user "
				+ "as their entrance sound when they join a channel";
	}

}
