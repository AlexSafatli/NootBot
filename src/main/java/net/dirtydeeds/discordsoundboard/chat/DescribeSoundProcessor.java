package net.dirtydeeds.discordsoundboard.chat;

import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class DescribeSoundProcessor extends SingleArgumentChatCommandProcessor {
	
	public DescribeSoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = event.getAuthor();
		String name = getArgument();
		Set<String> soundNames = bot.getSoundMap().keySet();
		if (name == null) {
			pm(event, formatString(Strings.NEED_NAME, getPrefix() + " " + StringUtils.randomString(soundNames)));
		} else if (!soundNames.contains(name)) {
			String suggestion = "Check your spelling.", possibleName = bot.getClosestMatchingSoundName(name);
			if (possibleName != null) {
				suggestion = "Did you mean `" + possibleName + "`?";
			}
        	event.getChannel().sendMessageAsync(formatString(Strings.NOT_FOUND, name) + 
        			suggestion + "* " + user.getAsMention(), null);
		} else {
			SoundFile file = bot.getDispatcher().getSoundFileByName(name);
			String desc = file.getDescription(), cat = file.getCategory();
			if (cat.equals("sounds")) cat = "Uncategorized";
			Long plays = file.getNumberOfPlays();
			if (desc == null || desc.isEmpty()) {
				event.getChannel().sendMessageAsync(formatString(Strings.SOUND_DESC, name, cat, plays) + ".", null);
			} else {
				event.getChannel().sendMessageAsync(formatString(Strings.SOUND_DESC, name, cat, plays) + " - " + desc + ".", null);
			}
		}
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " <soundfile>` - get a source/description for a sound file";
	}
	
}
