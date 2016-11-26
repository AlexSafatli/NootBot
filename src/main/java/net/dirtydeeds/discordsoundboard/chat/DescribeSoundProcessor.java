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
        			" *" + suggestion + "* " + user.getAsMention(), null);
		} else {
			SoundFile file = bot.getDispatcher().getSoundFileByName(name);
			String desc = file.getDescription(), cat = file.getCategory(), uploader = "";
			net.dirtydeeds.discordsoundboard.beans.User blame = file.getUser();
			if (cat.equals("sounds")) cat = "Uncategorized";
			if (blame != null) uploader = blame.getUsername();
			Long plays = file.getNumberOfPlays();
			String strToSend;
			if (desc == null || desc.isEmpty()) {
				strToSend = formatString(Strings.SOUND_DESC, name, cat, plays) + ".";
			} else {
				strToSend = formatString(Strings.SOUND_DESC, name, cat, plays) + " with description: " + desc + ".";
			}
			if (!uploader.isEmpty()) strToSend += " *This file was uploaded by* **" + uploader + "**.";
			event.getChannel().sendMessageAsync(strToSend, null);
		}
	}

	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " <soundfile>` - get a source/description and uploader for a sound file";
	}
	
}
