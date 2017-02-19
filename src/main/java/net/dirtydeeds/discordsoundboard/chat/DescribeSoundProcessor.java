package net.dirtydeeds.discordsoundboard.chat;

import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dirtydeeds.discordsoundboard.utils.StyledEmbedMessage;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class DescribeSoundProcessor extends SingleArgumentChatCommandProcessor {
	
	public DescribeSoundProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Get Information for a Sound", bot);
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
        	m(event, formatString(Strings.NOT_FOUND, name) + " *" + suggestion + "* " + user.getAsMention());
		} else {
			SoundFile file = bot.getDispatcher().getSoundFileByName(name);
			embed(event, StyledEmbedMessage.forSoundFile(file, "Information for Sound `" + file.getSoundFileId() + "`", 
					"You requested information for a sound called `" + file.getSoundFileId() + "`."));
		}
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " <soundfile>` - get information for a sound file";
	}
	
}
