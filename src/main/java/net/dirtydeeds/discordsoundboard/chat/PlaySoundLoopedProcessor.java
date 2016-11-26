package net.dirtydeeds.discordsoundboard.chat;

import java.util.Set;

import net.dirtydeeds.discordsoundboard.async.LambdaJob;
import net.dirtydeeds.discordsoundboard.async.SoundboardJob;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dirtydeeds.discordsoundboard.utils.Strings;
import net.dv8tion.jda.audio.player.FilePlayer;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class PlaySoundLoopedProcessor extends AuthenticatedMultiArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("SoundLoopProcessor");
	public static final int MAX_NUMBER_OF_LOOPED_PLAYS = 10;
	
	public PlaySoundLoopedProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = event.getAuthor();
		if (getArguments().length != 2) {
			pm(event, "Need sound name and number of times to play that sound.");
			return;
		}
        String name = getArguments()[0];
        int numTimesToPlay = Integer.valueOf(getArguments()[1]);
    	LOG.info(String.format("%s wants to play \"%s\".", user.getUsername(), name));
        if (!bot.isAllowedToPlaySound(user)) {
        	pm(event, lookupString(Strings.NOT_ALLOWED));
        	LOG.info(String.format("%s isn't allowed to play sounds.", user.getUsername()));
        } else if (numTimesToPlay <= 0 || numTimesToPlay > MAX_NUMBER_OF_LOOPED_PLAYS) {
        	pm(event, "Need to be <= **" + MAX_NUMBER_OF_LOOPED_PLAYS + "** for number of plays."); return;
        } else if (StringUtils.containsAny(name, '?')) {
        	return; // File names cannot contain question marks.
        } else if (bot.getSoundMap().get(name) == null) {
			String suggestion = "Check your spelling.", possibleName = bot.getClosestMatchingSoundName(name);
			if (possibleName != null) {
				LOG.info("Closest matching sound name is: " + possibleName);
				suggestion = "Did you mean `" + possibleName + "`?";
			}
			event.getChannel().sendMessageAsync(formatString(Strings.SOUND_NOT_FOUND_SUGGESTION,
					name, suggestion, user.getAsMention()), null);
        	LOG.info("Sound was not found.");
        } else {
    		SoundboardJob job = new LambdaJob((SoundboardBot b)-> {
    			if (b.equals(bot)) {
	    			for (int i = 0; i < numTimesToPlay; ++i) {
	    				FilePlayer p = (FilePlayer)bot.getAPI().getAudioManager(event.getGuild()).getSendingHandler();
	    				if (p != null) {
	    					p = (FilePlayer)bot.getAPI().getAudioManager(event.getGuild()).getSendingHandler();
	    					while (p.isPlaying()) p = (FilePlayer)bot.getAPI().getAudioManager(event.getGuild()).getSendingHandler();
	    				}
	    				try { b.playFileForChatCommand(name, event); } catch (Exception e) { ; }
	    			}
    			}
    		});
    		bot.getDispatcher().getAsyncService().runJob(job);
        }
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` `<soundfile>, X` - play a sound by name X number of times where X < **" + MAX_NUMBER_OF_LOOPED_PLAYS + "**";
	}

}
