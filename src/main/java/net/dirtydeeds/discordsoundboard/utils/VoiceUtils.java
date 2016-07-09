package net.dirtydeeds.discordsoundboard.utils;

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;

public class VoiceUtils {

	public static long numUsersInVoiceChannels(Guild guild) {
		// Does not count bots or users in voice channels.
    	long numUsers = 0;
    	for (VoiceChannel channel : guild.getVoiceChannels()) {
    		if (channel.getId().equals(guild.getAfkChannelId())) continue;
    		for (User user : channel.getUsers()) {
    			if (!user.isBot()) ++numUsers;
    		}
    	}
    	return numUsers;
	}
	
}
