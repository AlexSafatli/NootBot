package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class SetEntranceProcessor extends SingleArgumentChatCommandProcessor {

	private static final int WARNING_DURATION_IN_SECONDS = 5;
	private static final int MAX_DURATION_IN_SECONDS = 8;

	public SetEntranceProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Entrance", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String fileName = getArgument();
		if (bot.isAllowedToPlaySound(event.getAuthor())) {
			if (fileName == null) {
				bot.setEntranceForUser(event.getAuthor(), null, null);
				pm(event, "Cleared your entrance.");
			} else if (bot.getSoundMap().get(fileName) == null) {
				pm(event, lookupString(Strings.SOUND_NOT_FOUND));
			}
			SoundFile file = bot.getSoundMap().get(fileName);
			boolean authenticated = bot.isAuthenticated(event.getAuthor(),
			                        event.getGuild());
			if (!authenticated && file.getDuration() > MAX_DURATION_IN_SECONDS) {
				pm(event, "The file `" + fileName +
				   "` is far too **long**! *Pick something shorter*.");
				return;
			}
			bot.setEntranceForUser(event.getAuthor(), fileName, null);
			pm(event, "Updated your entrance to sound file `" + fileName + "`!");
			if (file.getDuration() > WARNING_DURATION_IN_SECONDS) {
				pm(event, "The file `" + fileName + "` may be a bit **long**. "
				   + "*Be careful of setting very long entrances!*");
			}
		}
	}

	@Override
	public String getCommandHelpString() {
		return getPrefix() + " <soundfile> - set a sound as your entrance for " +
		       "when you join a channel";
	}
}