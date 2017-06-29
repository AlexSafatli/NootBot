package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.async.PlaySoundsJob;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class PlayRandomSoundLoopedProcessor extends MultiArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("RandomSoundLoopProcessor");
	public static final int MAX_NUMBER_OF_LOOPED_PLAYS = 12;
	
	public PlayRandomSoundLoopedProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Random Loop", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = event.getAuthor();
        String cat = (getArguments().length > 1) ? getArguments()[1] : null;
        int numTimesToPlay = (getArguments().length > 0) ? Integer.valueOf(getArguments()[0]) : 0;
        boolean privileged = bot.getUser(user).isPrivileged();
        if (cat != null && !cat.isEmpty() && !bot.isASoundCategory(cat)) {
        	w(event, formatString(Strings.NOT_FOUND, cat));
        	cat = null;
        }
        if (!bot.isAllowedToPlaySound(user)) {
        	pm(event, lookupString(Strings.NOT_ALLOWED));
        	LOG.info(String.format("%s isn't allowed to play sounds.", user.getName()));
        } else if (numTimesToPlay <= 0 || (numTimesToPlay > MAX_NUMBER_OF_LOOPED_PLAYS && !privileged)) {
        	e(event, "Need to be positive and <= **" + MAX_NUMBER_OF_LOOPED_PLAYS + "** for #/plays."); return;
        } else {
        	String[] sounds = new String[numTimesToPlay];
    		bot.getDispatcher().getAsyncService().runJob(new PlaySoundsJob(sounds, bot, user, cat));
        }
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + " X, <category>` - play a random sound X number of times where X < **" + MAX_NUMBER_OF_LOOPED_PLAYS + "**";
	}

}
