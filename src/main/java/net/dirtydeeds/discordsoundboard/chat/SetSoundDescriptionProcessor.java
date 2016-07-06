package net.dirtydeeds.discordsoundboard.chat;

import java.util.Set;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class SetSoundDescriptionProcessor extends MultiArgumentChatCommandProcessor {

	private static final String EXAMPLE_DESCRIPTION = "this sound is magic";
	
	public SetSoundDescriptionProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	@Override
	protected void handleEvent(MessageReceivedEvent event, String message) {
		String[] args = getArgumentsCased(event);
		User user = event.getAuthor();
		int numArgs = (args != null) ? args.length : 0;
		Set<String> soundNames = bot.getSoundMap().keySet();
		if (numArgs != 2) {
			String randomSoundName = StringUtils.randomString(soundNames);
			pm(event, "You need to provide a sound name and a description. For example: `" + getPrefix() + 
					" " + randomSoundName + ", " + EXAMPLE_DESCRIPTION + "`.");
			return;
		}
		String name = args[0].toLowerCase(), desc = args[1];
		if (soundNames.contains(name) && desc != null) {
			SoundFile sound = bot.getDispatcher().getSoundFileByName(name);
			sound.setDescription(desc);
			bot.getDispatcher().saveSound(sound);
			event.getChannel().sendMessageAsync("Okay! The description for `" + name + 
					"` has been set to `" + desc + "`.", null);
		} else if (desc != null) {
			event.getChannel().sendMessageAsync("Oops! I couldn't find that sound `" + name + "` " +
					user.getAsMention(), null);
		} else {
			event.getChannel().sendMessageAsync("Oops! You didn't provide a description " + 
					user.getAsMention(), null);
		}
	}
	
	@Override
	public String getCommandHelpString() {
		String msg = "`" + getPrefix() + " <soundfile>, <description>` - add a description to a sound";
		if (bot.getSoundMap().size() > 0) {
			msg += " - e.g., `" + 
				getPrefix() + " " + StringUtils.randomString(bot.getSoundMap().keySet()) + ", " + 
				EXAMPLE_DESCRIPTION + "`.";
		}
		return msg;
	}

}
