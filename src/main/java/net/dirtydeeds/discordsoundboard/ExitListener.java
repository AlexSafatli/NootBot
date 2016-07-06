package net.dirtydeeds.discordsoundboard;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.events.voice.VoiceLeaveEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.utils.SimpleLog;

/**
 * @author asafatli.
 *
 * This class handles waiting for people to exit a discord voice channel.
 */
public class ExitListener extends ListenerAdapter {
    
    public static final SimpleLog LOG = SimpleLog.getLog("Exit");
    
    private SoundboardBot bot;
    
    public ExitListener(SoundboardBot bot) {
        this.bot = bot;
    }
    
	public void onVoiceLeave(VoiceLeaveEvent event) {

		User user = event.getUser();
    	if (bot.isUser(user) || bot.getConnectedChannel(event.getGuild()) == null)
    		return; // Ignore if it is just the bot or not even connected.
    	VoiceChannel channel = event.getOldChannel();
    	Guild guild = channel.getGuild();
    	AudioManager voice = bot.getAPI().getAudioManager(guild);
    	VoiceChannel botsChannel = voice.getConnectedChannel();
    	
    	LOG.info(user.getUsername() + " left " + channel.getName() + 
    			" in " + guild.getName());
    	
    	if (botsChannel != null && botsChannel.equals(channel)) {
    		if (channel.getUsers().size() > 1) return;
    	} else if (channel.getUsers().size() > 0) {
    		if (botsChannel != null && botsChannel.getUsers().size() == 1) bot.moveToChannel(channel);
    		return;
    	}
        for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
        	if (voiceChannel.equals(channel)) continue;
        	if (botsChannel != null && botsChannel.equals(voiceChannel)) {
        		if (voiceChannel.getUsers().size() > 1) return;
        	} else if (voiceChannel.getUsers().size() > 0 && !voiceChannel.getId().equals(guild.getAfkChannelId())) {
        		if (botsChannel != null && botsChannel.getUsers().size() == 1) bot.moveToChannel(voiceChannel);
        		return;
        	}
        }
        
        LOG.info("No more users found! Leaving voice channel in server " + guild.getName());
        voice.closeAudioConnection();
        
    }
}