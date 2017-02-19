package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class SetEntranceForUserProcessor extends AuthenticatedMultiArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("SetEntranceForUserProcessor");
	
	public SetEntranceForUserProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Entrance for User", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String username = getArguments()[0];
		String fileName = getArguments()[1];
		User user = null;
		if (username != null) user = bot.getUserByName(username);
		if (fileName != null && user != null) {
			if (bot.getSoundMap().get(fileName) != null) {
				bot.setEntranceForUser(user, fileName);
				pm(event, "User **" + user.getName() + "** had entrance updated" +
						" to sound `" + fileName + "`.");
			} else {
				pm(event, lookupString(Strings.SOUND_NOT_FOUND));
			}
		} else if (fileName == null) {
			bot.setEntranceForUser(user, null);
			pm(event, "User **" + user.getName() + "** had their entrance cleared.");
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
