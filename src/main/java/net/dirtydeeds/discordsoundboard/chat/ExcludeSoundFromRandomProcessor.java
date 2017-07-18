package net.dirtydeeds.discordsoundboard.chat;

import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ExcludeSoundFromRandomProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {
	
	public ExcludeSoundFromRandomProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Exclude Sound From Being Randomed", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		String name = getArgument();
		Set<String> soundNames = bot.getSoundMap().keySet();
		if (name == null) {
			pm(event, formatString(Strings.NEED_NAME, getPrefix() + " " + StringUtils.randomString(soundNames)));
		} else if (!soundNames.contains(name)) {
			String suggestion = "Check your spelling.", possibleName = bot.getClosestMatchingSoundName(name);
			if (possibleName != null) {
				suggestion = "Did you mean `" + possibleName + "`?";
			}
      pm(event, formatString(Strings.NOT_FOUND, name) + " *" + suggestion + "* ");
		} else {
			SoundFile file = bot.getDispatcher().getSoundFileByName(name);
			if (file.isExcludedFromRandom()) {
				pm(event, "That sound was already excluded! Use the pair to this command to include it again.");
			} else {
				file.setExcludedFromRandom(true);
				pm(event, "That sound has been excluded from being played through random events.");
			}
			bot.getDispatcher().saveSound(file);
		}
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " <soundfile>` (`*`) \u2014 exclude a sound file from random events";
	}
	
}
