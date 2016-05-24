package net.dirtydeeds.discordsoundboard;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.events.ReconnectedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.utils.SimpleLog;

/**
 * @author asafatli.
 *
 * This class handles listening to state of connection of the bot.
 */
public class ConnectionListener extends ListenerAdapter {
    
    public static final SimpleLog LOG = SimpleLog.getLog("Connection");

	public void onReconnect(ReconnectedEvent event) {
		JDA jda = event.getJDA();
		for (Guild guild : jda.getGuilds()) {
			// Attempt to recover from bad audio connection situations.
			LOG.info("Reopening audio connection for server " + guild);
			AudioManager voice = jda.getAudioManager(guild);
			VoiceChannel previousChannel = voice.getConnectedChannel();
			if (previousChannel == null) previousChannel = voice.getQueuedAudioConnection();
			voice.closeAudioConnection();
			if (previousChannel != null) {
				voice.openAudioConnection(previousChannel);
				LOG.info("Successfully reopened audio connection; previous channel was: \"" + 
						previousChannel.getName() + "\"");
			}
		}
    }
	
}
